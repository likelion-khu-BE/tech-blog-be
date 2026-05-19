package com.study.profile.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.profile.application.activity.ActivityService;
import com.study.profile.application.dto.ActivityDto.ActivityResponse;
import com.study.profile.application.dto.ActivityDto.RankingItemResponse;
import com.study.profile.application.dto.ActivityDto.StatsResponse;
import com.study.profile.application.dto.PageWrapper;
import com.study.profile.domain.activity.RankingPeriod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 활동 read API.
 *
 * <ul>
 *   <li>§6-1 GET /api/profile/members/{memberId}/stats — 도메인별 작성형 카운트
 *   <li>§6-2 GET /api/profile/members/{memberId}/activities — 작성형 활동 목록 (페이징)
 *   <li>§6-3 GET /api/profile/members/me/reactions — 반응형 활동 목록 (본인만, 페이징)
 *   <li>§6-4 GET /api/profile/ranking — 가중치 점수 랭킹 (페이징)
 * </ul>
 */
@Tag(name = "활동 프로필", description = "활동 통계 / 작성형·반응형 활동 목록 / 랭킹 API")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ActivityController {

  private final ActivityService activityService;

  @Operation(summary = "멤버 활동 통계 조회", description = "도메인(BLOG/QNA/SESSION)별 작성형 활동 누적 개수.")
  @GetMapping("/members/{memberId}/stats")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<StatsResponse> getStats(@PathVariable Long memberId) {
    return ResponseEntity.ok(activityService.getStats(memberId));
  }

  @Operation(summary = "멤버 작성형 활동 목록 조회", description = "글·답변·발표 등 작성형 활동 페이징. createdAt DESC 고정.")
  @GetMapping("/members/{memberId}/activities")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<PageWrapper<ActivityResponse>> getCreations(
      @PathVariable Long memberId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(activityService.getCreations(memberId, page, size));
  }

  @Operation(summary = "내 반응형 활동 목록 조회", description = "좋아요·투표·댓글 등 본인 반응형 활동. 토큰 기반, 타인 비노출.")
  @GetMapping("/members/me/reactions")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<PageWrapper<ActivityResponse>> getReactions(
      @CurrentUser CustomUserDetails user,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(activityService.getReactions(user.userId(), page, size));
  }

  @Operation(summary = "기여도 랭킹 조회", description = "가중치 점수 기준 랭킹 페이징. 기간/기수 필터 지원.")
  @GetMapping("/ranking")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<PageWrapper<RankingItemResponse>> getRanking(
      @Parameter(
              description = "집계 기간",
              required = true,
              schema = @Schema(implementation = RankingPeriod.class, defaultValue = "month"))
          @RequestParam(defaultValue = "month")
          RankingPeriod period,
      @RequestParam(required = false) Integer generationId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(activityService.getRanking(period, generationId, page, size));
  }
}
