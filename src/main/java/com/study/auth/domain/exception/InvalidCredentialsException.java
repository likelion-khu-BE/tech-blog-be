package com.study.auth.domain.exception;

/**
 * 로그인 실패 — 이메일 미존재 또는 비밀번호 불일치.
 *
 * <p>어떤 이유인지 구분하지 않는다 — "이메일이 없습니다" vs "비밀번호가 틀렸습니다"를 나누면 공격자에게 가입된 이메일 목록을 노출하는 셈이다.
 */
public class InvalidCredentialsException extends AuthException {

  public InvalidCredentialsException() {
    super("이메일 또는 비밀번호가 올바르지 않습니다");
  }
}
