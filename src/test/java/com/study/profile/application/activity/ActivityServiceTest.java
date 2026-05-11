package com.study.profile.application.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import com.study.profile.domain.activity.Activity;
import com.study.profile.domain.activity.ActivityType;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.ActivityRepository;
import com.study.profile.infrastructure.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

  @Mock private MemberRepository memberRepository;

  @Mock private ActivityRepository activityRepository;

  @InjectMocks private ActivityService activityService;

  @Nested
  @DisplayName("자기 행위 활동 기록 (record)")
  class Record {

    @Test
    @DisplayName("정상 호출 → Activity 저장 + 점수는 type.score()")
    void record_success() {
      // Given
      Long userId = 1L;
      Long postId = 42L;
      Member member = mockMember(userId);
      given(memberRepository.findByUserId(userId)).willReturn(Optional.of(member));

      // When
      activityService.record(userId, ActivityType.blog_post, postId);

      // Then
      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      Activity saved = captor.getValue();
      assertThat(saved.getMember()).isSameAs(member);
      assertThat(saved.getType()).isEqualTo(ActivityType.blog_post);
      assertThat(saved.getReferenceId()).isEqualTo(postId);
      assertThat(saved.getActorId()).isNull();
      assertThat(saved.getScore()).isEqualTo(30); // blog_post = 30
    }

    @Test
    @DisplayName("Member 없음 → IllegalStateException (profile-init 미완료)")
    void record_memberNotFound() {
      // Given
      Long userId = 999L;
      given(memberRepository.findByUserId(userId)).willReturn(Optional.empty());

      // When + Then
      assertThatThrownBy(() -> activityService.record(userId, ActivityType.blog_post, 42L))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("profile-init 미완료");
    }

    @Test
    @DisplayName("UNIQUE 위반 → IllegalStateException으로 wrapping (publisher 1:1 깨짐)")
    void record_duplicateActivity() {
      // Given
      Long userId = 1L;
      Member member = mockMember(userId);
      given(memberRepository.findByUserId(userId)).willReturn(Optional.of(member));
      willThrow(new DataIntegrityViolationException("duplicate key"))
          .given(activityRepository)
          .save(any(Activity.class));

      // When + Then
      assertThatThrownBy(() -> activityService.record(userId, ActivityType.blog_post, 42L))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Publisher 1:1 보장 깨짐")
          .hasMessageContaining("userId=1")
          .hasMessageContaining("referenceId=42")
          .hasCauseInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  @DisplayName("받은 좋아요 활동 기록 (recordReceived)")
  class RecordReceived {

    @Test
    @DisplayName("정상 호출 → Activity 저장 + actor_id 매핑 + 점수 +1")
    void recordReceived_success() {
      // Given
      Long ownerId = 1L;
      Long postId = 42L;
      Long actorId = 99L;
      Member owner = mockMember(ownerId);
      given(memberRepository.findByUserId(ownerId)).willReturn(Optional.of(owner));

      // When
      activityService.recordReceived(
          ownerId, ActivityType.blog_post_like_received, postId, actorId);

      // Then
      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      Activity saved = captor.getValue();
      assertThat(saved.getMember()).isSameAs(owner);
      assertThat(saved.getType()).isEqualTo(ActivityType.blog_post_like_received);
      assertThat(saved.getReferenceId()).isEqualTo(postId);
      assertThat(saved.getActorId()).isEqualTo(actorId);
      assertThat(saved.getScore()).isEqualTo(1); // *_like_received = 1
    }

    @Test
    @DisplayName("UNIQUE 위반 (같은 actor의 중복 좋아요) → IllegalStateException + actorId 포함 메시지")
    void recordReceived_duplicateActivity() {
      // Given
      Long ownerId = 1L;
      Long actorId = 99L;
      Member owner = mockMember(ownerId);
      given(memberRepository.findByUserId(ownerId)).willReturn(Optional.of(owner));
      willThrow(new DataIntegrityViolationException("duplicate key"))
          .given(activityRepository)
          .save(any(Activity.class));

      // When + Then
      assertThatThrownBy(
              () ->
                  activityService.recordReceived(
                      ownerId, ActivityType.blog_post_like_received, 42L, actorId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Publisher 1:1 보장 깨짐")
          .hasMessageContaining("ownerId=1")
          .hasMessageContaining("actorId=99");
    }
  }

  @Nested
  @DisplayName("일반 cascade 차감 (revoke)")
  class Revoke {

    @Test
    @DisplayName("호출 → repository.deleteByTypeAndReferenceId 위임")
    void revoke_success() {
      // When
      activityService.revoke(ActivityType.blog_post, 42L);

      // Then
      verify(activityRepository).deleteByTypeAndReferenceId(ActivityType.blog_post, 42L);
    }
  }

  @Nested
  @DisplayName("누른 좋아요 차감 (revokeLike)")
  class RevokeLike {

    @Test
    @DisplayName("호출 → repository.deleteByTypeAndReferenceIdAndUserId 위임")
    void revokeLike_success() {
      // When
      activityService.revokeLike(ActivityType.blog_post_like, 42L, 99L);

      // Then
      verify(activityRepository)
          .deleteByTypeAndReferenceIdAndUserId(ActivityType.blog_post_like, 42L, 99L);
    }
  }

  @Nested
  @DisplayName("받은 좋아요 차감 (revokeLikeReceived)")
  class RevokeLikeReceived {

    @Test
    @DisplayName("호출 → repository.deleteByTypeAndReferenceIdAndOwnerIdAndActorId 위임")
    void revokeLikeReceived_success() {
      // When
      activityService.revokeLikeReceived(ActivityType.blog_post_like_received, 42L, 1L, 99L);

      // Then
      verify(activityRepository)
          .deleteByTypeAndReferenceIdAndOwnerIdAndActorId(
              ActivityType.blog_post_like_received, 42L, 1L, 99L);
    }
  }

  @Nested
  @DisplayName("점수 매핑 (ActivityType.score())")
  class ScoreMapping {

    @Test
    @DisplayName("13 type 점수 매핑 확인")
    void allTypesHaveCorrectScore() {
      assertThat(ActivityType.session_speak.score()).isEqualTo(50);
      assertThat(ActivityType.blog_post.score()).isEqualTo(30);
      assertThat(ActivityType.session_event_post.score()).isEqualTo(30);
      assertThat(ActivityType.qna_accepted.score()).isEqualTo(25);
      assertThat(ActivityType.qna_question.score()).isEqualTo(10);
      assertThat(ActivityType.qna_answer.score()).isEqualTo(10);
      assertThat(ActivityType.blog_comment.score()).isEqualTo(3);
      assertThat(ActivityType.qna_comment.score()).isEqualTo(3);
      assertThat(ActivityType.session_event_comment.score()).isEqualTo(3);
      assertThat(ActivityType.blog_post_like.score()).isEqualTo(1);
      assertThat(ActivityType.blog_post_like_received.score()).isEqualTo(1);
      assertThat(ActivityType.session_event_post_like.score()).isEqualTo(1);
      assertThat(ActivityType.session_event_post_like_received.score()).isEqualTo(1);
    }
  }

  // ===== helpers =====

  private Member mockMember(Long userId) {
    Member member = org.mockito.Mockito.mock(Member.class);
    return member;
  }
}
