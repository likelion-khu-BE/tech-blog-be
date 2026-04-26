package com.study.shared.exception;

import com.study.auth.domain.exception.EmailAlreadyExistsException;
import com.study.auth.domain.exception.InvalidCredentialsException;
import com.study.auth.domain.exception.InvalidTokenException;
import com.study.auth.domain.exception.TokenReusedException;
import com.study.auth.domain.exception.UserNotActiveException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 → HTTP 응답 매핑.
 *
 * <p>인증/인가 예외를 일관된 JSON 형식으로 반환한다. Spring Security의 AuthenticationEntryPoint / AccessDeniedHandler는
 * 필터 체인 레벨에서 401/403을 처리하고, 여기서는 서비스 레이어에서 발생하는 비즈니스 예외를 처리한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidCredentials(
      InvalidCredentialsException e) {
    // 401 — 이메일/비밀번호 불일치. 어떤 이유인지 클라이언트에 노출하지 않는다.
    return errorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler(UserNotActiveException.class)
  public ResponseEntity<Map<String, Object>> handleUserNotActive(UserNotActiveException e) {
    return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<Map<String, Object>> handleEmailExists(EmailAlreadyExistsException e) {
    return errorResponse(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(TokenReusedException.class)
  public ResponseEntity<Map<String, Object>> handleTokenReuse(TokenReusedException e) {
    return errorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException e) {
    return errorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
    // @Valid 검증 실패 시 첫 번째 에러 메시지만 반환
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .orElse("요청 값이 올바르지 않습니다");
    return errorResponse(HttpStatus.BAD_REQUEST, message);
  }

  private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(Map.of("status", status.value(), "message", message));
  }
}
