package com.study.qna.application.dto.response.common;

/** 작성자 정보 응답 DTO. */
public record MemberSummaryResponse(Long userId, String nickname, int generation) {

  public static MemberSummaryResponse of(Long userId, String nickname, int generation) {
    return new MemberSummaryResponse(userId, nickname, generation);
  }
}
