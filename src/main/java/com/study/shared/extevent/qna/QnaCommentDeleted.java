package com.study.shared.extevent.qna;

/**
 * QnA 댓글 삭제 사건. 질문 댓글 또는 답변 댓글 둘 다 처리.
 *
 * @param userId 작성자 (auth.User.id)
 * @param questionId 댓글이 속한 질문 (질문 댓글: 직접 / 답변 댓글: 답변의 부모 질문)
 * @param answerId 답변 댓글이면 그 답변의 id, 질문 댓글이면 null
 * @param commentId 삭제된 댓글
 */
public record QnaCommentDeleted(Long userId, Long questionId, Long answerId, Long commentId) {}
