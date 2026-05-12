package com.study.profile.domain.activity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 활동 기록 실패 영구 로그.
 *
 * <p>Listener에서 throw된 예외를 운영자가 사후 검토할 수 있게 영구 보관. ADR 0003 §처리 실패 시 복구 전략의 "ERROR 로그 + alert + 수동
 * 보정"에서 로그 부분을 DB로 백업.
 *
 * <p>운영자가 SQL 조회로 누락 발견 → payload_json + error_msg 보고 잘못의 출처(listener 버그 / 인프라 상태 / publisher 측 잘못
 * 등) 판단 → 수동 보정 또는 책임자 추적.
 */
@Entity
@Table(name = "activity_failure")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityFailure {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  /** 이벤트 클래스명 (예: "BlogPostCreated"). */
  @Column(name = "event_type", nullable = false)
  private String eventType;

  /** 이벤트 원본 직렬화 (Jackson). 운영자가 재처리에 사용. */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb")
  private String payloadJson;

  /** 예외 클래스명 (예: "IllegalStateException"). */
  @Column(name = "error_class", nullable = false)
  private String errorClass;

  @Column(name = "error_msg", columnDefinition = "text")
  private String errorMsg;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public static ActivityFailure of(
      String eventType, String payloadJson, String errorClass, String errorMsg) {
    ActivityFailure failure = new ActivityFailure();
    failure.eventType = eventType;
    failure.payloadJson = payloadJson;
    failure.errorClass = errorClass;
    failure.errorMsg = errorMsg;
    return failure;
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
