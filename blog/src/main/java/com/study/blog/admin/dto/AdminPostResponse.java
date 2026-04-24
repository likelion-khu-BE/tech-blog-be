package com.study.blog.admin.dto;

import com.study.common.entity.Post;
import com.study.common.entity.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AdminPostResponse {

  private final Long id;
  private final String title;
  private final String board;
  private final String category;
  private final String generation;
  private final PostStatus status;
  private final UUID authorId;
  private final List<String> tags;
  private final long likeCount;
  private final LocalDateTime createdAt;

  private AdminPostResponse(
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
    this.id = id;
    this.title = title;
    this.board = board;
    this.category = category;
    this.generation = generation;
    this.status = status;
    this.authorId = authorId;
    this.tags = tags;
    this.likeCount = likeCount;
    this.createdAt = createdAt;
  }

  public static AdminPostResponse of(Post post, List<String> tags, long likeCount) {
    return new AdminPostResponse(
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

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getBoard() {
    return board;
  }

  public String getCategory() {
    return category;
  }

  public String getGeneration() {
    return generation;
  }

  public PostStatus getStatus() {
    return status;
  }

  public UUID getAuthorId() {
    return authorId;
  }

  public List<String> getTags() {
    return tags;
  }

  public long getLikeCount() {
    return likeCount;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
