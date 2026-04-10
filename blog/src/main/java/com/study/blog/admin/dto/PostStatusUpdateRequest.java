package com.study.blog.admin.dto;

import com.study.blog.post.PostStatus;
import jakarta.validation.constraints.NotNull;

public class PostStatusUpdateRequest {

  @NotNull
  private PostStatus status;

  public PostStatus getStatus() {
    return status;
  }
}
