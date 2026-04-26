package com.study.auth.domain;

/**
 * 사용자 계정 상태.
 *
 * <p>가입 → PENDING → 관리자 승인 시 ACTIVE / 반려 시 REJECTED 7일 내 미승인 시 EXPIRED
 */
public enum UserStatus {
  PENDING,
  ACTIVE,
  REJECTED,
  EXPIRED
}
