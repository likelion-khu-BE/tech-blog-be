package com.study.blog.application.comment.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentUpdateRequest {

  @NotBlank private String content;

  public String getContent() {
    return content;
  }
}
