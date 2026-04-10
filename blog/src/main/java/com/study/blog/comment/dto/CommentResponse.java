package com.study.blog.comment.dto;

import com.study.blog.comment.Comment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentResponse {

  private final Long id;
  private final String content;
  private final UUID userId;
  private final Long parentId;
  private final long likeCount;
  private final boolean liked;
  private final LocalDateTime createdAt;
  private final List<CommentResponse> replies;

  private CommentResponse(
      Long id,
      String content,
      UUID userId,
      Long parentId,
      long likeCount,
      boolean liked,
      LocalDateTime createdAt) {
    this.id = id;
    this.content = content;
    this.userId = userId;
    this.parentId = parentId;
    this.likeCount = likeCount;
    this.liked = liked;
    this.createdAt = createdAt;
    this.replies = new ArrayList<>();
  }

  public static CommentResponse of(Comment comment, long likeCount, boolean liked) {
    return new CommentResponse(
        comment.getId(),
        comment.getContent(),
        comment.getUserId(),
        comment.getParentId(),
        likeCount,
        liked,
        comment.getCreatedAt());
  }

  public void addReply(CommentResponse reply) {
    replies.add(reply);
  }

  public Long getId() {
    return id;
  }

  public String getContent() {
    return content;
  }

  public UUID getUserId() {
    return userId;
  }

  public Long getParentId() {
    return parentId;
  }

  public long getLikeCount() {
    return likeCount;
  }

  public boolean isLiked() {
    return liked;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public List<CommentResponse> getReplies() {
    return replies;
  }
}
