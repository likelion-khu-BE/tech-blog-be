package com.study.shared.extevent.sessionboard;

/**
 * 세션 발표자 등록 취소 사건.
 *
 * @param userId 발표자 (auth.User.id)
 * @param sessionId 세션
 */
public record SessionSpeakerUnregistered(Long userId, Long sessionId) {}
