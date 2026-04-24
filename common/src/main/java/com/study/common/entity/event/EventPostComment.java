package com.study.common.entity.event;

import com.study.common.entity.User;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_post_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventPostComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private EventPost post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private EventPostComment parent;

  @Column(nullable = false)
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() {
    createdAt = OffsetDateTime.now();
  }

  public static EventPostComment of(EventPost post, User author, String content) {
    EventPostComment eventPostComment = new EventPostComment();
    eventPostComment.post = post;
    eventPostComment.author = author;
    eventPostComment.content = content;
    return eventPostComment;
  }

  public static EventPostComment ofReply(EventPost post, User author, EventPostComment parent, String content) {
    EventPostComment eventPostComment = of(post, author, content);
    eventPostComment.parent = parent;
    return eventPostComment;
  }
}
