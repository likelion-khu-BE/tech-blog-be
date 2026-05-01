package com.study.qna.domain;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(
    name = "vote",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_vote_answer_user", columnNames = {"answer_id", "user_id"})
    })
@Getter
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 답변 투표 도메인 엔티티.
 *
 * <p>한 사용자는 한 답변에 최대 1회만 투표한다.
 */
public class Vote {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "answer_id", nullable = false)
  private Answer answer;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private VoteType type;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public static Vote create(Answer answer, Long userId, VoteType type) {
    Vote vote = new Vote();
    vote.answer = answer;
    vote.userId = userId;
    vote.type = type;
    return vote;
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}



