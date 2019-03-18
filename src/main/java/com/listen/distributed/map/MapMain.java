package com.listen.distributed.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.listen.nio.map.operation.GetOperation;
import com.listen.nio.map.operation.PutOperation;
import com.listen.nio.nonblocking.NonBlockingIOThread;
import com.listen.nio.nonblocking.NonBlockingSocketWriter;
import com.listen.nio.nonblocking.OutboundFrame;

public class MapMain {

  // public static void main(String[] args) {
  // int port = 8888;
  // Queue<SocketChannel> socketQueue = new ArrayBlockingQueue<SocketChannel>(10);
  // SocketAccepter accepter = new SocketAccepter(port, socketQueue);
  //
  // }

  /* 标识数字 */
  private static int flag = 0;
  /* 缓冲区大小 */
  private static int BLOCK = 4096;
  /* 接受数据缓冲区 */
  private static ByteBuffer sendbuffer = ByteBuffer.allocate(BLOCK);
  /* 发送数据缓冲区 */
  private static ByteBuffer receivebuffer = ByteBuffer.allocate(BLOCK);
  /* 服务器端地址 */
  private final static InetSocketAddress SERVER_ADDRESS = new InetSocketAddress("localhost", 9999);

  public static void main(String[] args) throws IOException {
    // TODO Auto-generated method stub
    // 打开socket通道
    SocketChannel socketChannel = SocketChannel.open();
    // 设置为非阻塞方式
    socketChannel.configureBlocking(false);
    // 打开选择器
    Selector selector = Selector.open();
    // 注册连接服务端socket动作
    socketChannel.register(selector, SelectionKey.OP_CONNECT);
    // 连接
    socketChannel.connect(SERVER_ADDRESS);
    // 分配缓冲区大小内存

    Set<SelectionKey> selectionKeys;
    Iterator<SelectionKey> iterator;
    SelectionKey selectionKey;
    SocketChannel client;

    // Queue<SocketChannel> socketQueue = new ArrayBlockingQueue<SocketChannel>(10);
    // Thread accepter = new Thread(new SocketAccepter(8888, socketQueue));
    // accepter.start();

    while (true) {
      // 选择一组键，其相应的通道已为 I/O 操作准备就绪。
      // 此方法执行处于阻塞模式的选择操作。
      selector.select();
      // 返回此选择器的已选择键集。
      selectionKeys = selector.selectedKeys();
      // System.out.println(selectionKeys.size());
      iterator = selectionKeys.iterator();
      while (iterator.hasNext()) {
        selectionKey = iterator.next();
        if (selectionKey.isConnectable()) {
          System.out.println("client connect");
          client = (SocketChannel) selectionKey.channel();
          // 判断此通道上是否正在进行连接操作。
          // 完成套接字通道的连接过程。
          if (client.isConnectionPending()) {
            client.finishConnect();
            System.out.println("完成连接!");
            // sendbuffer.clear();
            OutboundFrame frame = assemblePacket0();
            NonBlockingIOThread ioThread = new NonBlockingIOThread();
            NonBlockingSocketWriter writer = new NonBlockingSocketWriter(socketChannel, ioThread);
            writer.write(frame);

            ioThread.start();
            // sendbuffer.flip();
            // client.write(sendbuffer);
          }
          client.register(selector, SelectionKey.OP_READ);
        } else if (selectionKey.isReadable()) {
          client = (SocketChannel) selectionKey.channel();
          // 将缓冲区清空以备下次读取
          receivebuffer.clear();
          // 读取服务器发送来的数据到缓冲区中
          int count = client.read(receivebuffer);
          receivebuffer.flip();
          if (count > 0) {
            byte[] newBytes = new byte[1024 * 1024];;
            receivebuffer.get(newBytes, 0, count);
            System.out.println("receive byte ------>" + newBytes);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(newBytes);
            ObjectInputStream in = new ObjectInputStream(byteIn);
            String receiveText = null;
            try {
              receiveText = (String) in.readObject();
            } catch (ClassNotFoundException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            byteIn.close();
            in.close();
            // String receiveText = new String(receivebuffer.array(), 0, count);
            System.out.println("客户端接受服务器端数据--:" + receiveText);
            // client.register(selector, SelectionKey.OP_WRITE);
          }

        }
        // else if (selectionKey.isWritable()) {
        // sendbuffer.clear();
        // client = (SocketChannel) selectionKey.channel();
        // sendText = "message from client--" + (flag++);
        // sendbuffer.put(sendText.getBytes());
        // // 将缓冲区各标志复位,因为向里面put了数据标志被改变要想从中读取数据发向服务器,就要复位
        // sendbuffer.flip();
        // client.write(sendbuffer);
        // System.out.println("客户端向服务器端发送数据--：" + sendText);
        // client.register(selector, SelectionKey.OP_READ);
        // }
      }
      selectionKeys.clear();
    }
  }

  private static OutboundFrame assemblePacket() {
    byte[] bytes = null;
    PutOperation op = new PutOperation("remote", "map");
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    try {
      ObjectOutputStream out = new ObjectOutputStream(byteStream);
      out.writeObject(op);
      bytes = byteStream.toByteArray();
      byteStream.close();
      out.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    OutboundFrame frame = new OutboundFrame(bytes);
    return frame;

  }

  private static OutboundFrame assemblePacket0() {
    byte[] bytes = null;
    GetOperation op = new GetOperation("remote");
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    try {
      ObjectOutputStream out = new ObjectOutputStream(byteStream);
      out.writeObject(op);
      bytes = byteStream.toByteArray();
      byteStream.close();
      out.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    OutboundFrame frame = new OutboundFrame(bytes);
    return frame;
    
  }
}
