package com.study.profile.domain.activity;

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
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 멤버 활동 이력 엔티티. 각 행 단위로 점수 부여 → 기여도 랭킹 산정에 활용. */
@Entity
@Table(name = "activity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ActivityType type;

  @Column(name = "reference_id")
  private Long referenceId;

  /** 활동의 부모 리소스 id (라우팅용). 댓글이면 글 id, 답변이면 질문 id 등. root 리소스(글/질문 등)는 null. */
  @Column(name = "parent_resource_id")
  private Long parentResourceId;

  /**
   * 활동을 일으킨 외부 행위자 (auth.User.id). 받은 좋아요({@code *_like_received}) 류에서 "누가 누른 좋아요로 인한 row인지" 식별에
   * 사용. 그 외 type은 null.
   */
  @Column(name = "actor_id")
  private Long actorId;

  @Column(name = "score", nullable = false)
  private Integer score = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /** 자기 행위 활동 생성 — 글/댓글/좋아요 누름/채택/발표 등. actor_id 없음. parentResourceId는 루트 리소스면 null. */
  public static Activity create(
      Member member,
      ActivityType type,
      Long referenceId,
      Long parentResourceId,
      int score) {
    return createInternal(member, type, referenceId, parentResourceId, null, score);
  }

  /** 받은 좋아요({@code *_like_received}) 활동 생성. actorId(누가 누른 좋아요인지) 명시. */
  public static Activity createReceived(
      Member member,
      ActivityType type,
      Long referenceId,
      Long parentResourceId,
      Long actorId,
      int score) {
    return createInternal(member, type, referenceId, parentResourceId, actorId, score);
  }

  private static Activity createInternal(
      Member member,
      ActivityType type,
      Long referenceId,
      Long parentResourceId,
      Long actorId,
      int score) {
    Activity activity = new Activity();
    activity.member = member;
    activity.type = type;
    activity.referenceId = referenceId;
    activity.parentResourceId = parentResourceId;
    activity.actorId = actorId;
    activity.score = score;
    return activity;
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
