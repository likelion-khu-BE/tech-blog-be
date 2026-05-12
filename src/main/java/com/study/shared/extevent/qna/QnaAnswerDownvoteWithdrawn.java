package com.study.shared.extevent.qna;

/**
 * QnA 답변 비추천(downvote) 탈출 사건.
 *
 * <p>발행 시점: Vote가 downvote 상태에서 빠져나갈 때 — 직접 취소 또는 downvote → upvote 전환. 전환의 경우 {@link
 * QnaAnswerUpvoted}도 같이 발행.
 *
 * <p>답변 삭제 시 vote 일괄 제거는 별도 발행 불필요 — consumer가 {@link QnaAnswerDeleted}를 받아 cascade 처리.
 *
 * @param voterId 비추천 취소한 사람 (auth.User.id)
 * @param questionId 답변이 달린 질문 (consumer가 활동 응답 라우팅에 사용)
 * @param answerId 대상 답변
 */
public record QnaAnswerDownvoteWithdrawn(Long voterId, Long questionId, Long answerId) {}
