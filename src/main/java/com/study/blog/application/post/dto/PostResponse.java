package com.study.blog.application.post.dto;

import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PostResponse {

  private final Long id;
  private final String title;
  private final String content;
  private final String board;
  private final String category;
  private final PostStatus status;
  private final String generation;
  private final Long repostFromId;
  private final UUID authorId;
  private final List<String> tags;
  private final long likeCount;
  private final long bookmarkCount;
  private final boolean liked;
  private final boolean bookmarked;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  private PostResponse(
      Long id,
      String title,
      String content,
      String board,
      String category,
      PostStatus status,
      String generation,
      Long repostFromId,
      UUID authorId,
      List<String> tags,
      long likeCount,
      long bookmarkCount,
      boolean liked,
      boolean bookmarked,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.title = title;
    this.content = content;
    this.board = board;
    this.category = category;
    this.status = status;
    this.generation = generation;
    this.repostFromId = repostFromId;
    this.authorId = authorId;
    this.tags = tags;
    this.likeCount = likeCount;
    this.bookmarkCount = bookmarkCount;
    this.liked = liked;
    this.bookmarked = bookmarked;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

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
        tags,
        likeCount,
        bookmarkCount,
        liked,
        bookmarked,
        post.getCreatedAt(),
        post.getUpdatedAt());
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public String getBoard() {
    return board;
  }

  public String getCategory() {
    return category;
  }

  public PostStatus getStatus() {
    return status;
  }

  public String getGeneration() {
    return generation;
  }

  public Long getRepostFromId() {
    return repostFromId;
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

  public long getBookmarkCount() {
    return bookmarkCount;
  }

  public boolean isLiked() {
    return liked;
  }

  public boolean isBookmarked() {
    return bookmarked;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
