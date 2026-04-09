package com.study.profile.service;

import com.study.contract.blog.BlogPort;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

  private final BlogPort blogPort;

  public ProfileService(BlogPort blogPort) {
    this.blogPort = blogPort;
  }

  public String getBlogGreeting() {
    return blogPort.hello();
  }
}
