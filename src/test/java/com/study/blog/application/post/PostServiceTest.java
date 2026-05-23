package com.study.blog.application.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.study.auth.domain.User;
import com.study.blog.application.post.dto.PostCreateRequest;
import com.study.blog.application.post.dto.PostResponse;
import com.study.blog.application.post.dto.PostSummaryResponse;
import com.study.blog.application.post.dto.PostUpdateRequest;
import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostBookmark;
import com.study.blog.domain.post.PostLike;
import com.study.blog.domain.post.PostStatus;
import com.study.blog.domain.post.PostTag;
import com.study.blog.infrastructure.post.PostBookmarkRepository;
import com.study.blog.infrastructure.post.PostLikeRepository;
import com.study.blog.infrastructure.post.PostRepository;
import com.study.blog.infrastructure.post.PostTagRepository;
import com.study.blog.shared.exception.BlogErrorCode;
import com.study.blog.shared.exception.BlogException;
import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.generation.MemberGeneration;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.MemberGenerationRepository;
import com.study.profile.infrastructure.MemberRepository;
import com.study.shared.extevent.blog.BlogPostCreated;
import com.study.shared.extevent.blog.BlogPostDeleted;
import com.study.shared.extevent.blog.BlogPostLiked;
import com.study.shared.extevent.blog.BlogPostUnliked;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PostService")
class PostServiceTest {

  static final Long POST_ID = 1L;
  static final Long USER_ID = 10L;
  static final Long OTHER_USER_ID = 20L;

  @Mock PostRepository postRepository;
  @Mock PostTagRepository postTagRepository;
  @Mock PostLikeRepository postLikeRepository;
  @Mock PostBookmarkRepository postBookmarkRepository;
  @Mock MemberRepository memberRepository;
  @Mock MemberGenerationRepository memberGenerationRepository;
  @Mock ApplicationEventPublisher eventPublisher;
  @InjectMocks PostService postService;

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

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

  private void stubToResponse(Post post, Long requesterId) {
    when(postTagRepository.findByPost(post)).thenReturn(List.of());
    when(postLikeRepository.countByIdPostId(post.getId())).thenReturn(0L);
    when(postBookmarkRepository.countByIdPostId(post.getId())).thenReturn(0L);
    when(memberRepository.findByUserId(post.getUserId())).thenReturn(Optional.empty());
    if (requesterId != null) {
      when(postLikeRepository.findByIdPostIdAndIdUserId(post.getId(), requesterId))
          .thenReturn(Optional.empty());
      when(postBookmarkRepository.findByIdPostIdAndIdUserId(post.getId(), requesterId))
          .thenReturn(Optional.empty());
    }
  }

  // ── getPost ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getPost")
  class GetPost {

    @Test
    @DisplayName("PUBLISHED 포스트 - requesterId 없어도 조회 가능")
    void publishedPost_nullRequester_accessible() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      stubToResponse(post, null);

      PostResponse res = postService.getPost(POST_ID, null);

      assertThat(res.id()).isEqualTo(POST_ID);
      assertThat(res.status()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("DRAFT 포스트 - 작성자 본인은 조회 가능")
    void draftPost_owner_accessible() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      stubToResponse(post, USER_ID);

      PostResponse res = postService.getPost(POST_ID, USER_ID);

      assertThat(res.status()).isEqualTo(PostStatus.DRAFT);
    }

    @Test
    @DisplayName("DRAFT 포스트 - 타인 요청 → FORBIDDEN")
    void draftPost_otherUser_throwsForbidden() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> postService.getPost(POST_ID, OTHER_USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("DRAFT 포스트 - 비로그인 → FORBIDDEN")
    void draftPost_nullRequester_throwsForbidden() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> postService.getPost(POST_ID, null))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("PENDING_REVIEW 포스트 - 작성자 본인 조회 가능")
    void pendingReviewPost_owner_accessible() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PENDING_REVIEW);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      stubToResponse(post, USER_ID);

      PostResponse res = postService.getPost(POST_ID, USER_ID);

