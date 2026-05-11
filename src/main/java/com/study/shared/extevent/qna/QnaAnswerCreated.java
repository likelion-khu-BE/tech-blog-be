package com.study.shared.extevent.qna;

/**
 * QnA 답변 작성 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param answerId 작성된 답변
 */
public record QnaAnswerCreated(Long userId, Long answerId) {}
