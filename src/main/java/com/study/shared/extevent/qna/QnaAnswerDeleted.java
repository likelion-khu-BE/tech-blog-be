package com.study.shared.extevent.qna;

/**
 * QnA 답변 삭제 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param questionId 답변이 달린 질문 (consumer 활용은 자유 — 시그니처 일관 유지용)
 * @param answerId 삭제된 답변
 */
public record QnaAnswerDeleted(Long userId, Long questionId, Long answerId) {}
