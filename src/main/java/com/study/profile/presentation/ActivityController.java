package com.study.profile.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.profile.application.activity.ActivityService;
import com.study.profile.application.dto.ActivityDto.ActivityResponse;
import com.study.profile.application.dto.ActivityDto.ContributionResponse;
import com.study.profile.application.dto.ActivityDto.RankingItemResponse;
import com.study.profile.application.dto.PageWrapper;
import com.study.profile.domain.activity.ContributionPeriodType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 활동 조회 API — §6-1 활동 목록 / §6-2 기여도 요약 / §6-3 기여도 랭킹.
 *
 * <p>§6-1은 토큰 본인/타인에 따라 노출 분기: 본인이면 모든 활동, 타인이면 작성형만. §6-2/6-3은 점수 통계라 분기 X.
 *
 * <p>정렬: {@link PageableDefault}로 {@code createdAt DESC} 기본. 클라가 {@code ?sort=}로 override 가능.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ActivityController {

  private final ActivityService activityService;

  // ===== §6-1 멤버 활동 목록 =====

  @GetMapping("/members/{memberId}/activities")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<PageWrapper<ActivityResponse>> getActivities(
      @PathVariable Long memberId,
      @CurrentUser CustomUserDetails user,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(activityService.getActivities(memberId, user.userId(), pageable));
  }

  // ===== §6-2 멤버 기여도 요약 =====

  @GetMapping("/members/{memberId}/contributions")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<ContributionResponse> getContributions(
      @PathVariable Long memberId,
      @RequestParam(defaultValue = "all") ContributionPeriodType period) {
    return ResponseEntity.ok(activityService.getContributions(memberId, period));
  }

  // ===== §6-3 기여도 랭킹 =====

  @GetMapping("/contributions/ranking")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<List<RankingItemResponse>> getRanking(
      @RequestParam(defaultValue = "all") ContributionPeriodType period,
      @RequestParam(required = false) Long generationId,
      @RequestParam(defaultValue = "10") int limit) {
    return ResponseEntity.ok(activityService.getRanking(period, generationId, limit));
  }
}
