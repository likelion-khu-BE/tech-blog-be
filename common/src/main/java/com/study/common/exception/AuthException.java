package com.study.common.exception;

/** 인증/인가 관련 예외의 베이스 클래스. */
public abstract class AuthException extends RuntimeException {

  protected AuthException(String message) {
    super(message);
  }
}
