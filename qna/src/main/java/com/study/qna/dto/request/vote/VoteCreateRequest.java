package com.study.qna.dto.request.vote;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoteCreateRequest {

    /** 투표 타입. "UPVOTE" 또는 "DOWNVOTE". */
    @NotBlank
    private String type;
}
