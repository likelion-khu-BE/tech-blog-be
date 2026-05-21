package com.study.blog.application.post.dto;

import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostStatus;
import java.time.LocalDateTime;
import java.util.List;

public record PostSummaryResponse(
    Long id,
    String title,
    String board,
    String category,
    String generation,
    PostStatus status,
    Long authorId,
    String authorName,
    Long replyToId,
    String replyToTitle,
    List<String> tags,
    long likeCount,
    LocalDateTime createdAt) {

  public static PostSummaryResponse of(
      Post post, String authorName, String replyToTitle, List<String> tags, long likeCount) {
    return new PostSummaryResponse(
        post.getId(),
        post.getTitle(),
        post.getBoard(),
        post.getCategory(),
        post.getGeneration(),
        post.getStatus(),
        post.getUserId(),
        authorName,
        post.getReplyToId(),
        replyToTitle,
        tags,
        likeCount,
        post.getCreatedAt());
  }
}
