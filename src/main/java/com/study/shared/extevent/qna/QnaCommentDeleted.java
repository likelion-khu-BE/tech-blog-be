package com.study.shared.extevent.qna;

/**
 * QnA 댓글 삭제 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param commentId 삭제된 댓글
 */
public record QnaCommentDeleted(Long userId, Long commentId) {}
