package com.study.shared.extevent.qna;

/**
 * QnA 질문 등록 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param questionId 등록된 질문
 */
public record QnaQuestionCreated(Long userId, Long questionId) {}
