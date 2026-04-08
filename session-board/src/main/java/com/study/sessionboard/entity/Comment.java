package com.study.sessionboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private EventPost post;

  @Column(name = "author_id", nullable = false)
  private UUID authorId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Comment parent;

  @Column(nullable = false)
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() {
    createdAt = OffsetDateTime.now();
  }

  public static Comment of(EventPost post, UUID authorId, String content) {
    Comment comment = new Comment();
    comment.post = post;
    comment.authorId = authorId;
    comment.content = content;
    return comment;
  }

  public static Comment ofReply(EventPost post, UUID authorId, Comment parent, String content) {
    Comment comment = of(post, authorId, content);
    comment.parent = parent;
    return comment;
  }
}
