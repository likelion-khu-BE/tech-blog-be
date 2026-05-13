package com.study.profile.application.dto;

import com.study.profile.domain.activity.Activity;
import com.study.profile.domain.activity.ActivityType;
import com.study.profile.domain.activity.ContributionPeriodType;
import java.time.Instant;
import java.util.Map;

/**
 * Activity 도메인 DTO 모음 — 응답(Response) + Repository projection.
 *
 * <p>※ 컨벤션상 응답 DTO는 {@code presentation/dto/}가 정석이나, profile 기존 패턴(application/dto)을 따른다. 별도
 * refactor PR로 전체 정리 검토.
 */
public class ActivityDto {

  private ActivityDto() {}

  // ===== Response (Service → Controller → HTTP) =====

  /** §6-1 활동 목록 항목. */
  public record ActivityResponse(
      Long id, ActivityType type, Long referenceId, Integer score, Instant createdAt) {

    public static ActivityResponse from(Activity a) {
      return new ActivityResponse(
          a.getId(), a.getType(), a.getReferenceId(), a.getScore(), a.getCreatedAt());
    }
  }

  /** §6-2 기여도 요약 응답. {@code breakdown}은 13개 {@link ActivityType} 모두 포함 (점수 0이면 0으로 표시 — 명세 그대로). */
  public record ContributionResponse(
      Long memberId,
      String name,
      ContributionPeriodType period,
      Integer totalScore,
      Map<ActivityType, Integer> breakdown) {}

  /** §6-3 기여도 랭킹 항목. {@code rank}는 1-based 순차 (Q1 정렬로 동률 자체 깨짐). */
  public record RankingItemResponse(
      int rank, Long memberId, String name, String profileImageUrl, Integer totalScore) {}

  // ===== Projection (Repository → Service) =====

  /** §6-2 type별 점수 합산 결과 (group by type). */
  public record ContributionTypeSum(ActivityType type, Long totalScore) {}

  /** §6-3 랭킹 정렬 결과 행. */
  public record RankingProjection(
      Long memberId, String name, String profileImageUrl, Long totalScore, Long activityCount) {}
}
