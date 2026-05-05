package com.study.shared.extevent.qna;

/**
 * QnA 질문이 등록됐다는 도메인 사실.
 *
 * @param userId 질문자 (auth.User.id)
 * @param questionId 등록된 질문의 식별자
 */
public record QnaQuestionCreated(Long userId, Long questionId) {}
