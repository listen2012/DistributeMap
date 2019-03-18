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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * The writing side of the {@link TcpIpConnection}.
 */
public final class NonBlockingSocketWriter implements Runnable {


  public final Queue<OutboundFrame> writeQueue = new ConcurrentLinkedQueue<OutboundFrame>();

  private ByteBuffer outputBuffer = ByteBuffer.allocate(1024);

  private NonBlockingIOThread ioThread;
  
  private SocketChannel socketChannel;
  
  public NonBlockingSocketWriter(SocketChannel socketChannel, NonBlockingIOThread ioThread) {
    this.socketChannel = socketChannel;
    this.ioThread = ioThread;
  }
  

  public void write(OutboundFrame frame) {
    writeQueue.offer(frame);

    schedule();
  }


  private void schedule() {

    // We managed to schedule this WriteHandler. This means we need to add a task to
    // the ioThread and give it a kick so that it processes our frames.
    ioThread.addTaskAndWakeup(this);
  }



  @Override
  public void run() {
    try {
      handle();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  private void handle() throws Exception {
    fillOutputBuffer();
  }

  private void fillOutputBuffer() throws Exception {
    for (;;) {
      if (!outputBuffer.hasRemaining()) {
        // The buffer is completely filled, we are done.
        return;
      }
      poll();
    }
  }

  private OutboundFrame poll() {
    for (;;) {
      outputBuffer.clear();
      OutboundFrame frame = writeQueue.poll();
      frame.write(outputBuffer);
      outputBuffer.flip();
      try {
        socketChannel.write(outputBuffer);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return frame;
    }
  }




}
