package com.study.blog.application.post.dto;

import com.study.blog.domain.post.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PostCreateRequest(
    @NotBlank String title,
    @NotBlank String content,
    @NotBlank String board,
    @NotBlank String category,
    @NotNull PostStatus status,
    @NotBlank String generation,
    List<String> tags,
    Long repostFromId) {}
