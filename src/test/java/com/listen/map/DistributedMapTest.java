package com.listen.map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.listen.Application;
import com.listen.nio.map.operation.MapManager;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class DistributedMapTest {

  @Test
  public void test() throws InterruptedException {
    String value = (String)MapManager.newInstance().get("remote");
    System.out.println(value);

  }
}
