package com.study.shared.extevent.sessionboard;

/**
 * 행사 게시글 댓글 삭제 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param commentId 삭제된 댓글
 */
public record SessionEventCommentDeleted(Long userId, Long commentId) {}
