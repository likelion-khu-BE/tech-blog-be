package com.study.shared.extevent.sessionboard;

/**
 * 세션 발표자가 등록됐다는 도메인 사실.
 *
 * <p>발행자는 sessionboard 모듈 — {@code SessionSpeaker} 엔티티 등록 시점.
 *
 * @param userId 발표자 (auth.User.id)
 * @param sessionId 발표 대상 세션 ID
 */
public record SessionSpeakerRegistered(Long userId, Long sessionId) {}
