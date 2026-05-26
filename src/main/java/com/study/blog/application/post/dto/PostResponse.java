package com.study.blog.application.post.dto;

import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostStatus;
import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
    Long id,
    String title,
    String content,
    String board,
    String category,
    PostStatus status,
    String rejectedReason,
    String generation,
    Long replyToId,
    Long authorId,
    String authorName,
    List<String> tags,
    long likeCount,
    long bookmarkCount,
    boolean liked,
    boolean bookmarked,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static PostResponse of(
      Post post,
      String authorName,
      List<String> tags,
      long likeCount,
      long bookmarkCount,
      boolean liked,
      boolean bookmarked) {
    return new PostResponse(
        post.getId(),
        post.getTitle(),
        post.getContent(),
        post.getBoard(),
        post.getCategory(),
        post.getStatus(),
        post.getRejectedReason(),
        post.getGeneration(),
        post.getReplyToId(),
        post.getUserId(),
        authorName,
        tags,
        likeCount,
        bookmarkCount,
        liked,
        bookmarked,
        post.getCreatedAt(),
        post.getUpdatedAt());
  }
}
