package com.study.blog.domain.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "blog_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(nullable = false, length = 20)
  private String board;

  @Column(nullable = false, length = 20)
  private String category;

  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "blog_post_status")
  private PostStatus status;

  @Column(nullable = false, length = 10)
  private String generation;

  @Column(name = "repost_from_id")
  private Long repostFromId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  public Post(
      Long userId,
      String title,
      String content,
      String board,
      String category,
      PostStatus status,
      String generation,
      Long repostFromId) {
    this.userId = userId;
    this.title = title;
    this.content = content;
    this.board = board;
    this.category = category;
    this.status = status;
    this.generation = generation;
    this.repostFromId = repostFromId;
  }

  public void update(
      String title, String content, String board, String category, PostStatus status) {
    this.title = title;
    this.content = content;
    this.board = board;
    this.category = category;
    this.status = status;
  }

  public void changeStatus(PostStatus status) {
    this.status = status;
  }
}
