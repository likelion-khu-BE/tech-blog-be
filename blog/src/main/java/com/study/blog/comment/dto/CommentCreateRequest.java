package com.study.blog.comment.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentCreateRequest {

  @NotBlank
  private String content;

  /** null 이면 최상위 댓글, 있으면 대댓글 */
  private Long parentId;

  public String getContent() {
    return content;
  }

  public Long getParentId() {
    return parentId;
  }
}
