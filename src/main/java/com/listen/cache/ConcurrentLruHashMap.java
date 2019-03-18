package com.listen.cache;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EvictionListener;

//@Service
public class ConcurrentLruHashMap {

  private final static int cacheSize = 3;
  private final static int historySize = 5;

  private ConcurrentMap<String, Integer> lrukMap;

  private ConcurrentMap<String, Integer> historyMap;

  @Autowired
  private RedissonClient redisson;

  @PostConstruct
  public void initMap() {
    EvictionListener<String, Integer> listener = new EvictionListener<String, Integer>() {
      @Override
      public void onEviction(String key, Integer value) {
        System.out.println("Evicted key=" + key + ", value=" + value);
        redisson.getBucket(key).delete();
      }
    };
    
    setHistoryMap(new ConcurrentLinkedHashMap.Builder<String, Integer>().maximumWeightedCapacity(cacheSize)
        .listener(listener).build());

  }

  public ConcurrentMap<String, Integer> getLrukMap() {
    return lrukMap;
  }

  public void setLrukMap(ConcurrentMap<String, Integer> lrukMap) {
    this.lrukMap = lrukMap;
  }

  public ConcurrentMap<String, Integer> getHistoryMap() {
    return historyMap;
  }

  public void setHistoryMap(ConcurrentMap<String, Integer> historyMap) {
    this.historyMap = historyMap;
  }


}
