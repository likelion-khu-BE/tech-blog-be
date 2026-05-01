package com.study.qna.application.dto.request.vote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 답변 투표 생성 요청 DTO.
 */
public record VoteCreateRequest(
    @NotBlank
        @Pattern(regexp = "UPVOTE|DOWNVOTE", message = "type은 UPVOTE 또는 DOWNVOTE만 허용됩니다")
        String type) {}
