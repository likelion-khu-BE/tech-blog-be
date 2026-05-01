package com.study.qna.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "comment")
@Getter
@DynamicUpdate
@SQLDelete(sql = "UPDATE comment SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * QnA 댓글 도메인 엔티티.
 *
 * <p>질문 또는 답변에 연결되는 1단계 댓글만 지원한다.
 */
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id")
  private Question question;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "answer_id")
  private Answer answer;

  @Column(name = "content", nullable = false, columnDefinition = "text")
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  public static Comment createForQuestion(Long userId, Question question, String content) {
    Comment comment = new Comment();
    comment.userId = userId;
    comment.question = question;
    comment.content = content;
    return comment;
  }

  public static Comment createForAnswer(Long userId, Answer answer, String content) {
    Comment comment = new Comment();
    comment.userId = userId;
    comment.answer = answer;
    comment.content = content;
    return comment;
  }

  public void update(String content) {
    this.content = content;
  }

  public boolean isAuthor(Long userId) {
    return this.userId.equals(userId);
  }

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    this.updatedAt = Instant.now();
  }
}



