package com.study.qna.dto.response.vote;

import com.study.common.entity.qna.Vote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyVoteResponse {

    /** 투표 타입. "UPVOTE", "DOWNVOTE", 또는 투표하지 않은 경우 null. */
    private String type;

    public static MyVoteResponse of(Vote vote) {
        return MyVoteResponse.builder()
            .type(vote.getType().name())
            .build();
    }

    public static MyVoteResponse noVote() {
        return MyVoteResponse.builder()
            .type(null)
            .build();
    }
}
