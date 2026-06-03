package com.study.blog.application.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.study.blog.application.admin.dto.AdminPostResponse;
import com.study.blog.application.admin.dto.AdminStatsResponse;
import com.study.blog.application.admin.dto.PostStatusUpdateRequest;
import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostStatus;
import com.study.blog.infrastructure.admin.AdminActionLogRepository;
import com.study.blog.infrastructure.comment.CommentRepository;
import com.study.blog.infrastructure.post.PostLikeRepository;
import com.study.blog.infrastructure.post.PostRepository;
import com.study.blog.infrastructure.post.PostTagRepository;
import com.study.blog.shared.exception.BlogErrorCode;
import com.study.blog.shared.exception.BlogException;
import com.study.shared.extevent.blog.BlogPostCreated;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AdminService")
class AdminServiceTest {

  @Mock PostRepository postRepository;
  @Mock PostTagRepository postTagRepository;
  @Mock PostLikeRepository postLikeRepository;
  @Mock CommentRepository commentRepository;
  @Mock AdminActionLogRepository adminActionLogRepository;
  @Mock ApplicationEventPublisher eventPublisher;
  @InjectMocks AdminService adminService;

  private static final Long ACTOR_ID = 99L;

  private Post postWithId(Long id, Long userId, PostStatus status) {
    Post p =
        Post.builder()
            .userId(userId)
            .title("제목")
            .content("내용")
            .board("백엔드")
            .category("Spring")
            .status(status)
            .generation("13기")
            .build();
    ReflectionTestUtils.setField(p, "id", id);
    return p;
  }

  // ── getStats ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getStats")
  class GetStats {

    @Test
    @DisplayName("6개 필드 모두 정확히 집계")
    void aggregatesAllSixCountsFromRepositories() {
      when(postRepository.count()).thenReturn(20L);
      when(postRepository.count(any(Specification.class)))
          .thenReturn(2L) // DRAFT
          .thenReturn(5L) // PENDING_REVIEW
          .thenReturn(10L) // PUBLISHED
          .thenReturn(3L); // REJECTED
      when(commentRepository.count()).thenReturn(30L);

      AdminStatsResponse res = adminService.getStats();

      assertThat(res.totalPosts()).isEqualTo(20L);
      assertThat(res.draftPosts()).isEqualTo(2L);
      assertThat(res.pendingReviewPosts()).isEqualTo(5L);
      assertThat(res.publishedPosts()).isEqualTo(10L);
      assertThat(res.rejectedPosts()).isEqualTo(3L);
      assertThat(res.totalComments()).isEqualTo(30L);
    }
  }

  // ── changePostStatus ───────────────────────────────────────────────────────

  @Nested
  @DisplayName("changePostStatus")
  class ChangePostStatus {

    private PostStatusUpdateRequest req(PostStatus status) {
      return new PostStatusUpdateRequest(status, null);
    }

    private PostStatusUpdateRequest req(PostStatus status, String reason) {
      return new PostStatusUpdateRequest(status, reason);
    }

    @Test
    @DisplayName(
        "PENDING_REVIEW → PUBLISHED: publish() 호출로 rejectedReason 초기화 + BlogPostCreated 이벤트 발행")
    void pendingReview_toPublished_callsPublishAndPublishesEvent() {
      Post post = postWithId(1L, 10L, PostStatus.PENDING_REVIEW);
      post.reject("이전 거부 사유");
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));
      when(postTagRepository.findByPost(post)).thenReturn(List.of());
      when(postLikeRepository.countByIdPostId(1L)).thenReturn(0L);

      AdminPostResponse res = adminService.changePostStatus(1L, req(PostStatus.PUBLISHED));

