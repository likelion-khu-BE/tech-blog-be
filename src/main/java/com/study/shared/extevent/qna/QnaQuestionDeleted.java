package com.study.shared.extevent.qna;

/**
 * QnA 질문 삭제 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param questionId 삭제된 질문
 */
public record QnaQuestionDeleted(Long userId, Long questionId) {}
