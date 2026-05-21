package com.study.shared.extevent.auth;

/**
 * 회원가입 완료 사건. User 저장 직후, Member 프로필 생성을 트리거한다.
 *
 * @param userId 생성된 User.id
 * @param name 가입자 이름
 * @param sessionType 트랙 (backend / frontend / design / ai / pm / etc)
 */
public record UserSignedUpEvent(Long userId, String name, String sessionType) {}
