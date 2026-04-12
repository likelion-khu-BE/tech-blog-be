package com.study.common.exception;

/** 로그인 시도했으나 계정이 ACTIVE 상태가 아닌 경우 (PENDING, REJECTED, EXPIRED). */
public class UserNotActiveException extends AuthException {

  public UserNotActiveException() {
    super("계정이 활성화되지 않았습니다. 관리자 승인을 기다려주세요.");
  }
}