      assertThat(res.status()).isEqualTo(PostStatus.PUBLISHED);
      assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
      assertThat(post.getRejectedReason()).isNull();
      verify(eventPublisher).publishEvent(any(BlogPostCreated.class));
    }

    @Test
    @DisplayName("DRAFT → PUBLISHED 상태 변경")
    void draft_toPublished_changesStatus() {
      Post post = postWithId(1L, 10L, PostStatus.DRAFT);
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));
      when(postTagRepository.findByPost(post)).thenReturn(List.of());
      when(postLikeRepository.countByIdPostId(1L)).thenReturn(0L);

      AdminPostResponse res = adminService.changePostStatus(1L, req(PostStatus.PUBLISHED));

      assertThat(res.status()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("PENDING_REVIEW → REJECTED: 사유 있으면 reject() 호출")
    void pendingReview_toRejected_withReason_callsReject() {
      Post post = postWithId(1L, 10L, PostStatus.PENDING_REVIEW);
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));
      when(postTagRepository.findByPost(post)).thenReturn(List.of());
      when(postLikeRepository.countByIdPostId(1L)).thenReturn(0L);

      AdminPostResponse res = adminService.changePostStatus(1L, req(PostStatus.REJECTED, "내용 부족"));

      assertThat(res.status()).isEqualTo(PostStatus.REJECTED);
      assertThat(post.getRejectedReason()).isEqualTo("내용 부족");
    }

    @Test
    @DisplayName("REJECTED: 사유 없으면 → REJECTION_REASON_REQUIRED")
    void toRejected_withoutReason_throwsRejectionReasonRequired() {
      Post post = postWithId(1L, 10L, PostStatus.PENDING_REVIEW);
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> adminService.changePostStatus(1L, req(PostStatus.REJECTED)))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.REJECTION_REASON_REQUIRED));
    }

    @Test
    @DisplayName("REJECTED: 빈 문자열 사유 → REJECTION_REASON_REQUIRED")
    void toRejected_withBlankReason_throwsRejectionReasonRequired() {
      Post post = postWithId(1L, 10L, PostStatus.PENDING_REVIEW);
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> adminService.changePostStatus(1L, req(PostStatus.REJECTED, "   ")))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.REJECTION_REASON_REQUIRED));
    }

    @Test
    @DisplayName("PUBLISHED → DRAFT 상태 변경")
    void published_toDraft_changesStatus() {
      Post post = postWithId(1L, 10L, PostStatus.PUBLISHED);
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));
      when(postTagRepository.findByPost(post)).thenReturn(List.of());
      when(postLikeRepository.countByIdPostId(1L)).thenReturn(0L);

      AdminPostResponse res = adminService.changePostStatus(1L, req(PostStatus.DRAFT));

      assertThat(res.status()).isEqualTo(PostStatus.DRAFT);
    }

    @Test
    @DisplayName("존재하지 않는 포스트 → POST_NOT_FOUND")
    void notFound_throwsPostNotFound() {
      when(postRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminService.changePostStatus(999L, req(PostStatus.PUBLISHED)))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_NOT_FOUND));
    }
  }

  // ── forceDeletePost ────────────────────────────────────────────────────────

  @Nested
  @DisplayName("forceDeletePost")
  class ForceDeletePost {

    @Test
    @DisplayName("HIDDEN 상태 + 24시간 경과 → 태그 삭제 후 포스트 삭제")
    void deletesTagsThenPost() {
      Post post = postWithId(1L, 10L, PostStatus.HIDDEN);
      // hiddenAt을 25시간 전으로 설정
      ReflectionTestUtils.setField(post, "hiddenAt", java.time.LocalDateTime.now().minusHours(25));
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));
      when(adminActionLogRepository.save(any())).thenReturn(null);

      adminService.forceDeletePost(1L, ACTOR_ID);

      verify(postTagRepository).deleteByPost(post);
      verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("HIDDEN 아닌 포스트 → POST_NOT_HIDDEN")
    void notHidden_throwsPostNotHidden() {
      Post post = postWithId(1L, 10L, PostStatus.PUBLISHED);
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> adminService.forceDeletePost(1L, ACTOR_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_NOT_HIDDEN));
    }

    @Test
    @DisplayName("숨김 후 24시간 미경과 → POST_DELETE_TOO_EARLY")
    void tooEarly_throwsPostDeleteTooEarly() {
      Post post = postWithId(1L, 10L, PostStatus.HIDDEN);
      ReflectionTestUtils.setField(post, "hiddenAt", java.time.LocalDateTime.now().minusHours(1));
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> adminService.forceDeletePost(1L, ACTOR_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_DELETE_TOO_EARLY));
    }

    @Test
    @DisplayName("존재하지 않는 포스트 → POST_NOT_FOUND")
    void notFound_throwsPostNotFound() {
      when(postRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminService.forceDeletePost(999L, ACTOR_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_NOT_FOUND));
    }
  }
}
