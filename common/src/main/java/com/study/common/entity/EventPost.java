package com.study.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventPost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "author_id", nullable = false)
  private UUID authorId;

  @Column(name = "generation_id", nullable = false)
  private Integer generationId;

  @Column(nullable = false)
  private String type;

  @Column(nullable = false)
  private String title;

  private String body;

  @Column(nullable = false, columnDefinition = "text[]")
  private String[] tags = new String[0];

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PostStatus status = PostStatus.draft;

  @Column(name = "like_count", nullable = false)
  private int likeCount = 0;

  @Column(name = "published_at")
  private OffsetDateTime publishedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() {
    createdAt = OffsetDateTime.now();
  }

  public static EventPost of(UUID authorId, Integer generationId, String type, String title) {
    EventPost post = new EventPost();
    post.authorId = authorId;
    post.generationId = generationId;
    post.type = type;
    post.title = title;
    return post;
  }
}
