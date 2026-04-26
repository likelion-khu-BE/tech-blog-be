package com.study.blog.application.admin.dto;

import com.study.blog.domain.post.PostStatus;
import jakarta.validation.constraints.NotNull;

public class PostStatusUpdateRequest {

  @NotNull private PostStatus status;

  public PostStatus getStatus() {
    return status;
  }
}
