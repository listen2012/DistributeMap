package com.listen.distributed.map;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class MapAcceptMain {
  
  public static void main(String[] args) throws IOException {
    int port = 9999;
    Queue<SocketChannel> socketQueue = new ArrayBlockingQueue<SocketChannel>(10);
    Thread accepter = new Thread(new SocketAccepter(port, socketQueue));
    accepter.start();

  }
}
