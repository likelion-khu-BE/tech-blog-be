package com.study.blog.application.post.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PostCreateRequest(
    @NotBlank String title,
    @NotBlank String content,
    @NotBlank String board,
    @NotBlank String category,
    @NotBlank String generation,
    List<String> tags,
    Long repostFromId) {}
