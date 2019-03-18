/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.listen.nio.nonblocking;

import static java.lang.System.currentTimeMillis;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NonBlockingIOThread extends Thread {
  private final static Logger logger = LoggerFactory.getLogger(NonBlockingIOThread.class);

  private static final int SELECT_WAIT_TIME_MILLIS = 5000;

  private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();

  private Selector readSelector;

  private Selector writeSelector;
  
  private final SelectorMode selectMode;

  public NonBlockingIOThread() {
    this.selectMode = SelectorMode.SELECT;
    this.readSelector = newSelector();
    this.writeSelector = newSelector();
  }

  private static Selector newSelector() {
    try {
      Selector selector = Selector.open();
      return selector;
    } catch (final IOException e) {
      throw new IllegalArgumentException("Failed to open a Selector", e);
    }
  }

  /**
   * Gets the Selector
   *
   * @return the Selector
   */
  public final Selector getReadSelector() {
    return readSelector;
  }

  public final Selector getWriteSelector() {
    return writeSelector;
  }


  /**
   * Adds a task to this NonBlockingIOThread without notifying the thread.
   *
   * @param task the task to add
   * @throws NullPointerException if task is null
   */
  public final void addTask(Runnable task) {
    taskQueue.add(task);
  }

  /**
   * Adds a task to be executed by the NonBlockingIOThread and wakes up the selector so that it will
   * eventually pick up the task.
   *
   * @param task the task to add.
   * @throws NullPointerException if task is null
   */
  public void addTaskAndWakeup(Runnable task) {
    taskQueue.add(task);
    if (selectMode != SelectorMode.SELECT_NOW) {
      readSelector.wakeup();
    }
  }

  @Override
  public final void run() {
    // This outer loop is a bit complex but it takes care of a lot of stuff:
    // * it calls runSelectNowLoop or runSelectLoop based on selectNow enabled or not.
    // * handles backoff and retrying in case if io exception is thrown
    // * it takes care of other exception handling.
    //
    // The idea about this approach is that the runSelectNowLoop and runSelectLoop are as clean as
    // possible and don't contain
    // any logic that isn't happening on the happy-path.
    try {
      for (;;) {
        try {
          switch (selectMode) {
            case SELECT_WITH_FIX:
              selectLoopWithFix();
              break;
            case SELECT_NOW:
              selectNowLoop();
              break;
            case SELECT:
              System.out.println("seletloop");
              selectLoop();
              break;
            default:
              throw new IllegalArgumentException(
                  "Selector.select mode not set, use -Dhazelcast.io.selectorMode="
                      + "{select|selectnow|selectwithfix} to explicitly specify select mode or leave empty for "
                      + "default select mode.");
          }
          // break the for loop; we are done
          break;
        } catch (IOException nonFatalException) {
          logger.warn(getName() + " " + nonFatalException.toString(), nonFatalException);
          coolDown();
        }
      }
    } catch (OutOfMemoryError e) {
      logger.error("oom", e.toString());
    } catch (Throwable e) {
      logger.warn("Unhandled exception in " + getName(), e);
    } finally {
      closeSelector();
    }

    logger.info(getName() + " finished");
  }

  /**
   * When an IOException happened, the loop is going to be retried but we need to wait a bit before
   * retrying. If we don't wait, it can be that a subsequent call will run into an IOException
   * immediately. This can lead to a very hot loop and we don't want that. A similar approach is
   * used in Netty
   */
  private void coolDown() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException i) {
      // if the thread is interrupted, we just restore the interrupt flag and let one of the loops
      // deal with it
      interrupt();
    }
  }

  private void selectLoop() throws IOException {
    while (!isInterrupted()) {
      processTaskQueue();

      int selectedKeys = readSelector.select(SELECT_WAIT_TIME_MILLIS);
      if (selectedKeys > 0) {
        handleSelectionKeys();
      }
    }
  }

  private void selectLoopWithFix() throws IOException {
    int idleCount = 0;
    while (!isInterrupted()) {
      processTaskQueue();

      long before = currentTimeMillis();
      int selectedKeys = readSelector.select(SELECT_WAIT_TIME_MILLIS);
      if (selectedKeys > 0) {
        idleCount = 0;
        handleSelectionKeys();
      } else if (!taskQueue.isEmpty()) {
        idleCount = 0;
      } else {
        // no keys were selected, not interrupted by wakeup therefore we hit an issue with
        // JDK/network stack
        long selectTimeTaken = currentTimeMillis() - before;
        idleCount = selectTimeTaken < SELECT_WAIT_TIME_MILLIS ? idleCount + 1 : 0;

        // if (selectorBugDetected(idleCount)) {
        // rebuildSelector();
        // idleCount = 0;
        // }
      }
    }
  }


  private void selectNowLoop() throws IOException {
    while (!isInterrupted()) {
      processTaskQueue();

      int selectedKeys = readSelector.selectNow();
      if (selectedKeys > 0) {
        handleSelectionKeys();
      }
    }
  }

  private void processTaskQueue() {
    while (!isInterrupted()) {
      Runnable task = taskQueue.poll();
      if (task == null) {
        return;
      }
      executeTask(task);
    }
  }

  private void executeTask(Runnable task) {
    task.run();
  }


  private void handleSelectionKeys() {
    Iterator<SelectionKey> it = readSelector.selectedKeys().iterator();
    while (it.hasNext()) {
      SelectionKey sk = it.next();
      it.remove();

      handleSelectionKey(sk);
    }
  }

  protected void handleSelectionKey(SelectionKey sk) {
    SelectionHandler handler = (SelectionHandler) sk.attachment();
    try {
      if (!sk.isValid()) {
        // if the selectionKey isn't valid, we throw this exception to feedback the situation into
        // the handler.onFailure
        throw new CancelledKeyException();
      }

      // we don't need to check for sk.isReadable/sk.isWritable since the handler has only
      // registered
      // for events it can handle.
      handler.handle();
    } catch (Throwable t) {
      handler.onFailure(t);
    }
  }

  private void closeSelector() {
    try {
      readSelector.close();
    } catch (Exception e) {
      logger.error("Failed to close selector", e);
    }
  }

  public final void shutdown() {
    taskQueue.clear();
    interrupt();
  }

  
}
