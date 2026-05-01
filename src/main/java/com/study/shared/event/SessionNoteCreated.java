package com.study.shared.event;

/**
 * 세션 노트(발표 자료)가 작성됐다는 도메인 사실.
 *
 * <p>발행자는 sessionboard 모듈 — {@code SessionNote} 엔티티 작성 시점.
 *
 * @param userId 노트 작성자 (auth.User.id)
 * @param noteId 작성된 노트 ID
 */
public record SessionNoteCreated(Long userId, Long noteId) {}
