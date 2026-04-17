package com.study.common.exception;

/**
 * 이미 사용된 refresh token이 다시 제출됨 — 토큰 탈취 공격으로 간주.
 *
 * <p>이 예외가 발생하면 해당 family의 모든 토큰이 이미 REVOKED 처리된 상태다.
 */
public class TokenReusedException extends AuthException {

  public TokenReusedException() {
    super("토큰이 재사용되었습니다. 보안을 위해 모든 세션이 만료되었습니다. 다시 로그인해주세요.");
  }
}
