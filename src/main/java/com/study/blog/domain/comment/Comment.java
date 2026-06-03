package com.study.blog.domain.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "blog_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "post_id", nullable = false)
  private Long postId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Comment parent;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "hidden_at")
  private LocalDateTime hiddenAt;

  @Builder
  public Comment(Long postId, Long userId, Comment parent, String content) {
    this.postId = postId;
    this.userId = userId;
    this.parent = parent;
    this.content = content;
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public boolean isHidden() {
    return hiddenAt != null;
  }

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void hide() {
    this.hiddenAt = LocalDateTime.now();
  }

  public Long getParentId() {
    return parent != null ? parent.getId() : null;
  }

  public void updateContent(String content) {
    this.content = content;
  }
}
