package com.study.blog.shared.exception;

import com.study.blog.shared.ApiResponse;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("요청 형식이 올바르지 않습니다"));
  }
}
