package com.study.profile.application;

import com.study.profile.domain.activity.Activity;
import com.study.profile.domain.activity.ActivityType;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.ActivityRepository;
import com.study.profile.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 활동 기록 내부 서비스.
 *
 * <p>{@link ActivityEventListener}가 외부 BC 이벤트를 받아 호출. profile 내부 전용 — 외부 BC가 직접 호출 X (이벤트 발행으로만
 * 통신).
 *
 * <p>책임: ActivityType별 점수 매핑 + Member 매핑(userId → Member) + Activity 행 저장.
 *
 * <p>트랜잭션: 호출하는 listener의 publisher 트랜잭션에 자동 참여 (Spring {@code @EventListener} 기본 동작).
 */
@Service
@RequiredArgsConstructor
public class ActivityRecorder {

  private final MemberRepository memberRepository;
  private final ActivityRepository activityRepository;

  /**
   * 활동 한 건 기록.
   *
   * @param userId 활동 주체 (auth.User.id)
   * @param type 활동 종류
   * @param referenceId 활동 대상 식별자 (post.id, comment.id 등)
   * @throws IllegalStateException Member 없음 (profile-init 미완료) — publisher 트랜잭션과 함께 롤백
   */
  public void record(Long userId, ActivityType type, Long referenceId) {
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "userId=" + userId + " 에 해당하는 Member 없음. profile-init 미완료 가능."));

    int score = scoreOf(type);
    Activity activity = Activity.create(member, type, referenceId, score);
    activityRepository.save(activity);
  }

  /**
   * ActivityType별 표준 점수 매핑.
   *
   * <p>점수 정책은 profile 도메인의 비즈니스 룰 — 외부 BC는 모름. 정책 변경 시 이 메서드만 수정.
   */
  private int scoreOf(ActivityType type) {
    return switch (type) {
      case blog_post, qna_accepted, session_post -> 10;
      case blog_comment, qna_question, qna_answer, session_comment -> 5;
      case other -> 0; // 현재 미사용 — 미래에 새 활동 등장 시 새 ActivityType 추가
    };
  }
}
