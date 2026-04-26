package com.study.blog.shared.exception;

import com.study.blog.shared.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * blog 모듈 전용 예외 핸들러.
 *
 * <p>주의: 클래스명이 `GlobalExceptionHandler`면 app 모듈의 동명 클래스와 Spring Bean 이름(`globalExceptionHandler`)이
 * 중복되어 ConflictingBeanDefinitionException 발생. 반드시 모듈 접두어를 붙인 이름을 사용할 것.
 */
@RestControllerAdvice(basePackages = "com.study.blog")
public class BlogExceptionHandler {

  @ExceptionHandler(BlogException.class)
  public ResponseEntity<ApiResponse<Void>> handleBlogException(BlogException ex) {
    return ResponseEntity.status(ex.getErrorCode().getStatus())
        .body(ApiResponse.error(ex.getMessage()));
  }
}
