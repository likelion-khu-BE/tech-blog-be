package com.study.qna.application.dto.request.comment;

import jakarta.validation.constraints.NotBlank;

/**
 * 댓글 수정 요청 DTO.
 */
public record CommentUpdateRequest(@NotBlank String content) {}
