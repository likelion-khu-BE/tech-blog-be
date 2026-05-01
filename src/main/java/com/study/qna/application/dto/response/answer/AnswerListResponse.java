package com.study.qna.application.dto.response.answer;

import java.util.List;

/** 답변 목록 응답 DTO. */
public record AnswerListResponse(
    AnswerDetailResponse acceptedAnswer, List<AnswerDetailResponse> answers) {

  public static AnswerListResponse of(List<AnswerDetailResponse> answers) {
    AnswerDetailResponse accepted =
        answers.stream().filter(AnswerDetailResponse::accepted).findFirst().orElse(null);
    List<AnswerDetailResponse> normalAnswers =
        answers.stream().filter(answer -> !answer.accepted()).toList();
    return new AnswerListResponse(accepted, normalAnswers);
  }
}
