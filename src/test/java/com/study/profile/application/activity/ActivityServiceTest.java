package com.study.profile.application.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

  // ===== 쓰기 =====

  @Nested
  @DisplayName("자기 행위 활동 기록 (record)")
  class Record {

    @Test
    @DisplayName("정상 호출 → Activity 저장 + 점수 + parent_resource_id 저장")
    void record_success() {
      Long userId = 1L;
      Long commentId = 42L;
      Long postId = 7L;
      Member member = Mockito.mock(Member.class);
      given(memberRepository.findByUserId(userId)).willReturn(Optional.of(member));

      activityService.record(userId, ActivityType.blog_comment, commentId, postId);

      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      Activity saved = captor.getValue();
      assertThat(saved.getMember()).isSameAs(member);
      assertThat(saved.getType()).isEqualTo(ActivityType.blog_comment);
      assertThat(saved.getReferenceId()).isEqualTo(commentId);
      assertThat(saved.getParentResourceId()).isEqualTo(postId);
      assertThat(saved.getActorId()).isNull();
      assertThat(saved.getScore()).isEqualTo(3);
    }

    @Test
    @DisplayName("root 리소스 (blog_post) → parent_resource_id null")
    void record_rootResource_nullParent() {
      Member member = Mockito.mock(Member.class);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      activityService.record(1L, ActivityType.blog_post, 42L, null);

      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      assertThat(captor.getValue().getParentResourceId()).isNull();
    }

    @Test
    @DisplayName("Member 없음 → IllegalStateException (profile-init 미완료)")
    void record_memberNotFound() {
      given(memberRepository.findByUserId(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> activityService.record(999L, ActivityType.blog_post, 42L, null))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("profile-init 미완료");
    }

    @Test
    @DisplayName("UNIQUE 위반 → IllegalStateException wrapping")
    void record_duplicateActivity() {
      Member member = Mockito.mock(Member.class);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));
      willThrow(new DataIntegrityViolationException("duplicate key"))
          .given(activityRepository)
          .save(any(Activity.class));

      assertThatThrownBy(() -> activityService.record(1L, ActivityType.blog_post, 42L, null))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Publisher 1:1 보장 깨짐")
          .hasMessageContaining("userId=1")
          .hasCauseInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  @DisplayName("받은 좋아요 활동 기록 (recordReceived)")
  class RecordReceived {

    @Test
    @DisplayName("정상 호출 → actor_id 매핑 + 점수 +1 + parent null (post like는 root)")
    void recordReceived_success() {
      Member owner = Mockito.mock(Member.class);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(owner));

      activityService.recordReceived(1L, ActivityType.blog_post_like_received, 42L, null, 99L);

      ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
      verify(activityRepository).save(captor.capture());
      Activity saved = captor.getValue();
      assertThat(saved.getActorId()).isEqualTo(99L);
      assertThat(saved.getParentResourceId()).isNull();
      assertThat(saved.getScore()).isEqualTo(1);
    }

    @Test
    @DisplayName("UNIQUE 위반 → actorId 포함 IllegalStateException")
    void recordReceived_duplicateActivity() {
      Member owner = Mockito.mock(Member.class);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(owner));
      willThrow(new DataIntegrityViolationException("duplicate"))
          .given(activityRepository)
          .save(any(Activity.class));

      assertThatThrownBy(
              () ->
                  activityService.recordReceived(
                      1L, ActivityType.blog_post_like_received, 42L, null, 99L))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("actorId=99");
    }
  }

  @Nested
  @DisplayName("차감 (revoke/revokeLike/revokeLikeReceived)")
  class Revoke {

    @Test
    void revoke_delegatesToRepo() {
      activityService.revoke(ActivityType.blog_post, 42L);
      verify(activityRepository).deleteByTypeAndReferenceId(ActivityType.blog_post, 42L);
    }

    @Test
    void revokeLike_delegatesToRepo() {
      activityService.revokeLike(ActivityType.blog_post_like, 42L, 99L);
      verify(activityRepository)
          .deleteByTypeAndReferenceIdAndUserId(ActivityType.blog_post_like, 42L, 99L);
    }

    @Test
    void revokeLikeReceived_delegatesToRepo() {
      activityService.revokeLikeReceived(ActivityType.blog_post_like_received, 42L, 1L, 99L);
      verify(activityRepository)
          .deleteByTypeAndReferenceIdAndOwnerIdAndActorId(
              ActivityType.blog_post_like_received, 42L, 1L, 99L);
    }
  }

  @Nested
  @DisplayName("점수 매핑 (ActivityType.score())")
  class ScoreMapping {

    @Test
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
      assertThat(ActivityType.qna_answer_upvote.score()).isEqualTo(1);
      assertThat(ActivityType.qna_answer_downvote.score()).isEqualTo(1);
      assertThat(ActivityType.session_event_post_like.score()).isEqualTo(1);
      assertThat(ActivityType.session_event_post_like_received.score()).isEqualTo(1);
    }
  }

  // ===== 읽기 =====

  @Nested
  @DisplayName("§6-1 활동 통계 (getStats)")
  class GetStats {

    @Test
    @DisplayName("작성형만 도메인별 카운트, 반응형은 제외")
    void getStats_creationOnly_byDomain() {
      Long memberId = 1L;
      given(memberRepository.existsById(memberId)).willReturn(true);
      List<ActivityTypeCount> rows =
          List.of(
              new ActivityTypeCount(ActivityType.blog_post, 3L),
              new ActivityTypeCount(ActivityType.blog_comment, 5L), // 반응형 → 제외
              new ActivityTypeCount(ActivityType.qna_question, 2L),
              new ActivityTypeCount(ActivityType.qna_accepted, 1L),
              new ActivityTypeCount(ActivityType.qna_answer_upvote, 10L), // 반응형 → 제외
              new ActivityTypeCount(ActivityType.session_speak, 4L));
      given(activityRepository.countByMemberIdGroupByType(memberId)).willReturn(rows);

      StatsResponse result = activityService.getStats(memberId);

      assertThat(result.memberId()).isEqualTo(memberId);
      assertThat(result.blog()).isEqualTo(3L); // blog_post만
      assertThat(result.qna()).isEqualTo(3L); // qna_question + qna_accepted
      assertThat(result.session()).isEqualTo(4L); // session_speak
    }

    @Test
    @DisplayName("활동 없음 → 모두 0")
    void getStats_noActivities_allZero() {
      Long memberId = 1L;
      given(memberRepository.existsById(memberId)).willReturn(true);
      given(activityRepository.countByMemberIdGroupByType(memberId)).willReturn(List.of());

      StatsResponse result = activityService.getStats(memberId);

      assertThat(result.blog()).isZero();
      assertThat(result.qna()).isZero();
      assertThat(result.session()).isZero();
    }

    @Test
    @DisplayName("memberId 미존재 → MemberNotFoundException")
    void getStats_memberNotFound() {
      given(memberRepository.existsById(999L)).willReturn(false);

      assertThatThrownBy(() -> activityService.getStats(999L))
          .isInstanceOf(MemberNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("§6-2 작성형 활동 목록 (getCreations)")
  class GetCreations {

    @Test
    @DisplayName("작성형 type 집합으로 Repository 위임")
    void getCreations_passesCreationTypes() {
      Long memberId = 1L;
      given(memberRepository.existsById(memberId)).willReturn(true);
      given(
              activityRepository.findByMember_IdAndTypeIn(
                  eq(memberId), eq(ActivityType.creationTypes()), any(Pageable.class)))
          .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

      PageWrapper<ActivityResponse> result = activityService.getCreations(memberId, 0, 20);

      assertThat(result.content()).isEmpty();
      verify(activityRepository)
          .findByMember_IdAndTypeIn(
              eq(memberId), eq(ActivityType.creationTypes()), any(Pageable.class));
    }

    @Test
    @DisplayName("memberId 미존재 → MemberNotFoundException")
    void getCreations_memberNotFound() {
      given(memberRepository.existsById(999L)).willReturn(false);

      assertThatThrownBy(() -> activityService.getCreations(999L, 0, 20))
          .isInstanceOf(MemberNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("§6-3 본인 반응형 활동 목록 (getReactions)")
  class GetReactions {

    @Test
    @DisplayName("토큰 userId → Member.id로 변환 후 반응형 type만 조회")
    void getReactions_translatesUserIdToMemberId() {
      Long viewerUserId = 7L;
      Long memberId = 1L;
      Member viewer = Mockito.mock(Member.class);
      given(viewer.getId()).willReturn(memberId);
      given(memberRepository.findByUserId(viewerUserId)).willReturn(Optional.of(viewer));
      given(
              activityRepository.findByMember_IdAndTypeIn(
                  eq(memberId), eq(ActivityType.reactionTypes()), any(Pageable.class)))
          .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

      activityService.getReactions(viewerUserId, 0, 20);

      verify(activityRepository)
          .findByMember_IdAndTypeIn(
              eq(memberId), eq(ActivityType.reactionTypes()), any(Pageable.class));
    }

    @Test
    @DisplayName("토큰 userId에 매핑된 Member 없음 → MemberNotFoundException")
    void getReactions_noMemberForUserId() {
      given(memberRepository.findByUserId(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> activityService.getReactions(999L, 0, 20))
          .isInstanceOf(MemberNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("§6-4 기여도 랭킹 (getRanking)")
  class GetRanking {

    @Test
    @DisplayName("rank 1-based, 페이지 오프셋 0이면 1부터")
    void getRanking_firstPage_rankStartsAt1() {
      List<RankingProjection> rows =
          List.of(
              new RankingProjection(1L, "홍길동", "url1", 120L, 5L),
              new RankingProjection(3L, "김지수", "url3", 95L, 4L),
              new RankingProjection(5L, "이몽룡", "url5", 0L, 0L));
      given(activityRepository.findRanking(eq(null), eq(null), any())).willReturn(rows);
      given(activityRepository.countRankingMembers(null)).willReturn(3L);

      PageWrapper<RankingItemResponse> result =
          activityService.getRanking(RankingPeriod.all, null, 0, 20);

      assertThat(result.content()).hasSize(3);
      assertThat(result.content().get(0).rank()).isEqualTo(1);
      assertThat(result.content().get(0).totalScore()).isEqualTo(120);
      assertThat(result.content().get(2).rank()).isEqualTo(3);
      assertThat(result.content().get(2).totalScore()).isZero();
      assertThat(result.totalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("page=1·size=20 → rank 21부터 시작")
    void getRanking_secondPage_rankStartsAt21() {
      List<RankingProjection> rows = List.of(new RankingProjection(21L, "21등", "url21", 10L, 1L));
      given(activityRepository.findRanking(any(), any(), any())).willReturn(rows);
      given(activityRepository.countRankingMembers(null)).willReturn(50L);

      PageWrapper<RankingItemResponse> result =
          activityService.getRanking(RankingPeriod.all, null, 1, 20);

      assertThat(result.content().get(0).rank()).isEqualTo(21);
      assertThat(result.page()).isEqualTo(1);
      assertThat(result.totalElements()).isEqualTo(50);
    }

    @Test
    @DisplayName("generationId 필터 → Repository에 그대로 전달")
    void getRanking_withGenerationId() {
      given(activityRepository.findRanking(any(), eq(13), any())).willReturn(List.of());
      given(activityRepository.countRankingMembers(13)).willReturn(0L);

      PageWrapper<RankingItemResponse> result =
          activityService.getRanking(RankingPeriod.year, 13, 0, 20);

      assertThat(result.content()).isEmpty();
    }

    @Test
    @DisplayName("totalScore null → 0 (null safe)")
    void getRanking_nullTotalScoreHandled() {
      List<RankingProjection> rows = List.of(new RankingProjection(1L, "홍길동", "url1", null, 0L));
      given(activityRepository.findRanking(any(), any(), any())).willReturn(rows);
      given(activityRepository.countRankingMembers(null)).willReturn(1L);

      PageWrapper<RankingItemResponse> result =
          activityService.getRanking(RankingPeriod.all, null, 0, 20);

      assertThat(result.content().get(0).totalScore()).isZero();
    }
  }
}
