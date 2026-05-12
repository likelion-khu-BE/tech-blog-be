package com.study.shared.extevent.qna;

/**
 * QnA 답변 비추천(downvote) 진입 사건.
 *
 * <p>발행 시점: Vote가 downvote 상태로 들어올 때 — 신규 downvote 생성 또는 upvote → downvote 전환. 전환의 경우 {@link
 * QnaAnswerUpvoteWithdrawn}도 같이 발행.
 *
 * <p>활동 type: {@code qna_answer_downvote} (+1). 답변 퀄리티 평가는 채택 시스템이 별도로 처리하므로 답변 받는 측엔 활동 X.
 *
 * @param voterId 비추천 누른 사람 (auth.User.id)
 * @param questionId 답변이 달린 질문 (consumer가 활동 응답 라우팅에 사용)
 * @param answerId 대상 답변
 */
public record QnaAnswerDownvoted(Long voterId, Long questionId, Long answerId) {}
