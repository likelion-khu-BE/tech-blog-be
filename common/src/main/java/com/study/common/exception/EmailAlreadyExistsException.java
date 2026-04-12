package com.study.common.exception;

/** 회원가입 시 이미 등록된 이메일. */
public class EmailAlreadyExistsException extends AuthException {

  public EmailAlreadyExistsException() {
    super("이미 등록된 이메일입니다");
  }
}
