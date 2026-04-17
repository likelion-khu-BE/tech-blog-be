package com.study.common.exception;

/** 유효하지 않거나 만료된 토큰. */
public class InvalidTokenException extends AuthException {

  public InvalidTokenException(String message) {
    super(message);
  }
}
