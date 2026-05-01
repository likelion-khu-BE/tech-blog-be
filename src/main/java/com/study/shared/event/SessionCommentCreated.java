package com.study.shared.event;

/**
 * 세션보드 게시글의 댓글이 작성됐다는 도메인 사실.
 *
 * <p>발행자는 sessionboard 모듈 — 정확한 도메인 매핑(예: {@code EventPostComment})은 sessionboard 팀과 합의 시 확정.
 *
 * @param userId 댓글 작성자 (auth.User.id)
 * @param commentId 작성된 댓글의 식별자
 */
public record SessionCommentCreated(Long userId, Long commentId) {}
