package com.study.blog.application.comment.dto;

import com.study.blog.domain.comment.Comment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
    Long id,
    String content,
    UUID userId,
    Long parentId,
    long likeCount,
    boolean liked,
    LocalDateTime createdAt,
    List<CommentResponse> replies) {

  public static CommentResponse of(Comment comment, long likeCount, boolean liked) {
    return new CommentResponse(
        comment.getId(),
        comment.getContent(),
        comment.getUserId(),
        comment.getParentId(),
        likeCount,
        liked,
        comment.getCreatedAt(),
        new ArrayList<>());
  }

  public void addReply(CommentResponse reply) {
    replies.add(reply);
  }
}
