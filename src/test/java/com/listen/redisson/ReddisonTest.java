package com.listen.redisson;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.listen.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class ReddisonTest {

  @Autowired
  private RedissonClient redisson;

  @Test
  public void test() throws InterruptedException {


    RLock lock1 = redisson.getLock("lock");
    lock1.lock(30, TimeUnit.SECONDS);
    lock1.lock(10, TimeUnit.SECONDS);
    Thread t = new Thread() {
      public void run() {
          RLock lock2 = redisson.getLock("lock");
          try {
            if(lock2.isLocked()) {
                System.out.println("locked by other thread");
            }
//            Thread.sleep(300);
//            if(lock2.tryLock(4, TimeUnit.SECONDS)) {
            lock2.lock();
              System.out.println("lock2 acquired lock");
//            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      };
  };
    t.start();
    t.join();
    redisson.shutdown();
  }
}
