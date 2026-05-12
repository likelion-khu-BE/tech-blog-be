package com.study.shared.extevent.sessionboard;

/**
 * 세션 발표자 등록 사건.
 *
 * @param userId 발표자 (auth.User.id)
 * @param sessionId 세션
 */
public record SessionSpeakerRegistered(Long userId, Long sessionId) {}
