package com.study.shared.extevent.qna;

/**
 * QnA 답변 삭제 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param answerId 삭제된 답변
 */
public record QnaAnswerDeleted(Long userId, Long answerId) {}
