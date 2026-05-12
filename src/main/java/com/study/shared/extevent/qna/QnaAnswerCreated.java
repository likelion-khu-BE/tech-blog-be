package com.study.shared.extevent.qna;

/**
 * QnA 답변 작성 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param questionId 답변이 달린 질문 (consumer가 활동 응답 라우팅에 사용)
 * @param answerId 작성된 답변
 */
public record QnaAnswerCreated(Long userId, Long questionId, Long answerId) {}
