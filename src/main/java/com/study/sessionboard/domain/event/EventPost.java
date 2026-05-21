package com.study.sessionboard.domain.event;

import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private Member author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "generation_number", nullable = false) // generation number로 시현 수정
  private Generation generation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventPostType type;

  @Column(nullable = false)
  private String title;

  private String body;

  @Column(nullable = false, columnDefinition = "text[]")
  private String[] tags = new String[0];

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventPostStatus status = EventPostStatus.DRAFT;

  @Column(name = "like_count", nullable = false)
  private int likeCount = 0;

  @Column(name = "comment_count", nullable = false)
  private int commentCount = 0;

  @Column(name = "has_thumb", nullable = false)
  private boolean hasThumb = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @PrePersist
  void prePersist() {
    createdAt = OffsetDateTime.now();
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  public static EventPost of(
      Member author,
      Generation generation,
      EventPostType type,
      String title,
      String body,
      String[] tags) {
    EventPost post = new EventPost();
    post.author = author;
    post.generation = generation;
    post.type = type;
    post.title = title;
    post.body = body;
    post.tags = tags != null ? tags : new String[0];
    post.status = EventPostStatus.PUBLISHED; // Default to PUBLISHED for now as per spec 1-3
    return post;
  }

  public void update(EventPostType type, String title, String body, String[] tags) {
    this.type = type;
    this.title = title;
    this.body = body;
    this.tags = tags != null ? tags : new String[0];
  }
}
