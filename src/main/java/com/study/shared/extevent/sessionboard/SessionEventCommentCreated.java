package com.study.shared.extevent.sessionboard;

/**
 * 행사 게시글 댓글 작성 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param commentId 작성된 댓글
 */
public record SessionEventCommentCreated(Long userId, Long commentId) {}
