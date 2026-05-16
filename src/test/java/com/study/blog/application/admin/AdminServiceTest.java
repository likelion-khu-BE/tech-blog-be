package com.study.blog.application.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.study.blog.application.admin.dto.AdminPostResponse;
import com.study.blog.application.admin.dto.AdminStatsResponse;
import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostStatus;
import com.study.blog.infrastructure.comment.CommentRepository;
import com.study.blog.infrastructure.post.PostLikeRepository;
import com.study.blog.infrastructure.post.PostRepository;
import com.study.blog.infrastructure.post.PostTagRepository;
import com.study.blog.shared.exception.BlogErrorCode;
import com.study.blog.shared.exception.BlogException;
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
  @InjectMocks AdminService adminService;

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
    @DisplayName("전체/발행/임시/댓글 수 집계 반환")
    void aggregatesCountsFromRepositories() {
      when(postRepository.count()).thenReturn(10L);
      when(postRepository.count(any(Specification.class))).thenReturn(7L);
      when(commentRepository.count()).thenReturn(30L);

      AdminStatsResponse res = adminService.getStats();

      assertThat(res.totalPosts()).isEqualTo(10L);
      assertThat(res.publishedPosts()).isEqualTo(7L);
      assertThat(res.draftPosts()).isEqualTo(3L); // 10 - 7
      assertThat(res.totalComments()).isEqualTo(30L);
    }
  }

  // ── changePostStatus ───────────────────────────────────────────────────────

  @Nested
  @DisplayName("changePostStatus")
  class ChangePostStatus {

    @Test
    @DisplayName("DRAFT → PUBLISHED 상태 변경")
    void draft_toPublished_changesStatus() {
      Post post = postWithId(1L, 10L, PostStatus.DRAFT);
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));
      when(postTagRepository.findByPost(post)).thenReturn(List.of());
      when(postLikeRepository.countByIdPostId(1L)).thenReturn(0L);

      AdminPostResponse res = adminService.changePostStatus(1L, PostStatus.PUBLISHED);

      assertThat(res.status()).isEqualTo(PostStatus.PUBLISHED);
      assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("PUBLISHED → DRAFT 상태 변경")
    void published_toDraft_changesStatus() {
      Post post = postWithId(1L, 10L, PostStatus.PUBLISHED);
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));
      when(postTagRepository.findByPost(post)).thenReturn(List.of());
      when(postLikeRepository.countByIdPostId(1L)).thenReturn(0L);

      AdminPostResponse res = adminService.changePostStatus(1L, PostStatus.DRAFT);

      assertThat(res.status()).isEqualTo(PostStatus.DRAFT);
    }

    @Test
    @DisplayName("존재하지 않는 포스트 → POST_NOT_FOUND")
    void notFound_throwsPostNotFound() {
      when(postRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminService.changePostStatus(999L, PostStatus.PUBLISHED))
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
    @DisplayName("태그 삭제 후 포스트 삭제")
    void deletesTagsThenPost() {
      Post post = postWithId(1L, 10L, PostStatus.PUBLISHED);
      when(postRepository.findById(1L)).thenReturn(Optional.of(post));

      adminService.forceDeletePost(1L);

      verify(postTagRepository).deleteByPost(post);
      verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("존재하지 않는 포스트 → POST_NOT_FOUND")
    void notFound_throwsPostNotFound() {
      when(postRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminService.forceDeletePost(999L))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_NOT_FOUND));
    }
  }
}
