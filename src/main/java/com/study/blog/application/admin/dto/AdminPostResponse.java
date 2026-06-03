package com.study.blog.application.admin.dto;

import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostStatus;
import java.time.LocalDateTime;
import java.util.List;

public record AdminPostResponse(
    Long id,
    String title,
    String board,
    String category,
    String generation,
    PostStatus status,
    String rejectedReason,
    Long authorId,
    List<String> tags,
    long likeCount,
    LocalDateTime createdAt,
    LocalDateTime hiddenAt) {

  public static AdminPostResponse of(Post post, List<String> tags, long likeCount) {
    return new AdminPostResponse(
        post.getId(),
        post.getTitle(),
        post.getBoard(),
        post.getCategory(),
        post.getGeneration(),
        post.getStatus(),
        post.getRejectedReason(),
        post.getUserId(),
        tags,
        likeCount,
        post.getCreatedAt(),
        post.getHiddenAt());
  }
}
