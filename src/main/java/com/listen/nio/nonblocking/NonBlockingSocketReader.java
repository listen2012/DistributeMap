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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

import com.listen.nio.map.operation.BasicOperation;
import com.listen.nio.map.operation.MapManager;


/**
 * The writing side of the {@link TcpIpConnection}.
 */
public final class NonBlockingSocketReader implements Runnable, SelectionHandler {


  public final Queue<OutboundFrame> writeQueue = new ConcurrentLinkedQueue<OutboundFrame>();

  private ByteBuffer inputBuffer = ByteBuffer.allocate(1024);

  private NonBlockingIOThread ioThread;

  private SocketChannel socketChannel;

  private SelectionKey selectionKey;

  public NonBlockingSocketReader(SocketChannel socketChannel, NonBlockingIOThread ioThread) {
    this.socketChannel = socketChannel;
    this.ioThread = ioThread;
  }


  public void init() {
    ioThread.addTaskAndWakeup(this);
  }

  @Override
  public void run() {
    try {
      getSelectionKey();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected SelectionKey getSelectionKey() throws IOException {
    if (selectionKey == null) {
      selectionKey = socketChannel.register(ioThread.getReadSelector(), SelectionKey.OP_READ, this);
    }
    return selectionKey;
  }

  @Override
  public void handle() throws Exception {
    int readBytes = socketChannel.read(inputBuffer);
    if (readBytes <= 0) {
      if (readBytes == -1) {
        throw new IOException("Remote socket closed!");
      }
      return;
    }
    inputBuffer.flip();
    Object obj = readBuffer(inputBuffer);
    writeResponse(obj);
    inputBuffer.clear();

  }

  private Object readBuffer(ByteBuffer buffer) throws IOException {
    Object value = null;
    ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer.array());
    System.out.println("before run --> " + MapManager.newInstance().get("remote"));
    try {
      ObjectInputStream in = new ObjectInputStream(byteIn);
      FutureTask<Object> f = new FutureTask<Object>(new Callable<Object>() {
        public Object call() throws Exception {
          BasicOperation op = (BasicOperation) in.readObject();
          op.run();
          return op.getResponse();
        }
      });
      new Thread(f).start();
      value = f.get();
      System.out.println("after run --> " + MapManager.newInstance().get("remote"));
      System.out.println("future response --> " + value);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      byteIn.close();
    }
    return value;
  }

  private void writeResponse(Object obj) throws IOException {
    int writeReady = ioThread.getWriteSelector().selectNow();

    if(writeReady > 0){
        Set<SelectionKey>      selectionKeys = ioThread.getWriteSelector().selectedKeys();
        Iterator<SelectionKey> keyIterator   = selectionKeys.iterator();

        while(keyIterator.hasNext()){
            SelectionKey key = keyIterator.next();

            NonBlockingSocketReader socket = (NonBlockingSocketReader) key.attachment();
            socket.socketChannel.write(inputBuffer);
            socket.socketChannel.register(ioThread.getWriteSelector(), SelectionKey.OP_READ);
            keyIterator.remove();
        }

        selectionKeys.clear();

    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    try {
      oos.writeObject(obj);
      oos.flush();
      byte[] response = bos.toByteArray();
      inputBuffer.put(response);
      socketChannel.write(inputBuffer);
    } catch (IOException e) {

    } finally {
      oos.close();
      bos.close();
    }

  }

  @Override
  public void onFailure(Throwable throwable) {};



}
