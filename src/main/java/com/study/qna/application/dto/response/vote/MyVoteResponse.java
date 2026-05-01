package com.study.qna.application.dto.response.vote;

import com.study.qna.domain.Vote;

/** 내 투표 상태 응답 DTO. */
public record MyVoteResponse(String type) {

  public static MyVoteResponse of(Vote vote) {
    return new MyVoteResponse(vote.getType().name());
  }

  public static MyVoteResponse noVote() {
    return new MyVoteResponse(null);
  }
}
