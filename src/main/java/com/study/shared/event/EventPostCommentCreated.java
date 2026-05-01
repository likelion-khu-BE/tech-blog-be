package com.study.shared.event;

/**
 * 행사 게시글 댓글이 작성됐다는 도메인 사실.
 *
 * <p>발행자는 sessionboard 모듈 — {@code EventPostComment} 엔티티 작성 시점.
 *
 * @param userId 댓글 작성자 (auth.User.id)
 * @param commentId 작성된 댓글 ID
 */
public record EventPostCommentCreated(Long userId, Long commentId) {}
