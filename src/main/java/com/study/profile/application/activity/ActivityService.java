package com.study.profile.application.activity;

import com.study.profile.application.dto.ActivityDto.ActivityResponse;
import com.study.profile.application.dto.ActivityDto.ContributionResponse;
import com.study.profile.application.dto.ActivityDto.ContributionTypeSum;
import com.study.profile.application.dto.ActivityDto.RankingItemResponse;
import com.study.profile.application.dto.ActivityDto.RankingProjection;
import com.study.profile.application.dto.PageWrapper;
import com.study.profile.domain.activity.Activity;
import com.study.profile.domain.activity.ActivityType;
import com.study.profile.domain.activity.ContributionPeriodType;
import com.study.profile.domain.exception.MemberNotFoundException;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.ActivityRepository;
import com.study.profile.infrastructure.MemberRepository;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 활동 도메인 서비스.
 *
 * <p>쓰기: {@link ActivityEventListener}가 외부 BC 이벤트를 받아 호출. 점수 매핑 정책은 profile 도메인의 비즈니스 룰 — 외부 BC는
 * 모름.
 *
 * <p>읽기: 조회 API용 메서드는 후속 PR에서 추가.
 *
 * <p>점수 정책 출처: {@code docs/profile/profile-api.md} "점수 기준" 표.
 */
@Service
@RequiredArgsConstructor
public class ActivityService {

  private final MemberRepository memberRepository;
  private final ActivityRepository activityRepository;

  // ===== 쓰기: 활동 기록 =====

  /**
   * 자기 행위 활동 기록 — 글 작성, 댓글, 좋아요 누르기, 채택, 발표 등.
   *
   * <p>발행자 트랜잭션 commit 후 비동기 listener에서 호출되는 흐름이라 새 트랜잭션으로 분리.
   *
   * @throws IllegalStateException Member 없음 (profile-init 미완료 가능)
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void record(Long userId, ActivityType type, Long referenceId) {
    Member member = findMember(userId);
    try {
      activityRepository.save(Activity.create(member, type, referenceId, type.score()));
    } catch (DataIntegrityViolationException e) {
      throw new IllegalStateException(
          "Publisher 1:1 보장 깨짐 — 중복 활동 시도. userId="
              + userId
              + ", type="
              + type
              + ", referenceId="
              + referenceId,
          e);
    }
  }

  /**
   * 받은 좋아요({@code *_like_received}) 활동 기록. 같은 글에서 여러 명의 좋아요를 받을 수 있어 actorId로 식별.
   *
   * @throws IllegalStateException Member 없음 / Publisher 1:1 보장 깨짐 (중복 활동)
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void recordReceived(Long ownerId, ActivityType type, Long referenceId, Long actorId) {
    Member member = findMember(ownerId);
    try {
      activityRepository.save(
          Activity.createReceived(member, type, referenceId, actorId, type.score()));
    } catch (DataIntegrityViolationException e) {
      throw new IllegalStateException(
          "Publisher 1:1 보장 깨짐 — 중복 활동 시도. ownerId="
              + ownerId
              + ", type="
              + type
              + ", referenceId="
              + referenceId
              + ", actorId="
              + actorId,
          e);
    }
  }

  // ===== 쓰기: 활동 차감 (cascade) =====

  /** type + referenceId 매칭 row 일괄 차감. 일반 cascade 차감(글/댓글/답변/채택/발표 등)에 사용. */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void revoke(ActivityType type, Long referenceId) {
    activityRepository.deleteByTypeAndReferenceId(type, referenceId);
  }

  /** type + referenceId + userId 매칭 row 차감. 누른 좋아요({@code *_like}) 차감에 사용. */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void revokeLike(ActivityType type, Long referenceId, Long userId) {
    activityRepository.deleteByTypeAndReferenceIdAndUserId(type, referenceId, userId);
  }

