package com.study.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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

/**
 * {@link ActivityRecorder} 단위 테스트 — Mock 기반.
 *
 * <p>핵심 검증: 점수 매핑 + Activity 저장 + Member 없을 때 throw.
 */
@ExtendWith(MockitoExtension.class)
class ActivityRecorderTest {

  @Mock private MemberRepository memberRepository;
  @Mock private ActivityRepository activityRepository;

  @InjectMocks private ActivityRecorder activityRecorder;

  @Nested
  @DisplayName("점수 매핑")
  class ScoreMapping {

    @Test
    @DisplayName("blog_post → 10점")
    void blogPost_10() {
      Member member = mock(Member.class);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      activityRecorder.record(1L, ActivityType.blog_post, 42L);

      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      assertThat(captor.getValue().getScore()).isEqualTo(10);
      assertThat(captor.getValue().getType()).isEqualTo(ActivityType.blog_post);
      assertThat(captor.getValue().getReferenceId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("blog_comment → 5점")
    void blogComment_5() {
      Member member = mock(Member.class);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      activityRecorder.record(1L, ActivityType.blog_comment, 42L);

      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      assertThat(captor.getValue().getScore()).isEqualTo(5);
    }

    @Test
    @DisplayName("qna_question → 5점")
    void qnaQuestion_5() {
      Member member = mock(Member.class);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      activityRecorder.record(1L, ActivityType.qna_question, 42L);

      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      assertThat(captor.getValue().getScore()).isEqualTo(5);
    }

    @Test
    @DisplayName("qna_answer → 5점")
    void qnaAnswer_5() {
      Member member = mock(Member.class);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      activityRecorder.record(1L, ActivityType.qna_answer, 42L);

      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      assertThat(captor.getValue().getScore()).isEqualTo(5);
    }

    @Test
    @DisplayName("qna_accepted → 10점")
    void qnaAccepted_10() {
      Member member = mock(Member.class);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      activityRecorder.record(1L, ActivityType.qna_accepted, 42L);

      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      assertThat(captor.getValue().getScore()).isEqualTo(10);
    }

    @Test
    @DisplayName("session_post → 10점")
    void sessionPost_10() {
      Member member = mock(Member.class);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      activityRecorder.record(1L, ActivityType.session_post, 42L);

      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      assertThat(captor.getValue().getScore()).isEqualTo(10);
    }

    @Test
    @DisplayName("session_comment → 5점")
    void sessionComment_5() {
      Member member = mock(Member.class);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      activityRecorder.record(1L, ActivityType.session_comment, 42L);

      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      assertThat(captor.getValue().getScore()).isEqualTo(5);
    }
  }

  @Nested
  @DisplayName("Member 없음 처리")
  class MemberNotFound {

    @Test
    @DisplayName("findByUserId 빈 결과 → IllegalStateException")
    void noMember_throws() {
      given(memberRepository.findByUserId(any())).willReturn(Optional.empty());

      assertThatThrownBy(() -> activityRecorder.record(999L, ActivityType.blog_post, 42L))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Member 없음");
    }
  }
}
