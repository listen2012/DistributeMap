package com.listen;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LockServletInitializer extends SpringBootServletInitializer{

     @Override
protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
            return application.sources(Application.class);
        }
//    public static void main(String[] args) {
//        SpringApplication.run(MailServletInitializer.class, args);
//    }
}