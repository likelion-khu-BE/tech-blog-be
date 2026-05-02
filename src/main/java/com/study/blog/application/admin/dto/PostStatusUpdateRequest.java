package com.study.blog.application.admin.dto;

import com.study.blog.domain.post.PostStatus;
import jakarta.validation.constraints.NotNull;

public record PostStatusUpdateRequest(@NotNull PostStatus status) {}
