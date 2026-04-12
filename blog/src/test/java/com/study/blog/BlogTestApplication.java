package com.study.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"com.study.blog", "com.study.common.entity"})
public class BlogTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(BlogTestApplication.class, args);
  }
}
