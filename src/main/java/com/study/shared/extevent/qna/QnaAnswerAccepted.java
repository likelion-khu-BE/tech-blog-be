package com.study.shared.extevent.qna;

/**
 * QnA 답변 채택 사건. userId는 채택된 답변 작성자 (점수 받는 사람).
 *
 * @param userId 답변 작성자 (auth.User.id)
 * @param answerId 채택된 답변
 */
public record QnaAnswerAccepted(Long userId, Long answerId) {}
