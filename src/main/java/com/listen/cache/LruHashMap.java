package com.listen.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//@Service
public class LruHashMap {

  private final static int cacheSize = 4;
  private final static int historySize = 5;

  private LinkedHashMap<String, Integer> lrukMap;

  private LinkedHashMap<String, Integer> historyMap;

  @Autowired
  private RedissonClient redisson;

  @PostConstruct
  public void initMap() {
    setLrukMap(new LinkedHashMap<String, Integer>(5, 0.75f, true) {
      private static final long serialVersionUID = 6019921984475033463L;

      @Override
      public boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest) {
        boolean tooBig = size() > cacheSize;
        if (tooBig) {
          String eldestKey = (String) eldest.getKey();
           redisson.getBucket(eldestKey).delete();
        }
        return tooBig;
      }
    });

    setHistoryMap(new LinkedHashMap<String, Integer>(5, 0.75f, true) {
      private static final long serialVersionUID = 5676398059406878447L;

      @Override
      public boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest) {
        boolean tooBig = size() > historySize;
        if (tooBig) {
          String eldestKey = (String) eldest.getKey();
//           redisson.getBucket(eldestKey).delete();
        }
        return tooBig;
      }

      @Override
      public Integer put(String key, Integer value) {
        try {
          int count = (int) (historyMap.get(key) == null ? 0 : historyMap.get(key));
          if (count > 3) {
            historyMap.remove(key);
            if (lrukMap.containsKey(key)) {
              lrukMap.put(key, lrukMap.get(key) + 1);
            } else {
              lrukMap.put(key, count);
            }
            return null;
          }
          return super.put(key, count + 1);
        } finally {
        }
      }
    });

  }

  public LinkedHashMap<String, Integer> getLrukMap() {
    return lrukMap;
  }

  public void setLrukMap(LinkedHashMap<String, Integer> lrukMap) {
    this.lrukMap = lrukMap;
  }

  public LinkedHashMap<String, Integer> getHistoryMap() {
    return historyMap;
  }

  public void setHistoryMap(LinkedHashMap<String, Integer> historyMap) {
    this.historyMap = historyMap;
  }

}
