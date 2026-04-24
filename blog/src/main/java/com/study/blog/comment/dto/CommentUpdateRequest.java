package com.study.blog.comment.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentUpdateRequest {

  @NotBlank
  private String content;

  public String getContent() {
    return content;
  }
}
