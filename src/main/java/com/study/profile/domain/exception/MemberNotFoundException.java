package com.study.profile.domain.exception;

/** 멤버를 찾을 수 없을 때 (잘못된 memberId, 또는 profile-init 미완료 상태 userId). 404로 매핑. */
public class MemberNotFoundException extends ProfileException {
  public MemberNotFoundException(Long id) {
    super("멤버를 찾을 수 없습니다: " + id);
  }
}