      assertThat(res.status()).isEqualTo(PostStatus.PENDING_REVIEW);
    }

    @Test
    @DisplayName("PENDING_REVIEW 포스트 - 타인 요청 → FORBIDDEN")
    void pendingReviewPost_otherUser_throwsForbidden() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PENDING_REVIEW);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> postService.getPost(POST_ID, OTHER_USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("REJECTED 포스트 - 작성자 본인 조회 가능")
    void rejectedPost_owner_accessible() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.REJECTED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      stubToResponse(post, USER_ID);

      PostResponse res = postService.getPost(POST_ID, USER_ID);

      assertThat(res.status()).isEqualTo(PostStatus.REJECTED);
    }

    @Test
    @DisplayName("REJECTED 포스트 - 타인 요청 → FORBIDDEN")
    void rejectedPost_otherUser_throwsForbidden() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.REJECTED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> postService.getPost(POST_ID, OTHER_USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("PENDING_REVIEW 포스트 - 어드민 조회 가능")
    void pendingReviewPost_admin_accessible() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PENDING_REVIEW);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      stubToResponse(post, OTHER_USER_ID);

      PostResponse res = postService.getPost(POST_ID, OTHER_USER_ID, true);

      assertThat(res.status()).isEqualTo(PostStatus.PENDING_REVIEW);
    }

    @Test
    @DisplayName("REJECTED 포스트 - 어드민 조회 가능")
    void rejectedPost_admin_accessible() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.REJECTED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      stubToResponse(post, OTHER_USER_ID);

      PostResponse res = postService.getPost(POST_ID, OTHER_USER_ID, true);

      assertThat(res.status()).isEqualTo(PostStatus.REJECTED);
    }

    @Test
    @DisplayName("존재하지 않는 포스트 → POST_NOT_FOUND")
    void notFound_throwsPostNotFound() {
      when(postRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> postService.getPost(999L, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_NOT_FOUND));
    }
  }

  // ── createPost ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("createPost")
  class CreatePost {

    @Test
    @DisplayName("항상 DRAFT 상태로 저장")
    void alwaysSavesAsDraft() {
      PostCreateRequest req = new PostCreateRequest("제목", "내용", "백엔드", "Spring", List.of(), null);
      Post saved = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.save(any())).thenReturn(saved);
      when(memberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
      stubToResponse(saved, USER_ID);

      PostResponse res = postService.createPost(req, USER_ID);

      assertThat(res.status()).isEqualTo(PostStatus.DRAFT);
    }

    @Test
    @DisplayName("태그 있으면 각 태그마다 save 호출")
    void withTags_savesEachTag() {
      PostCreateRequest req =
          new PostCreateRequest("제목", "내용", "백엔드", "Spring", List.of("spring", "java"), null);
      Post saved = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.save(any())).thenReturn(saved);
      when(postTagRepository.save(any())).thenReturn(null);
      when(memberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
      stubToResponse(saved, USER_ID);

      postService.createPost(req, USER_ID);

      verify(postTagRepository, org.mockito.Mockito.times(2)).save(any(PostTag.class));
    }

    @Test
    @DisplayName("중복 태그는 한 번만 저장")
    void withDuplicateTags_savesDistinct() {
      PostCreateRequest req =
          new PostCreateRequest(
              "제목", "내용", "백엔드", "Spring", List.of("spring", "spring", "java"), null);
      Post saved = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.save(any())).thenReturn(saved);
      when(postTagRepository.save(any())).thenReturn(null);
      when(memberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
      stubToResponse(saved, USER_ID);

      postService.createPost(req, USER_ID);

      verify(postTagRepository, org.mockito.Mockito.times(2)).save(any(PostTag.class));
    }

    @Test
    @DisplayName("태그 없으면 postTagRepository.save 미호출")
    void withNoTags_doesNotSaveTags() {
      PostCreateRequest req = new PostCreateRequest("제목", "내용", "백엔드", "Spring", null, null);
      Post saved = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.save(any())).thenReturn(saved);
      when(memberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
      stubToResponse(saved, USER_ID);

      postService.createPost(req, USER_ID);

      verify(postTagRepository, never()).save(any());
    }
  }

  // ── updatePost ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("updatePost")
  class UpdatePost {

    @Test
    @DisplayName("본인 포스트 수정 → 기존 태그 삭제 후 새 태그 저장")
    void owner_deletesOldTagsAndSavesNew() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      PostUpdateRequest req =
          new PostUpdateRequest("새 제목", "새 내용", "프론트", "React", List.of("react"));
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      when(postTagRepository.save(any())).thenReturn(null);
      stubToResponse(post, USER_ID);

      PostResponse res = postService.updatePost(POST_ID, req, USER_ID);

      assertThat(res.title()).isEqualTo("새 제목");
      verify(postTagRepository).deleteByPost(post);
      verify(postTagRepository).save(any(PostTag.class));
    }

    @Test
    @DisplayName("본인 포스트 수정 - status는 기존 값 유지")
    void owner_statusUnchanged() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      PostUpdateRequest req = new PostUpdateRequest("새 제목", "내용", "백엔드", "Spring", List.of());
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      stubToResponse(post, USER_ID);

      PostResponse res = postService.updatePost(POST_ID, req, USER_ID);

      assertThat(res.status()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("타인 포스트 수정 → FORBIDDEN")
    void notOwner_throwsForbidden() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      PostUpdateRequest req = new PostUpdateRequest("x", "x", "x", "x", List.of());
      assertThatThrownBy(() -> postService.updatePost(POST_ID, req, OTHER_USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("존재하지 않는 포스트 수정 → POST_NOT_FOUND")
    void notFound_throwsPostNotFound() {
      when(postRepository.findById(999L)).thenReturn(Optional.empty());

      PostUpdateRequest req = new PostUpdateRequest("x", "x", "x", "x", List.of());
      assertThatThrownBy(() -> postService.updatePost(999L, req, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_NOT_FOUND));
    }

    @Test
    @DisplayName("REJECTED 포스트 수정 → DRAFT로 전환, rejectedReason 유지")
    void rejectedPost_update_resetsToDraftPreservingReason() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.REJECTED);
      post.reject("내용 부족");
      PostUpdateRequest req = new PostUpdateRequest("수정된 제목", "수정된 내용", "백엔드", "Spring", List.of());
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      stubToResponse(post, USER_ID);

      postService.updatePost(POST_ID, req, USER_ID);

      assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
      assertThat(post.getRejectedReason()).isEqualTo("내용 부족");
    }

    @Test
    @DisplayName("DRAFT 포스트 수정 → 상태 변경 없음")
    void draftPost_update_statusUnchanged() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      PostUpdateRequest req = new PostUpdateRequest("새 제목", "새 내용", "백엔드", "Spring", List.of());
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      stubToResponse(post, USER_ID);

      postService.updatePost(POST_ID, req, USER_ID);

      assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
    }
  }

  // ── deletePost ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("deletePost")
  class DeletePost {

    @Test
    @DisplayName("본인 포스트 삭제 → 태그 삭제 후 포스트 삭제")
    void owner_deletesTagsThenPost() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      postService.deletePost(POST_ID, USER_ID);

      verify(postTagRepository).deleteByPost(post);
      verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("타인 포스트 삭제 → FORBIDDEN")
    void notOwner_throwsForbidden() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> postService.deletePost(POST_ID, OTHER_USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.FORBIDDEN));
      verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("존재하지 않는 포스트 삭제 → POST_NOT_FOUND")
    void notFound_throwsPostNotFound() {
      when(postRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> postService.deletePost(999L, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_NOT_FOUND));
    }
  }

  // ── toggleLike ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("toggleLike")
  class ToggleLike {

    @Test
    @DisplayName("좋아요 없는 상태 → 저장 후 true")
    void noExistingLike_savesAndReturnsTrue() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      when(postLikeRepository.findByIdPostIdAndIdUserId(POST_ID, USER_ID))
          .thenReturn(Optional.empty());

      boolean result = postService.toggleLike(POST_ID, USER_ID);

      assertThat(result).isTrue();
      verify(postLikeRepository).saveAndFlush(any(PostLike.class));
    }

    @Test
    @DisplayName("좋아요 있는 상태 → 삭제 후 false")
    void existingLike_deletesAndReturnsFalse() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      PostLike existing = new PostLike(post, USER_ID);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      when(postLikeRepository.findByIdPostIdAndIdUserId(POST_ID, USER_ID))
          .thenReturn(Optional.of(existing));

      boolean result = postService.toggleLike(POST_ID, USER_ID);

      assertThat(result).isFalse();
      verify(postLikeRepository).delete(existing);
    }

    @Test
    @DisplayName("존재하지 않는 포스트 → POST_NOT_FOUND")
    void notFound_throwsPostNotFound() {
      when(postRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> postService.toggleLike(999L, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_NOT_FOUND));
    }
  }

  // ── toggleBookmark ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("toggleBookmark")
  class ToggleBookmark {

    @Test
    @DisplayName("북마크 없는 상태 → 저장 후 true")
    void noExistingBookmark_savesAndReturnsTrue() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      when(postBookmarkRepository.findByIdPostIdAndIdUserId(POST_ID, USER_ID))
          .thenReturn(Optional.empty());

      boolean result = postService.toggleBookmark(POST_ID, USER_ID);

      assertThat(result).isTrue();
      verify(postBookmarkRepository).saveAndFlush(any(PostBookmark.class));
    }

    @Test
    @DisplayName("북마크 있는 상태 → 삭제 후 false")
    void existingBookmark_deletesAndReturnsFalse() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      PostBookmark existing = new PostBookmark(post, USER_ID);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      when(postBookmarkRepository.findByIdPostIdAndIdUserId(POST_ID, USER_ID))
          .thenReturn(Optional.of(existing));

      boolean result = postService.toggleBookmark(POST_ID, USER_ID);

      assertThat(result).isFalse();
      verify(postBookmarkRepository).delete(existing);
    }

    @Test
    @DisplayName("DRAFT 포스트 북마크 → POST_NOT_PUBLISHED")
    void draftPost_throwsPostNotPublished() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> postService.toggleBookmark(POST_ID, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_NOT_PUBLISHED));
    }

    @Test
    @DisplayName("존재하지 않는 포스트 → POST_NOT_FOUND")
    void notFound_throwsPostNotFound() {
      when(postRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> postService.toggleBookmark(999L, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_NOT_FOUND));
    }
  }

  // ── getBookmarkedPosts ─────────────────────────────────────────────────────

  @Nested
  @DisplayName("getBookmarkedPosts")
  class GetBookmarkedPosts {

    private void stubBookmarkedPage(Post post) {
      when(postBookmarkRepository.findPostIdsByUserId(USER_ID)).thenReturn(List.of(post.getId()));
      when(postRepository.findAll(any(Specification.class), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of(post)));
      when(postTagRepository.findByIdPostIdIn(any())).thenReturn(List.of());
      when(postLikeRepository.countGroupedByPostId(any())).thenReturn(List.of());
      when(memberRepository.findAllByUserIdIn(any())).thenReturn(List.of());
    }

    @Test
    @DisplayName("북마크 없으면 빈 페이지 반환 — postRepository.findAll 미호출")
    void noBookmarks_returnsEmptyPage() {
      when(postBookmarkRepository.findPostIdsByUserId(USER_ID)).thenReturn(List.of());

      Page<PostSummaryResponse> result = postService.getBookmarkedPosts(USER_ID, 0, 10);

      assertThat(result.getTotalElements()).isZero();
      verify(postRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("태그 있으면 응답에 포함")
    void withTags_tagsInResponse() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      stubBookmarkedPage(post);
      PostTag tag = new PostTag(post, "spring");
      when(postTagRepository.findByIdPostIdIn(any())).thenReturn(List.of(tag));

      Page<PostSummaryResponse> result = postService.getBookmarkedPosts(USER_ID, 0, 10);

      assertThat(result.getContent().get(0).tags()).containsExactly("spring");
    }

    @Test
    @DisplayName("태그 없으면 빈 리스트")
    void withoutTags_emptyTagList() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      stubBookmarkedPage(post);

      Page<PostSummaryResponse> result = postService.getBookmarkedPosts(USER_ID, 0, 10);

      assertThat(result.getContent().get(0).tags()).isEmpty();
    }

    @Test
    @DisplayName("좋아요 수 응답에 반영")
    void withLikeCount_likeCountInResponse() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      stubBookmarkedPage(post);
      when(postLikeRepository.countGroupedByPostId(any()))
          .thenReturn(List.<Object[]>of(new Object[] {POST_ID, 5L}));

      Page<PostSummaryResponse> result = postService.getBookmarkedPosts(USER_ID, 0, 10);

      assertThat(result.getContent().get(0).likeCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("좋아요 없으면 likeCount = 0")
    void withoutLikes_likeCountIsZero() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      stubBookmarkedPage(post);

      Page<PostSummaryResponse> result = postService.getBookmarkedPosts(USER_ID, 0, 10);

      assertThat(result.getContent().get(0).likeCount()).isZero();
    }

    @Test
    @DisplayName("Member 있으면 authorName 응답에 포함")
    void withMember_authorNameInResponse() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      stubBookmarkedPage(post);
      User user = mock(User.class);
      when(user.getId()).thenReturn(USER_ID);
      Member member = mock(Member.class);
      when(member.getUser()).thenReturn(user);
      when(member.getName()).thenReturn("홍길동");
      when(memberRepository.findAllByUserIdIn(any())).thenReturn(List.of(member));

      Page<PostSummaryResponse> result = postService.getBookmarkedPosts(USER_ID, 0, 10);

      assertThat(result.getContent().get(0).authorName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("Member 없으면 authorName = null")
    void withoutMember_authorNameIsNull() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      stubBookmarkedPage(post);

      Page<PostSummaryResponse> result = postService.getBookmarkedPosts(USER_ID, 0, 10);

      assertThat(result.getContent().get(0).authorName()).isNull();
    }

    @Test
    @DisplayName("replyToId 있으면 원글 제목 응답에 포함")
    void withReplyToId_replyTitleInResponse() {
      Post original = postWithId(2L, OTHER_USER_ID, PostStatus.PUBLISHED);
      Post reply =
          Post.builder()
              .userId(USER_ID)
              .title("답글")
              .content("내용")
              .board("백엔드")
              .category("Spring")
              .status(PostStatus.PUBLISHED)
              .replyToId(2L)
              .build();
      ReflectionTestUtils.setField(reply, "id", POST_ID);

      when(postBookmarkRepository.findPostIdsByUserId(USER_ID)).thenReturn(List.of(POST_ID));
      when(postRepository.findAll(any(Specification.class), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of(reply)));
      when(postTagRepository.findByIdPostIdIn(any())).thenReturn(List.of());
      when(postLikeRepository.countGroupedByPostId(any())).thenReturn(List.of());
      when(memberRepository.findAllByUserIdIn(any())).thenReturn(List.of());
      when(postRepository.findAllById(List.of(2L))).thenReturn(List.of(original));

      Page<PostSummaryResponse> result = postService.getBookmarkedPosts(USER_ID, 0, 10);

      assertThat(result.getContent().get(0).replyToTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("replyToId 없으면 postRepository.findAllById 미호출")
    void withoutReplyToId_findAllByIdNotCalled() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      stubBookmarkedPage(post);

      postService.getBookmarkedPosts(USER_ID, 0, 10);

      verify(postRepository, never()).findAllById(any());
    }
  }

  // ── authorName ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("authorName 조회")
  class AuthorName {

    @Test
    @DisplayName("Member가 존재하면 getPost 응답에 authorName이 포함된다")
    void memberExists_getPost_returnsAuthorName() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      Member member = mock(Member.class);
      when(member.getName()).thenReturn("홍길동");

      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      when(postTagRepository.findByPost(post)).thenReturn(List.of());
      when(postLikeRepository.countByIdPostId(POST_ID)).thenReturn(0L);
      when(postBookmarkRepository.countByIdPostId(POST_ID)).thenReturn(0L);
      when(memberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(member));

      PostResponse res = postService.getPost(POST_ID, null);

      assertThat(res.authorName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("Member가 없으면 getPost 응답의 authorName이 null이다")
    void memberNotFound_getPost_authorNameIsNull() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      stubToResponse(post, null); // memberRepository → Optional.empty()

      PostResponse res = postService.getPost(POST_ID, null);

      assertThat(res.authorName()).isNull();
    }
  }

  // ── submitPost ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("submitPost")
  class SubmitPost {

    @Test
    @DisplayName("DRAFT → PENDING_REVIEW 성공")
    void draft_toPendingReview_success() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      stubToResponse(post, USER_ID);

      PostResponse res = postService.submitPost(POST_ID, USER_ID);

      assertThat(res.status()).isEqualTo(PostStatus.PENDING_REVIEW);
      assertThat(post.getStatus()).isEqualTo(PostStatus.PENDING_REVIEW);
    }

    @Test
    @DisplayName("PENDING_REVIEW 포스트 → INVALID_STATUS_TRANSITION")
    void pendingReview_throwsInvalidStatusTransition() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PENDING_REVIEW);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> postService.submitPost(POST_ID, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    @DisplayName("PUBLISHED 포스트 → INVALID_STATUS_TRANSITION")
    void published_throwsInvalidStatusTransition() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> postService.submitPost(POST_ID, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    @DisplayName("타인 포스트 제출 → FORBIDDEN")
    void otherUser_throwsForbidden() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      assertThatThrownBy(() -> postService.submitPost(POST_ID, OTHER_USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("존재하지 않는 포스트 → POST_NOT_FOUND")
    void notFound_throwsPostNotFound() {
      when(postRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> postService.submitPost(999L, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(
              e ->
                  assertThat(((BlogException) e).getErrorCode())
                      .isEqualTo(BlogErrorCode.POST_NOT_FOUND));
    }
  }

  // ── getMyPosts ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getMyPosts")
  class GetMyPosts {

    private void stubMyPostsPage(Post post) {
      when(postRepository.findAll(any(Specification.class), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of(post)));
      when(postTagRepository.findByIdPostIdIn(any())).thenReturn(List.of());
      when(postLikeRepository.countGroupedByPostId(any())).thenReturn(List.of());
      when(memberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("본인 포스트 목록 반환")
    void returnsUserPosts() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      stubMyPostsPage(post);

      Page<PostSummaryResponse> result = postService.getMyPosts(USER_ID, null, 0, 10);

      assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("DRAFT 상태 필터 결과 반환")
    void withDraftFilter_returnsDraftPosts() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      stubMyPostsPage(post);

      Page<PostSummaryResponse> result = postService.getMyPosts(USER_ID, PostStatus.DRAFT, 0, 10);

      assertThat(result.getContent().get(0).status()).isEqualTo(PostStatus.DRAFT);
    }

    @Test
    @DisplayName("PENDING_REVIEW 상태 필터 결과 반환")
    void withPendingReviewFilter_returnsPendingReviewPosts() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PENDING_REVIEW);
      stubMyPostsPage(post);

      Page<PostSummaryResponse> result =
          postService.getMyPosts(USER_ID, PostStatus.PENDING_REVIEW, 0, 10);

      assertThat(result.getContent().get(0).status()).isEqualTo(PostStatus.PENDING_REVIEW);
    }

    @Test
    @DisplayName("포스트 없으면 빈 페이지 반환")
    void noPosts_returnsEmptyPage() {
      when(postRepository.findAll(any(Specification.class), any(Pageable.class)))
          .thenReturn(Page.empty());
      when(memberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

      Page<PostSummaryResponse> result = postService.getMyPosts(USER_ID, null, 0, 10);

      assertThat(result.getTotalElements()).isZero();
    }
  }

  // ── 이벤트 발행 ────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("이벤트 발행")
  class EventPublishing {

    @Test
    @DisplayName("createPost - DRAFT 저장 시 BlogPostCreated 이벤트 미발행")
    void createPost_draft_doesNotPublishBlogPostCreated() {
      PostCreateRequest req = new PostCreateRequest("제목", "내용", "백엔드", "Spring", List.of(), null);
      Post saved = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.save(any())).thenReturn(saved);
      when(memberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
      stubToResponse(saved, USER_ID);

      postService.createPost(req, USER_ID);

      verify(eventPublisher, never()).publishEvent(any(BlogPostCreated.class));
    }

    @Test
    @DisplayName("deletePost - BlogPostDeleted 이벤트 발행")
    void deletePost_publishesBlogPostDeleted() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

      postService.deletePost(POST_ID, USER_ID);

      verify(eventPublisher).publishEvent(any(BlogPostDeleted.class));
    }

    @Test
    @DisplayName("toggleLike 추가 - BlogPostLiked 이벤트 발행")
    void toggleLike_add_publishesBlogPostLiked() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      when(postLikeRepository.findByIdPostIdAndIdUserId(POST_ID, USER_ID))
          .thenReturn(Optional.empty());

      postService.toggleLike(POST_ID, USER_ID);

      verify(eventPublisher).publishEvent(any(BlogPostLiked.class));
    }

    @Test
    @DisplayName("toggleLike 취소 - BlogPostUnliked 이벤트 발행")
    void toggleLike_remove_publishesBlogPostUnliked() {
      Post post = postWithId(POST_ID, USER_ID, PostStatus.PUBLISHED);
      PostLike existing = new PostLike(post, USER_ID);
      when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
      when(postLikeRepository.findByIdPostIdAndIdUserId(POST_ID, USER_ID))
          .thenReturn(Optional.of(existing));

      postService.toggleLike(POST_ID, USER_ID);

      verify(eventPublisher).publishEvent(any(BlogPostUnliked.class));
    }
  }

  // ── generation 자동 주입 ────────────────────────────────────────────────────

  @Nested
  @DisplayName("createPost - generation 자동 주입")
  class GenerationAutoInject {

    @Test
    @DisplayName("Member와 MemberGeneration이 있으면 \"N기\" 형식으로 generation이 저장된다")
    void memberWithGeneration_createPost_generationSaved() {
      PostCreateRequest req = new PostCreateRequest("제목", "내용", "백엔드", "Spring", List.of(), null);

      Member member = mock(Member.class);
      when(member.getId()).thenReturn(99L);
      Generation gen = mock(Generation.class);
      when(gen.getNumber()).thenReturn(17);
      MemberGeneration mg = mock(MemberGeneration.class);
      when(mg.getGeneration()).thenReturn(gen);

      // createPost calls findByUserId once (generation 조회), toResponse calls it again (authorName)
      when(memberRepository.findByUserId(USER_ID))
          .thenReturn(Optional.of(member)) // 1st call: generation 도출
          .thenReturn(Optional.empty()); // 2nd call: toResponse authorName
      when(memberGenerationRepository.findByMemberId(99L)).thenReturn(List.of(mg));

      Post saved = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.save(any())).thenReturn(saved);
      when(postTagRepository.findByPost(saved)).thenReturn(List.of());
      when(postLikeRepository.countByIdPostId(POST_ID)).thenReturn(0L);
      when(postBookmarkRepository.countByIdPostId(POST_ID)).thenReturn(0L);
      when(postLikeRepository.findByIdPostIdAndIdUserId(POST_ID, USER_ID))
          .thenReturn(Optional.empty());
      when(postBookmarkRepository.findByIdPostIdAndIdUserId(POST_ID, USER_ID))
          .thenReturn(Optional.empty());

      postService.createPost(req, USER_ID);

      verify(postRepository).save(argThat(p -> "17기".equals(p.getGeneration())));
    }

    @Test
    @DisplayName("Member가 없으면 generation이 null로 저장된다")
    void memberNotFound_createPost_generationIsNull() {
      PostCreateRequest req = new PostCreateRequest("제목", "내용", "백엔드", "Spring", List.of(), null);

      when(memberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

      Post saved = postWithId(POST_ID, USER_ID, PostStatus.DRAFT);
      when(postRepository.save(any())).thenReturn(saved);
      stubToResponse(saved, USER_ID);

      postService.createPost(req, USER_ID);

      verify(postRepository).save(argThat(p -> p.getGeneration() == null));
    }
  }
}
