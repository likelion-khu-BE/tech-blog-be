package com.study.profile.domain.exception;

/**
 * profile 도메인 예외 베이스. 모든 profile 도메인 예외는 이 클래스를 상속한다.
 *
 * <p>이유: GlobalExceptionHandler에서 도메인 단위로 공통 처리 가능 + 다른 BC 예외와 카탈로그 분리 (qna {@code QnaException},
 * auth {@code AuthException} 등과 일관).
 */
public abstract class ProfileException extends RuntimeException {
  protected ProfileException(String message) {
    super(message);
  }
}
