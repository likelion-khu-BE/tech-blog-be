package com.study.qna.application.dto.response.answer;

import com.study.qna.application.dto.response.common.MemberSummaryResponse;
import com.study.qna.domain.Answer;
import java.time.Instant;

/** 답변 상세 응답 DTO. */
public record AnswerDetailResponse(
    Long id,
    String content,
    boolean accepted,
    int upvoteCount,
    int downvoteCount,
    int commentCount,
    MemberSummaryResponse author,
    Instant createdAt,
    Instant updatedAt) {

  public static AnswerDetailResponse from(Answer answer, MemberSummaryResponse author) {
    return new AnswerDetailResponse(
        answer.getId(),
        answer.getContent(),
        answer.isAccepted(),
        0,
        0,
        answer.getCommentCount(),
        author,
        answer.getCreatedAt(),
        answer.getUpdatedAt());
  }

  public static AnswerDetailResponse of(
      Answer answer, MemberSummaryResponse author, int upvoteCount, int downvoteCount) {
    return new AnswerDetailResponse(
        answer.getId(),
        answer.getContent(),
        answer.isAccepted(),
        upvoteCount,
        downvoteCount,
        answer.getCommentCount(),
        author,
        answer.getCreatedAt(),
        answer.getUpdatedAt());
  }
}
