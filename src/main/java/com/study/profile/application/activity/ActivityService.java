package com.study.profile.application.activity;

import com.study.profile.domain.activity.Activity;
import com.study.profile.domain.activity.ActivityType;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.ActivityRepository;
import com.study.profile.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

  // ===== 읽기: 조회 API (후속 PR) =====

  // TODO: 후속 PR에서 추가
  // - getActivities(memberId, type, pageable)
  // - getContributions(memberId, period)
  // - getRanking(period, generationId, limit)

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
