package com.study.profile.application.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import com.study.profile.application.dto.ActivityDto.ContributionResponse;
import com.study.profile.application.dto.ActivityDto.ContributionTypeSum;
import com.study.profile.application.dto.ActivityDto.RankingItemResponse;
import com.study.profile.application.dto.ActivityDto.RankingProjection;
import com.study.profile.domain.activity.Activity;
import com.study.profile.domain.activity.ActivityType;
import com.study.profile.domain.activity.ContributionPeriodType;
import com.study.profile.domain.exception.MemberNotFoundException;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.ActivityRepository;
import com.study.profile.infrastructure.MemberRepository;
import java.util.List;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

  @Nested
  @DisplayName("§6-1 활동 목록 조회 (getActivities)")
  class GetActivities {

    @Test
    @DisplayName("본인 호출 (memberId == viewer Member.id) → 모든 type 페이징 반환")
    void getActivities_owner_returnsAllTypes() {
      Long memberId = 1L;
      Long viewerUserId = 7L;
      Pageable pageable = PageRequest.of(0, 20);

      Member viewer = org.mockito.Mockito.mock(Member.class);
      given(viewer.getId()).willReturn(memberId);

      given(memberRepository.existsById(memberId)).willReturn(true);
      given(memberRepository.findByUserId(viewerUserId)).willReturn(Optional.of(viewer));
      given(activityRepository.findByMember_Id(eq(memberId), eq(pageable)))
          .willReturn(new PageImpl<>(List.of(), pageable, 0));

      activityService.getActivities(memberId, viewerUserId, pageable);

      // findByMember_Id 호출됨 (모든 type)
      org.mockito.Mockito.verify(activityRepository).findByMember_Id(memberId, pageable);
      org.mockito.Mockito.verify(activityRepository, org.mockito.Mockito.never())
          .findByMember_IdAndTypeIn(any(), any(), any());
    }

    @Test
    @DisplayName("타인 호출 (memberId != viewer Member.id) → 작성형 type만 필터해 반환")
    void getActivities_other_returnsCreationOnly() {
      Long memberId = 1L;
      Long viewerUserId = 99L;
      Pageable pageable = PageRequest.of(0, 20);

      Member viewer = org.mockito.Mockito.mock(Member.class);
      given(viewer.getId()).willReturn(2L); // viewer.Member.id != memberId

      given(memberRepository.existsById(memberId)).willReturn(true);
      given(memberRepository.findByUserId(viewerUserId)).willReturn(Optional.of(viewer));
      given(
              activityRepository.findByMember_IdAndTypeIn(
                  eq(memberId), eq(ActivityType.creationTypes()), eq(pageable)))
          .willReturn(new PageImpl<>(List.of(), pageable, 0));

      activityService.getActivities(memberId, viewerUserId, pageable);

      // findByMember_IdAndTypeIn 호출됨 (작성형만)
      org.mockito.Mockito.verify(activityRepository)
          .findByMember_IdAndTypeIn(memberId, ActivityType.creationTypes(), pageable);
      org.mockito.Mockito.verify(activityRepository, org.mockito.Mockito.never())
          .findByMember_Id(any(), any());
    }

    @Test
    @DisplayName("viewer가 profile-init 미완료 (userId의 Member 없음) → 타인 취급, 작성형만 반환")
    void getActivities_viewerProfileNotInitialized_treatedAsOther() {
      Long memberId = 1L;
      Long viewerUserId = 99L;
      Pageable pageable = PageRequest.of(0, 20);

      given(memberRepository.existsById(memberId)).willReturn(true);
      given(memberRepository.findByUserId(viewerUserId)).willReturn(Optional.empty());
      given(activityRepository.findByMember_IdAndTypeIn(eq(memberId), any(), eq(pageable)))
          .willReturn(new PageImpl<>(List.of(), pageable, 0));

      activityService.getActivities(memberId, viewerUserId, pageable);

      org.mockito.Mockito.verify(activityRepository)
          .findByMember_IdAndTypeIn(memberId, ActivityType.creationTypes(), pageable);
    }

    @Test
    @DisplayName("memberId 없음 → MemberNotFoundException (404)")
    void getActivities_memberNotFound() {
      Long memberId = 999L;
      given(memberRepository.existsById(memberId)).willReturn(false);

      assertThatThrownBy(() -> activityService.getActivities(memberId, 1L, PageRequest.of(0, 20)))
          .isInstanceOf(MemberNotFoundException.class)
          .hasMessageContaining("999");
    }
  }

  @Nested
  @DisplayName("§6-2 기여도 요약 (getContributions)")
  class GetContributions {

    @Test
    @DisplayName("breakdown 13개 type 모두 포함 (점수 0이면 0), totalScore는 합")
    void getContributions_breakdownAllTypes() {
      Long memberId = 1L;
      Member member = org.mockito.Mockito.mock(Member.class);
      given(member.getName()).willReturn("홍길동");
      given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

      List<ContributionTypeSum> rows =
          List.of(
              new ContributionTypeSum(ActivityType.blog_post, 60L),
              new ContributionTypeSum(ActivityType.qna_accepted, 25L));
      given(activityRepository.sumScoresByMemberIdGroupByType(eq(memberId), any()))
          .willReturn(rows);

      ContributionResponse result =
          activityService.getContributions(memberId, ContributionPeriodType.all);

      assertThat(result.name()).isEqualTo("홍길동");
      assertThat(result.breakdown()).hasSize(ActivityType.values().length);
      assertThat(result.breakdown().get(ActivityType.blog_post)).isEqualTo(60);
      assertThat(result.breakdown().get(ActivityType.qna_accepted)).isEqualTo(25);
      assertThat(result.breakdown().get(ActivityType.blog_comment)).isZero();
      assertThat(result.totalScore()).isEqualTo(85);
    }

    @Test
    @DisplayName("memberId 없음 → MemberNotFoundException (404)")
    void getContributions_memberNotFound() {
      Long memberId = 999L;
      given(memberRepository.findById(memberId)).willReturn(Optional.empty());

      assertThatThrownBy(
              () -> activityService.getContributions(memberId, ContributionPeriodType.month))
          .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("period=all → since=null로 Repository에 전달")
    void getContributions_periodAll_passesNullSince() {
      Long memberId = 1L;
      given(memberRepository.findById(memberId))
          .willReturn(Optional.of(org.mockito.Mockito.mock(Member.class)));
      given(activityRepository.sumScoresByMemberIdGroupByType(memberId, null))
          .willReturn(List.of());

      ContributionResponse result =
          activityService.getContributions(memberId, ContributionPeriodType.all);

      assertThat(result.totalScore()).isZero();
    }
  }

  @Nested
  @DisplayName("§6-3 기여도 랭킹 (getRanking)")
  class GetRanking {

    @Test
    @DisplayName("rank 1-based 순차 (Q1 정렬로 동률 깨짐), 0점 멤버도 포함")
    void getRanking_rankSequential_zeroIncluded() {
      List<RankingProjection> rows =
          List.of(
              new RankingProjection(1L, "홍길동", "url1", 120L, 5L),
              new RankingProjection(3L, "김지수", "url3", 95L, 4L),
              new RankingProjection(5L, "이몽룡", "url5", 0L, 0L));

      given(activityRepository.findRanking(eq(null), eq(null), any())).willReturn(rows);

      List<RankingItemResponse> result =
          activityService.getRanking(ContributionPeriodType.all, null, 10);

      assertThat(result).hasSize(3);
      assertThat(result.get(0).rank()).isEqualTo(1);
      assertThat(result.get(0).totalScore()).isEqualTo(120);
      assertThat(result.get(1).rank()).isEqualTo(2);
      assertThat(result.get(2).rank()).isEqualTo(3);
      assertThat(result.get(2).totalScore()).isZero();
    }

    @Test
    @DisplayName("generationId 필터 — Repository에 그대로 전달")
    void getRanking_withGenerationId() {
      given(activityRepository.findRanking(any(), eq(13L), any())).willReturn(List.of());

      List<RankingItemResponse> result =
          activityService.getRanking(ContributionPeriodType.year, 13L, 10);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("totalScore가 null인 row → 0 (null safe)")
    void getRanking_nullTotalScoreHandled() {
      List<RankingProjection> rows = List.of(new RankingProjection(1L, "홍길동", "url1", null, 0L));
      given(activityRepository.findRanking(any(), any(), any())).willReturn(rows);

      List<RankingItemResponse> result =
          activityService.getRanking(ContributionPeriodType.all, null, 10);

      assertThat(result.get(0).totalScore()).isZero();
    }
  }

  // ===== helpers =====

  private Member mockMember(Long userId) {
    Member member = org.mockito.Mockito.mock(Member.class);
    return member;
  }
}
