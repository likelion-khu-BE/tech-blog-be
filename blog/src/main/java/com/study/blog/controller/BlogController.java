package com.study.blog.controller;

import com.study.contract.blog.BlogPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlogController {

  private final BlogPort blogPort;

  public BlogController(BlogPort blogPort) {
    this.blogPort = blogPort;
  }

  @GetMapping("/api/blog/hello")
  public String hello() {
    return blogPort.hello();
  }
}
