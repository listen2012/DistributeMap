package com.listen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ScheduleConfig {

  @Bean
  public TaskScheduler getTaskScheduler() {
    ThreadPoolTaskScheduler taskSchedule = new ThreadPoolTaskScheduler();
    taskSchedule.setPoolSize(100);
    return taskSchedule;
  }

}
