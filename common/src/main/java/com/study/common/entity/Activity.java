package com.study.common.entity;

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
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "activity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ActivityType type;

  @Column(name = "reference_id")
  private UUID referenceId;

  @Column(name = "reference_type")
  private String referenceType;

  @Column(nullable = false)
  private Integer score = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public static Activity create(Member member, ActivityType type, UUID referenceId, String referenceType, int score) {
    Activity activity = new Activity();
    activity.member = member;
    activity.type = type;
    activity.referenceId = referenceId;
    activity.referenceType = referenceType;
    activity.score = score;
    return activity;
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
