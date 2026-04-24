package com.study.common.entity;

/**
 * Refresh Token 상태.
 *
 * <p>ACTIVE → rotation 시 USED로 전환. 재사용 감지 또는 로그아웃 시 family 전체 REVOKED.
 */
public enum RefreshTokenStatus {
  ACTIVE,
  USED,
  REVOKED
}
