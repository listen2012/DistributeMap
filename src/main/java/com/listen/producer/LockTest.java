package com.listen.producer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Service
public class LockTest {

  final Lock evictionLock = new ReentrantLock();

  @Test
  public void test() {
    Thread t1 = new Thread() {
      public void run() {
        try {
          evictionLock.lock();
          System.out.println("t1 get lock");
          Thread.sleep(5000);
        } catch (Exception e) {
        } finally {
          evictionLock.unlock();

        }
      }
    };

    t1.run();
    if (evictionLock.tryLock()) {
      System.out.println("t2 get lock");
    }
  }
}
