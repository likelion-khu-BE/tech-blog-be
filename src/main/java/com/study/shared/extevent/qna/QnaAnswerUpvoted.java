package com.study.shared.extevent.qna;

/**
 * QnA 답변 추천(upvote) 진입 사건.
 *
 * <p>발행 시점: Vote가 upvote 상태로 들어올 때 — 신규 upvote 생성 또는 downvote → upvote 전환. 전환의 경우 {@link
 * QnaAnswerDownvoteWithdrawn}도 같이 발행.
 *
 * <p>활동 type: {@code qna_answer_upvote} (+1).
 *
 * @param voterId 추천 누른 사람 (auth.User.id)
 * @param questionId 답변이 달린 질문 (consumer가 활동 응답 라우팅에 사용)
 * @param answerId 대상 답변
 */
public record QnaAnswerUpvoted(Long voterId, Long questionId, Long answerId) {}
