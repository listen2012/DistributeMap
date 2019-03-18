package com.listen.producer;

import java.util.LinkedHashMap;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.listen.cache.LruHashMap;

//@Service
public class LinkedTaskProducer {
  private final static Logger log = LoggerFactory.getLogger(LinkedTaskProducer.class);

  @Autowired
  private RedissonClient redisson;

  @Autowired
  private LruHashMap lru;

  @Scheduled(cron = "0/10 * * * * ? ")
  public void job0() throws InterruptedException {
    RBucket<String> key = redisson.getBucket("lru:0");
    if (key.isExists()) {
      String v = key.get();
      lru.getHistoryMap().put(v, 1);
    } else {
      key.set("lru:0");
    }
  }

  @Scheduled(cron = "0/15 * * * * ? ")
  public void job1() throws InterruptedException {
    RBucket<String> key = redisson.getBucket("lru:1");
    if (key.isExists()) {
      String v = key.get();
      lru.getHistoryMap().put(v, 1);
    } else {
      key.set("lru:1");
    }
  }

  @Scheduled(cron = "0/30 * * * * ? ")
  public void job2() throws InterruptedException {
    RBucket<String> key = redisson.getBucket("lru:2");
    if (key.isExists()) {
      String v = key.get();
      lru.getHistoryMap().put(v, 1);
    } else {
      key.set("lru:2");
    }
  }

  @Scheduled(cron = "0/58 * * * * ? ")
  public void job3() throws InterruptedException {
    RBucket<String> key = redisson.getBucket("lru:3");
    if (key.isExists()) {
      String v = key.get();
      lru.getHistoryMap().put(v, 1);
    } else {
      key.set("lru:3");
    }
  }

  @Scheduled(cron = "0/120 * * * * ? ")
  public void job4() throws InterruptedException {
    RBucket<String> key = redisson.getBucket("lru:4");
    if (key.isExists()) {
      String v = key.get();
      lru.getHistoryMap().put(v, 1);
    } else {
      key.set("lru:4");
    }
  }

  @Scheduled(cron = "0/60 * * * * ? ")
  public void job5() throws InterruptedException {
    LinkedHashMap<String, Integer> map = lru.getLrukMap();
    map.forEach((k, v) -> {
      System.out.println("Item : " + k + "----- Count : " + v);

    });
    System.out.println("**************");
  }



}
