package com.study.profile.application.activity;

import com.study.profile.application.dto.ActivityDto.ActivityResponse;
import com.study.profile.application.dto.ActivityDto.ActivityTypeCount;
import com.study.profile.application.dto.ActivityDto.RankingItemResponse;
import com.study.profile.application.dto.ActivityDto.RankingProjection;
import com.study.profile.application.dto.ActivityDto.StatsResponse;
import com.study.profile.application.dto.PageWrapper;
import com.study.profile.domain.activity.Activity;
import com.study.profile.domain.activity.ActivityType;
import com.study.profile.domain.activity.RankingPeriod;
import com.study.profile.domain.exception.MemberNotFoundException;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.ActivityRepository;
import com.study.profile.infrastructure.MemberRepository;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 활동 도메인 서비스.
 *
 * <p>쓰기: {@link ActivityEventListener}가 외부 BC 이벤트를 받아 호출. 점수 매핑 정책은 profile 도메인의 비즈니스 룰 — 외부 BC는
 * 모름.
 *
 * <p>읽기 API (§6-1~§6-4):
 *
 * <ul>
 *   <li>{@link #getStats} — 도메인별 작성형 활동 카운트
 *   <li>{@link #getCreations} — 작성형 활동 페이징
 *   <li>{@link #getReactions} — 반응형 활동 페이징 (본인만)
 *   <li>{@link #getRanking} — 가중치 점수 랭킹 페이징
 * </ul>
 *
 * <p>점수·분류 정책 출처: {@link ActivityType}.
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
   * @param parentResourceId 댓글/답변/vote처럼 부모 리소스가 있는 type은 부모 id, 루트(글/질문 등)는 null
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void record(Long userId, ActivityType type, Long referenceId, Long parentResourceId) {
    Member member = findMemberByUserId(userId);
    try {
      activityRepository.save(
          Activity.create(member, type, referenceId, parentResourceId, type.score()));
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

  /** 받은 좋아요({@code *_like_received}) 활동 기록. actorId로 누가 누른 좋아요인지 식별. */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void recordReceived(
      Long ownerId, ActivityType type, Long referenceId, Long parentResourceId, Long actorId) {
    Member member = findMemberByUserId(ownerId);
    try {
      activityRepository.save(
          Activity.createReceived(
              member, type, referenceId, parentResourceId, actorId, type.score()));
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

  /** 누른 좋아요({@code *_like}) 차감. */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void revokeLike(ActivityType type, Long referenceId, Long userId) {
    activityRepository.deleteByTypeAndReferenceIdAndUserId(type, referenceId, userId);
  }

  /** 받은 좋아요({@code *_like_received}) 차감 — actorId로 정확히 1건만 식별. */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void revokeLikeReceived(ActivityType type, Long referenceId, Long ownerId, Long actorId) {
    activityRepository.deleteByTypeAndReferenceIdAndOwnerIdAndActorId(
        type, referenceId, ownerId, actorId);
  }

  // ===== 읽기: 조회 API =====

  /**
   * §6-1 멤버 활동 통계 — 도메인(BLOG/QNA/SESSION)별 작성형 활동 누적 개수.
   *
   * <p>반응형(좋아요·댓글)은 제외 — 본인이 글로 기여한 양을 카운트. 가중치 없이 단순 카운트. 누구나 조회 가능.
   *
   * @throws MemberNotFoundException memberId 미존재 → 404
   */
  @Transactional(readOnly = true)
  public StatsResponse getStats(Long memberId) {
    requireMemberExists(memberId);

    List<ActivityTypeCount> rows = activityRepository.countByMemberIdGroupByType(memberId);

    EnumMap<ActivityType.Domain, Long> byDomain = new EnumMap<>(ActivityType.Domain.class);
    for (ActivityType.Domain d : ActivityType.Domain.values()) {
      byDomain.put(d, 0L);
    }
    for (ActivityTypeCount row : rows) {
      if (row.type().kind() != ActivityType.Kind.CREATION) {
        continue;
      }
      long c = row.count() == null ? 0L : row.count();
      byDomain.merge(row.type().domain(), c, Long::sum);
    }

    long blog = byDomain.get(ActivityType.Domain.BLOG);
    long qna = byDomain.get(ActivityType.Domain.QNA);
    long session = byDomain.get(ActivityType.Domain.SESSION);
    return new StatsResponse(memberId, blog, qna, session);
  }

  /**
   * §6-2 멤버 작성형 활동 목록 — 페이징. 누구나 조회 가능. 정렬은 {@code createdAt DESC} 고정.
   *
   * @throws MemberNotFoundException memberId 미존재 → 404
   */
  @Transactional(readOnly = true)
  public PageWrapper<ActivityResponse> getCreations(Long memberId, int page, int size) {
    requireMemberExists(memberId);
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Activity> result =
        activityRepository.findByMember_IdAndTypeIn(
            memberId, ActivityType.creationTypes(), pageable);
    return PageWrapper.from(result.map(ActivityResponse::from));
  }

  /**
   * §6-3 본인 반응형 활동 목록 — 페이징. 본인 전용 (토큰의 userId로 식별). 정렬은 {@code createdAt DESC} 고정.
   *
   * @throws MemberNotFoundException userId에 매핑된 Member 없음 → 404
   */
  @Transactional(readOnly = true)
  public PageWrapper<ActivityResponse> getReactions(Long viewerUserId, int page, int size) {
    Long memberId =
        memberRepository
            .findByUserId(viewerUserId)
            .map(Member::getId)
            .orElseThrow(() -> new MemberNotFoundException(viewerUserId));
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Activity> result =
        activityRepository.findByMember_IdAndTypeIn(
            memberId, ActivityType.reactionTypes(), pageable);
    return PageWrapper.from(result.map(ActivityResponse::from));
  }

  /**
   * §6-4 기여도 랭킹 — 가중치 점수 합산 기준. 모든 멤버(활동 0 포함) × 기간/기수 필터.
   *
   * <p>정렬: score desc → activityCount desc → member.id asc. rank는 1-based, 페이지 오프셋 반영.
   */
  @Transactional(readOnly = true)
  public PageWrapper<RankingItemResponse> getRanking(
      RankingPeriod period, Integer generationId, int page, int size) {
    Instant since = period.toStartInstant().orElse(null);
    PageRequest pageRequest = PageRequest.of(page, size);
    List<RankingProjection> rows =
        activityRepository.findRanking(since, generationId, pageRequest);
    long total = activityRepository.countRankingMembers(generationId);

    int rankOffset = page * size;
    List<RankingItemResponse> items =
        IntStream.range(0, rows.size())
            .mapToObj(
                i -> {
                  RankingProjection r = rows.get(i);
                  return new RankingItemResponse(
                      rankOffset + i + 1,
                      r.memberId(),
                      r.name(),
                      r.profileImageUrl(),
                      r.totalScore() == null ? 0 : r.totalScore().intValue());
                })
            .toList();

    return PageWrapper.from(new PageImpl<>(items, pageRequest, total));
  }

  // ===== 내부 helper =====

  private void requireMemberExists(Long memberId) {
    if (!memberRepository.existsById(memberId)) {
      throw new MemberNotFoundException(memberId);
    }
  }

  private Member findMemberByUserId(Long userId) {
    return memberRepository
        .findByUserId(userId)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "userId=" + userId + " 에 해당하는 Member 없음. profile-init 미완료 가능."));
  }
}
