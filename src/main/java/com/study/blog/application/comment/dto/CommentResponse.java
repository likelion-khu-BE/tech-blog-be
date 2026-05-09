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
    if (comment.isDeleted()) {
      return new CommentResponse(
          comment.getId(),
          "삭제된 댓글입니다.",
          null,
          comment.getParentId(),
          0,
          false,
          comment.getCreatedAt(),
          List.copyOf(replies));
    }
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
