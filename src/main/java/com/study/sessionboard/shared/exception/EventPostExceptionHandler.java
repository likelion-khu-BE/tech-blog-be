package com.study.sessionboard.shared.exception;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * sessionboard 모듈 전용 예외 핸들러.
 *
 * <p>BlogExceptionHandler와 Bean 이름 충돌을 피하기 위해 basePackages를 명시한다.
 */
@RestControllerAdvice(basePackages = "com.study.sessionboard")
public class EventPostExceptionHandler {

  @ExceptionHandler(EventPostException.class)
  public ResponseEntity<Map<String, String>> handleEventPostException(EventPostException ex) {
    return ResponseEntity.status(ex.getErrorCode().getStatus())
        .body(Map.of("message", ex.getMessage()));
  }
}
