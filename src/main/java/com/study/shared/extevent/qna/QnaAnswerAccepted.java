package com.study.shared.extevent.qna;

/**
 * QnA 답변 채택 사건. userId는 채택된 답변 작성자 (점수 받는 사람).
 *
 * @param userId 답변 작성자 (auth.User.id)
 * @param questionId 답변이 달린 질문 (consumer가 활동 응답 라우팅에 사용)
 * @param answerId 채택된 답변
 */
public record QnaAnswerAccepted(Long userId, Long questionId, Long answerId) {}
