package com.study.shared.extevent.qna;

/**
 * QnA 답변 채택 취소 사건. userId는 답변 작성자 (점수 회수되는 사람).
 *
 * @param userId 답변 작성자 (auth.User.id)
 * @param questionId 답변이 달린 질문 (consumer 활용은 자유 — 시그니처 일관 유지용)
 * @param answerId 채택 취소된 답변
 */
public record QnaAnswerUnaccepted(Long userId, Long questionId, Long answerId) {}
