package com.study.blog.application.post.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PostUpdateRequest(
    @NotBlank String title,
    @NotBlank String content,
    @NotBlank String board,
    @NotBlank String category,
    List<String> tags) {}
