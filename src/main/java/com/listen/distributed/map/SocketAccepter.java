package com.listen.distributed.map;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

import com.listen.nio.nonblocking.NonBlockingIOThread;
import com.listen.nio.nonblocking.NonBlockingSocketReader;

public class SocketAccepter implements Runnable {

  private int tcpPort;
  private ServerSocketChannel serverSocket = null;

  private Queue socketQueue = null;

  public SocketAccepter(int tcpPort, Queue socketQueue) {
    this.tcpPort = tcpPort;
    this.socketQueue = socketQueue;
  }



  public void run() {
    try {
      this.serverSocket = ServerSocketChannel.open();
      this.serverSocket.bind(new InetSocketAddress("localhost", tcpPort));
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }


    for (;;) {
      try {
        SocketChannel socketChannel = this.serverSocket.accept();

        System.out.println("Socket accepted: " + socketChannel);

        // todo check if the queue can even accept more sockets.
        this.socketQueue.add(socketChannel);
        
        socketChannel = (SocketChannel)this.socketQueue.poll();
        socketChannel.configureBlocking(false);
        NonBlockingIOThread ioThread = new NonBlockingIOThread();
        new NonBlockingSocketReader(socketChannel, ioThread).init();

        ioThread.run();

      } catch (IOException e) {
        e.printStackTrace();
      }

    }

  }
}
