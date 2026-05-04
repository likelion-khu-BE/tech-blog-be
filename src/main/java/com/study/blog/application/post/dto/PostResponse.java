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
    String generation,
    Long repostFromId,
    Long authorId,
    String authorEmail,
    List<String> tags,
    long likeCount,
    long bookmarkCount,
    boolean liked,
    boolean bookmarked,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static PostResponse of(
      Post post,
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
        post.getGeneration(),
        post.getRepostFromId(),
        post.getUserId(),
        post.getAuthorEmail(),
        tags,
        likeCount,
        bookmarkCount,
        liked,
        bookmarked,
        post.getCreatedAt(),
        post.getUpdatedAt());
  }
}
