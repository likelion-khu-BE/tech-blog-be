package com.study.profile.infrastructure;

import com.study.profile.domain.activity.Activity;
import com.study.profile.domain.activity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** Activity 엔티티 Repository — 활동 이력 저장. */
public interface ActivityRepository extends JpaRepository<Activity, Long> {

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
}
