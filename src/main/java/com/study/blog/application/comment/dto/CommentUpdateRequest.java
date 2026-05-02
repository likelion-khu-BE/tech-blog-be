package com.study.blog.application.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(@NotBlank String content) {}
