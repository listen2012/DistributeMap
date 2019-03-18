package com.listen.producer;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

//@Service
public class TaskProducer {
  private final static Logger log = LoggerFactory.getLogger(TaskProducer.class);
  private final String JOB = "string:test";
  private final String cron = "0 1/1 * * * ? ";
  

  @Autowired
  private RedissonClient redisson;

  @Scheduled(cron = cron)
  public void job1() throws InterruptedException {
      Thread.sleep(10);
      log.info("定时任务启动");
      RLock lock = redisson.getLock(JOB);
      boolean getLock = false;
      try {
          //todo 若任务执行时间过短，则有可能在等锁的过程中2个服务任务都会获取到锁，这与实际需要的功能不一致，故需要将waitTime设置为0
          if (lock.tryLock(0, 5, TimeUnit.SECONDS)) {
            log.info("Redisson get lock:{},ThreadName :{}", JOB, Thread.currentThread().getName());
            getLock = true;
            System.out.println("task execute ---- ");
          } else {
              log.info("Redisson can't get lock:{},ThreadName :{}", JOB, Thread.currentThread().getName());
          }
      } catch (InterruptedException e) {
         log.error("Redisson 获取分布式锁异常",e);
      }finally {
          if (!getLock) {
              return;
          }
          lock.unlock();
          log.info("Redisson relase lock:{},ThreadName :{}", JOB, Thread.currentThread().getName());
      }
  }
  
  @Scheduled(cron = cron)
  public void job2() throws InterruptedException {
      Thread.sleep(10);
      log.info("定时任务启动");
      RLock lock = redisson.getLock(JOB);
      boolean getLock = false;
      try {
          //todo 若任务执行时间过短，则有可能在等锁的过程中2个服务任务都会获取到锁，这与实际需要的功能不一致，故需要将waitTime设置为0
          if (lock.tryLock(0, 5, TimeUnit.SECONDS)) {
            log.info("Redisson get lock:{},ThreadName :{}", JOB, Thread.currentThread().getName());
            getLock = true;
            System.out.println("task execute === ");
          } else {
              log.info("Redisson can't get lock:{},ThreadName :{}", JOB, Thread.currentThread().getName());
          }
      } catch (InterruptedException e) {
         log.error("Redisson 获取分布式锁异常",e);
      }finally {
          if (!getLock) {
              return;
          }
          lock.unlock();
          log.info("Redisson relase lock:{},ThreadName :{}", JOB, Thread.currentThread().getName());
      }
  }
}
