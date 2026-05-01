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
@Table(name = "answer")
@Getter
@DynamicUpdate
@SQLDelete(sql = "UPDATE answer SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * QnA 답변 도메인 엔티티.
 *
 * <p>답변 본문, 채택 여부, 순추천수(voteCount), 댓글 수를 관리한다.
 */
public class Answer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "content", nullable = false, columnDefinition = "text")
  private String content;

  @Column(name = "accepted", nullable = false)
  private boolean accepted = false;

  @Column(name = "vote_count", nullable = false)
  private int voteCount = 0;

  @Column(name = "comment_count", nullable = false)
  private int commentCount = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  public static Answer create(Question question, Long userId, String content) {
    Answer answer = new Answer();
    answer.question = question;
    answer.userId = userId;
    answer.content = content;
    return answer;
  }

  public void update(String content) {
    this.content = content;
  }

  public void accept() {
    this.accepted = true;
  }

  public void cancelAccept() {
    this.accepted = false;
  }

  public boolean isAuthor(Long userId) {
    return this.userId.equals(userId);
  }

  public void applyVote(VoteType type) {
    if (type == VoteType.UPVOTE) {
      this.voteCount += 1;
      return;
    }
    this.voteCount -= 1;
  }

  public void cancelVote(VoteType type) {
    if (type == VoteType.UPVOTE) {
      this.voteCount -= 1;
      return;
    }
    this.voteCount += 1;
  }

  public void incrementCommentCount() {
    this.commentCount += 1;
  }

  public void decrementCommentCount() {
    if (this.commentCount > 0) {
      this.commentCount -= 1;
    }
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



