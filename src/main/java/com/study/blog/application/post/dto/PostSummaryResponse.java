package com.study.blog.application.post.dto;

import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostSummaryResponse(
    Long id,
    String title,
    String board,
    String category,
    String generation,
    PostStatus status,
    UUID authorId,
    List<String> tags,
    long likeCount,
    LocalDateTime createdAt) {

  public static PostSummaryResponse of(Post post, List<String> tags, long likeCount) {
    return new PostSummaryResponse(
        post.getId(),
        post.getTitle(),
        post.getBoard(),
        post.getCategory(),
        post.getGeneration(),
        post.getStatus(),
        post.getUserId(),
        tags,
        likeCount,
        post.getCreatedAt());
  }
}
