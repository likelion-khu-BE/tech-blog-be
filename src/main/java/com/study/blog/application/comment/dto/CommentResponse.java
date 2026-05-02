package com.study.blog.application.comment.dto;

import com.study.blog.domain.comment.Comment;
import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
    Long id,
    String content,
    Long userId,
    Long parentId,
    long likeCount,
    boolean liked,
    LocalDateTime createdAt,
    List<CommentResponse> replies) {

  public static CommentResponse of(
      Comment comment, long likeCount, boolean liked, List<CommentResponse> replies) {
    return new CommentResponse(
        comment.getId(),
        comment.getContent(),
        comment.getUserId(),
        comment.getParentId(),
        likeCount,
        liked,
        comment.getCreatedAt(),
        List.copyOf(replies));
  }
}
