package com.study.sessionboard.application.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
    @NotBlank @Size(max = 500, message = "댓글은 500자 이하여야 합니다") String content) {}
