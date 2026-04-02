package com.study.blog.service;

import com.study.contract.blog.BlogPort;
import org.springframework.stereotype.Service;

@Service
public class BlogService implements BlogPort {

  @Override
  public String hello() {
    return "hello blog";
  }
}
