package com.study.profile.domain.activity;

import com.study.profile.domain.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "type", nullable = false)
  private ActivityType type;

  @Column(name = "reference_id")
  private Long referenceId;

  @Column(name = "score", nullable = false)
  private Integer score = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public static Activity create(Member member, ActivityType type, Long referenceId, int score) {
    Activity activity = new Activity();
    activity.member = member;
    activity.type = type;
    activity.referenceId = referenceId;
    activity.score = score;
    return activity;
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
