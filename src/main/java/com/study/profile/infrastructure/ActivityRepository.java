package com.study.profile.infrastructure;

import com.study.profile.application.dto.ActivityDto.ActivityTypeCount;
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

/** Activity 엔티티 Repository — 이벤트로 기록된 활동 이력의 저장 + 조회. */
public interface ActivityRepository extends JpaRepository<Activity, Long> {

  // ===== 차감 (cascade) — 활동 발행자에서 호출 =====

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

  /**
   * §6-2 activities / §6-3 reactions — 멤버의 type 집합 필터링 페이징.
   *
   * <p>호출 측에서 {@link ActivityType#creationTypes()} 또는 {@link ActivityType#reactionTypes()} 전달.
   */
  Page<Activity> findByMember_IdAndTypeIn(
      Long memberId, Collection<ActivityType> types, Pageable pageable);

  /** §6-1 stats — 멤버의 type별 활동 개수. 결과 없는 type은 row X (호출 측에서 0 처리). */
  @Query(
      "select new com.study.profile.application.dto.ActivityDto$ActivityTypeCount("
          + "  a.type, count(a)) "
          + "from Activity a "
          + "where a.member.id = :memberId "
          + "group by a.type")
  List<ActivityTypeCount> countByMemberIdGroupByType(Long memberId);

  /**
   * §6-4 ranking — 모든 멤버(활동 0 포함, LEFT JOIN) × 기간/기수 필터. 정렬: score desc → activityCount desc →
   * member.id asc (Q1 동률 처리).
   */
  @Query(
      "select new com.study.profile.application.dto.ActivityDto$RankingProjection("
          + "  m.id, m.name, m.profileImageUrl, "
          + "  coalesce(sum(a.score), 0), "
          + "  coalesce(count(a), 0)) "
          + "from Member m "
          + "left join Activity a on a.member = m "
          + "  and (cast(:since as java.time.Instant) is null or a.createdAt >= :since) "
          + "where cast(:generationId as java.lang.Integer) is null or exists ("
          + "  select 1 from MemberGeneration mg "
          + "  where mg.member = m and mg.generation.number = :generationId) "
          + "group by m.id, m.name, m.profileImageUrl "
          + "order by coalesce(sum(a.score), 0) desc, "
          + "         coalesce(count(a), 0) desc, "
          + "         m.id asc")
  List<RankingProjection> findRanking(
      @Nullable Instant since, @Nullable Integer generationId, Pageable pageable);

  /** §6-4 ranking totalElements용 — 기수 필터만 적용한 멤버 총 수. */
  @Query(
      "select count(m) from Member m "
          + "where cast(:generationId as java.lang.Integer) is null or exists ("
          + "  select 1 from MemberGeneration mg "
          + "  where mg.member = m and mg.generation.number = :generationId)")
  long countRankingMembers(@Nullable Integer generationId);
}
