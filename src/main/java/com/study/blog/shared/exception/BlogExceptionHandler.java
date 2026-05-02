package com.study.blog.shared.exception;

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
  public ResponseEntity<ErrorResponse> handleBlogException(BlogException ex) {
    int status = ex.getErrorCode().getStatus().value();
    return ResponseEntity.status(status).body(new ErrorResponse(status, ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, "요청 형식이 올바르지 않습니다"));
  }
}
