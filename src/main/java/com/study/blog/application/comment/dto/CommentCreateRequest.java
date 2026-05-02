package com.study.blog.application.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(@NotBlank String content, Long parentId) {
  /** null 이면 최상위 댓글, 있으면 대댓글 */
}
