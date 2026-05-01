package com.study.shared.event;

/**
 * QnA 답변이 작성됐다는 도메인 사실.
 *
 * @param userId 답변 작성자 (auth.User.id)
 * @param answerId 작성된 답변의 식별자
 */
public record QnaAnswerCreated(Long userId, Long answerId) {}
