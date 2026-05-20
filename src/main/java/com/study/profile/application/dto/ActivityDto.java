package com.study.profile.application.dto;

import com.study.profile.domain.activity.Activity;
import com.study.profile.domain.activity.ActivityType;
import java.time.Instant;

/** 활동 도메인 DTO 모음 — 응답(Response) + Repository projection. */
public class ActivityDto {

  private ActivityDto() {}

  // ===== Response =====

  /**
   * §6-2 activities / §6-3 reactions 항목.
   *
   * <p>{@code link}는 클라이언트가 클릭 시 라우팅할 frontend path — 백엔드에서 type별 매핑. {@code session_speak}는 page
   * 미정으로 null.
   */
  public record ActivityResponse(
      Long id, ActivityType type, Integer score, Instant createdAt, String link) {

    public static ActivityResponse from(Activity a) {
      return new ActivityResponse(
          a.getId(),
          a.getType(),
          a.getScore(),
          a.getCreatedAt(),
          a.getType().linkPath(a.getReferenceId(), a.getParentResourceId()));
    }
  }

  /**
   * §6-1 멤버 활동 통계 — 도메인별 작성형 활동 누적 개수.
   *
   * <p>가중치 없이 단순 카운트 (반응형 제외). 멤버 프로필 페이지 카드용.
   */
  public record StatsResponse(Long memberId, long blog, long qna, long session) {}

  /** §6-4 ranking 항목. {@code rank}는 1-based 순차, 페이지 오프셋 반영. */
  public record RankingItemResponse(
      int rank, Long memberId, String name, String profileImageUrl, Integer totalScore) {}

  // ===== Projection (Repository → Service) =====

  /** §6-1 stats용 — type별 활동 개수 (group by type). */
  public record ActivityTypeCount(ActivityType type, Long count) {}

  /** §6-4 랭킹 정렬 결과 행. */
  public record RankingProjection(
      Long memberId, String name, String profileImageUrl, Long totalScore, Long activityCount) {}
}
