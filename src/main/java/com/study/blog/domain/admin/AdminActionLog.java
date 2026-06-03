package com.study.blog.domain.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "admin_action_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminActionLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "actor_user_id", nullable = false)
  private Long actorUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 20)
  private AdminTargetType targetType;

  @Column(name = "target_id", nullable = false)
  private String targetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_type", nullable = false, length = 30)
  private AdminActionType actionType;

  @Column(name = "before_value", columnDefinition = "TEXT")
  private String beforeValue;

  @Column(name = "after_value", columnDefinition = "TEXT")
  private String afterValue;

  @Column(columnDefinition = "TEXT")
  private String reason;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public static AdminActionLog of(
      Long actorUserId,
      AdminTargetType targetType,
      String targetId,
      AdminActionType actionType,
      String beforeValue,
      String afterValue) {
    AdminActionLog log = new AdminActionLog();
    log.actorUserId = actorUserId;
    log.targetType = targetType;
    log.targetId = targetId;
    log.actionType = actionType;
    log.beforeValue = beforeValue;
    log.afterValue = afterValue;
    return log;
  }
}