  /**
   * type + referenceId + ownerId + actorId 매칭 row 차감. 받은 좋아요({@code *_like_received}) 차감에 사용. 누가 누른
   * 좋아요로 인한 row인지 식별해 정확히 1건만 삭제.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void revokeLikeReceived(ActivityType type, Long referenceId, Long ownerId, Long actorId) {
    activityRepository.deleteByTypeAndReferenceIdAndOwnerIdAndActorId(
        type, referenceId, ownerId, actorId);
  }

  // ===== 읽기: 조회 API =====

  /**
   * §6-1 멤버 활동 목록 — 호출자 본인/타인에 따라 노출 분기.
   *
   * <p>본인 호출(토큰의 Member.id == path memberId): 모든 type 노출 (작성형 + 반응형).
   *
   * <p>타인 호출: 작성형(creation) type만 노출. 반응형(좋아요/댓글)은 UI상 "기타 활동" 영역 자체 안 보임.
   *
   * @throws MemberNotFoundException memberId가 존재하지 않을 때 → 404
   */
  @Transactional(readOnly = true)
  public PageWrapper<ActivityResponse> getActivities(
      Long memberId, Long viewerUserId, Pageable pageable) {
    if (!memberRepository.existsById(memberId)) {
      throw new MemberNotFoundException(memberId);
    }

    Long viewerMemberId =
        memberRepository.findByUserId(viewerUserId).map(Member::getId).orElse(null);
    boolean isOwner = memberId.equals(viewerMemberId);

    Page<Activity> page =
        isOwner
            ? activityRepository.findByMember_Id(memberId, pageable)
            : activityRepository.findByMember_IdAndTypeIn(
                memberId, ActivityType.creationTypes(), pageable);

    return PageWrapper.from(page.map(ActivityResponse::from));
  }

  /**
   * §6-2 기여도 요약 — 기간 내 type별 점수 합산 + 전체 합. 응답 breakdown은 13개 {@link ActivityType} 모두 포함 (점수 0이면 0).
   *
   * @throws MemberNotFoundException memberId가 존재하지 않을 때 → 404
   */
  @Transactional(readOnly = true)
  public ContributionResponse getContributions(Long memberId, ContributionPeriodType period) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(memberId));

    Instant since = period.toStartInstant().orElse(null);
    List<ContributionTypeSum> rows =
        activityRepository.sumScoresByMemberIdGroupByType(memberId, since);

    Map<ActivityType, Integer> breakdown = new EnumMap<>(ActivityType.class);
    for (ActivityType type : ActivityType.values()) {
      breakdown.put(type, 0);
    }
    for (ContributionTypeSum row : rows) {
      breakdown.put(row.type(), row.totalScore() == null ? 0 : row.totalScore().intValue());
    }
    int totalScore = breakdown.values().stream().mapToInt(Integer::intValue).sum();

    return new ContributionResponse(memberId, member.getName(), period, totalScore, breakdown);
  }

  /**
   * §6-3 기여도 랭킹 — 모든 멤버 (활동 0인 멤버 포함). 정렬: score desc → activityCount desc → member.id asc.
   *
   * @param period 기간 필터 (null 허용 X — Controller에서 default 채움)
   * @param generationId 기수 필터 (null이면 전체)
   * @param limit 반환할 순위 수
   */
  @Transactional(readOnly = true)
  public List<RankingItemResponse> getRanking(
      ContributionPeriodType period, Long generationId, int limit) {
    Instant since = period.toStartInstant().orElse(null);
    List<RankingProjection> rows =
        activityRepository.findRanking(since, generationId, PageRequest.of(0, limit));

    return java.util.stream.IntStream.range(0, rows.size())
        .mapToObj(
            i -> {
              RankingProjection r = rows.get(i);
              return new RankingItemResponse(
                  i + 1,
                  r.memberId(),
                  r.name(),
                  r.profileImageUrl(),
                  r.totalScore() == null ? 0 : r.totalScore().intValue());
            })
        .toList();
  }

  // ===== 내부 helper =====

  private Member findMember(Long userId) {
    return memberRepository
        .findByUserId(userId)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "userId=" + userId + " 에 해당하는 Member 없음. profile-init 미완료 가능."));
  }
}
