package com.study.qna.application.dto.request.comment;

import jakarta.validation.constraints.NotBlank;

/** 댓글 생성 요청 DTO. */
public record CommentCreateRequest(@NotBlank String content) {}
