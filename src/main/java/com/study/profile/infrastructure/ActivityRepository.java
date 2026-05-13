package com.study.profile.infrastructure;

import com.study.profile.application.dto.ActivityDto.ContributionTypeSum;
import com.study.profile.application.dto.ActivityDto.RankingProjection;
import com.study.profile.domain.activity.Activity;
import com.study.profile.domain.activity.ActivityType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

/** Activity 엔티티 Repository — 활동 이력 저장 + 조회. */
public interface ActivityRepository extends JpaRepository<Activity, Long> {

  // ===== 차감 (cascade) =====

  /** type + referenceId 매칭 row 삭제. 일반 cascade 차감(글/댓글/답변/채택/발표 등)에 사용. */
  @Modifying
  @Query("delete from Activity a where a.type = :type and a.referenceId = :referenceId")
  int deleteByTypeAndReferenceId(ActivityType type, Long referenceId);

  /** type + referenceId + member.user.id 매칭 row 삭제. 누른 좋아요({@code *_like}) 차감에 사용. */
  @Modifying
  @Query(
      "delete from Activity a "
          + "where a.type = :type "
          + "and a.referenceId = :referenceId "
          + "and a.member.user.id = :userId")
  int deleteByTypeAndReferenceIdAndUserId(ActivityType type, Long referenceId, Long userId);

  /**
   * type + referenceId + member.user.id + actorId 매칭 row 삭제. 받은 좋아요({@code *_like_received}) 차감에 사용
   * — 누가 누른 좋아요로 인한 row인지 식별해 정확히 1건만 삭제.
   */
  @Modifying
  @Query(
      "delete from Activity a "
          + "where a.type = :type "
          + "and a.referenceId = :referenceId "
          + "and a.member.user.id = :ownerId "
          + "and a.actorId = :actorId")
  int deleteByTypeAndReferenceIdAndOwnerIdAndActorId(
      ActivityType type, Long referenceId, Long ownerId, Long actorId);

  // ===== 조회 =====

  /** §6-1 활동 목록 (본인 호출용) — 멤버의 모든 type 활동을 페이징. */
  Page<Activity> findByMember_Id(Long memberId, Pageable pageable);

  /** §6-1 활동 목록 (타인 호출용) — 작성형 type만 페이징. 호출 측에서 {@link ActivityType#creationTypes()} 전달. */
  Page<Activity> findByMember_IdAndTypeIn(
      Long memberId, Collection<ActivityType> types, Pageable pageable);

  /** §6-2 기여도 — 멤버의 type별 점수 합산. {@code since=null}이면 전체 기간. 결과 없는 type은 row X (호출 측에서 0으로 채움). */
  @Query(
      "select new com.study.profile.application.dto.ActivityDto$ContributionTypeSum("
          + "  a.type, sum(a.score)) "
          + "from Activity a "
          + "where a.member.id = :memberId "
          + "and (:since is null or a.createdAt >= :since) "
          + "group by a.type")
  List<ContributionTypeSum> sumScoresByMemberIdGroupByType(Long memberId, @Nullable Instant since);

  /**
   * §6-3 기여도 랭킹 — 모든 멤버(활동 0인 멤버 포함, LEFT JOIN) × 기간/기수 필터. 정렬: score desc → activityCount desc →
   * member.id asc (Q1 동률 처리).
   */
  @Query(
      "select new com.study.profile.application.dto.ActivityDto$RankingProjection("
          + "  m.id, m.name, m.profileImageUrl, "
          + "  coalesce(sum(a.score), 0), "
          + "  coalesce(count(a), 0)) "
          + "from Member m "
          + "left join Activity a on a.member = m "
          + "  and (:since is null or a.createdAt >= :since) "
          + "where :generationId is null or exists ("
          + "  select 1 from MemberGeneration mg "
          + "  where mg.member = m and mg.generation.id = :generationId) "
          + "group by m.id, m.name, m.profileImageUrl "
          + "order by coalesce(sum(a.score), 0) desc, "
          + "         coalesce(count(a), 0) desc, "
          + "         m.id asc")
  List<RankingProjection> findRanking(
      @Nullable Instant since, @Nullable Long generationId, Pageable pageable);
}
