package com.study.blog.common.exception;

import com.study.blog.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BlogException.class)
  public ResponseEntity<ApiResponse<Void>> handleBlogException(BlogException ex) {
    return ResponseEntity.status(ex.getErrorCode().getStatus())
        .body(ApiResponse.error(ex.getMessage()));
  }
}
