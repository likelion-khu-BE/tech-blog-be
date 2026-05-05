package com.study.shared.extevent.qna;

/**
 * QnA 답변이 채택됐다는 도메인 사실.
 *
 * <p>주의: {@code userId}는 <b>채택된 답변의 작성자</b> (점수 받는 사람) — 채택을 누른 질문자가 아님.
 *
 * @param userId 채택된 답변의 작성자 (auth.User.id)
 * @param answerId 채택된 답변의 식별자
 */
public record QnaAnswerAccepted(Long userId, Long answerId) {}
