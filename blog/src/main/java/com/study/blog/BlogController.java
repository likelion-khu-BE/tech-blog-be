package com.study.blog;

import com.study.blog.service.BlogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Blog 모듈 기본 엔드포인트. */
@RestController
@RequestMapping("/api/blog")
public class BlogController {

  private final BlogService blogService;

  public BlogController(BlogService blogService) {
    this.blogService = blogService;
  }

  @GetMapping("/hello")
  public String hello() {
    return blogService.hello();
  }
}
