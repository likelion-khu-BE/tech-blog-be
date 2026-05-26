package com.study.blog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.study.StudyBeApplication;
import com.study.blog.domain.comment.Comment;
import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostLike;
import com.study.blog.domain.post.PostStatus;
import com.study.blog.domain.post.PostTag;
import com.study.blog.infrastructure.comment.CommentRepository;
import com.study.blog.infrastructure.post.PostLikeRepository;
import com.study.blog.infrastructure.post.PostRepository;
import com.study.blog.infrastructure.post.PostTagRepository;
import com.study.config.TestcontainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for Admin API endpoints.
 *
 * <p>Test data:
 *
 * <ul>
 *   <li>3 PUBLISHED posts (p1, p2, p3), 1 DRAFT post (p4)
 *   <li>p1 has tags and a like from MOCK_USER
 *   <li>3 comments on p1 (2 roots + 1 reply)
 * </ul>
 */
@SpringBootTest(
    classes = StudyBeApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfig.class)
@DisplayName("어드민 API")
class AdminApiTest {

  static final Long MOCK_USER_ID = 1L;
  static final Long OTHER_USER_ID = 2L;

  @Autowired MockMvc mvc;
  @Autowired PostRepository postRepository;
  @Autowired PostTagRepository postTagRepository;
  @Autowired PostLikeRepository postLikeRepository;
  @Autowired CommentRepository commentRepository;

  Post p1, p2, p3, p4, p5, p6;

  @BeforeEach
  void setUp() {
    // p1: PUBLISHED, 13기, 백엔드/CI/CD, MOCK_USER — with tags and like
    p1 =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("Spring Boot + GitHub Actions CI/CD 구축")
                .content("GitHub Actions와 AWS EC2로 배포 파이프라인을 구성합니다.")
                .board("백엔드")
                .category("CI/CD")
                .status(PostStatus.PUBLISHED)
                .generation("13기")
                .build());
    postTagRepository.save(new PostTag(p1, "Spring Boot"));
    postTagRepository.save(new PostTag(p1, "GitHub Actions"));
    postLikeRepository.save(new PostLike(p1, MOCK_USER_ID));

    // p2: PUBLISHED, 12기, AI/LLM, OTHER_USER
    p2 =
        postRepository.save(
            Post.builder()
                .userId(OTHER_USER_ID)
                .title("ChatGPT API로 번역 서비스 구현")
                .content("OpenAI API를 Python으로 연동하는 방법입니다.")
                .board("AI")
                .category("LLM")
                .status(PostStatus.PUBLISHED)
                .generation("12기")
                .build());

    // p3: PUBLISHED, 13기, 해커톤/후기, OTHER_USER
    p3 =
        postRepository.save(
            Post.builder()
                .userId(OTHER_USER_ID)
                .title("멋쟁이사자처럼 13기 해커톤 후기")
                .content("48시간 해커톤 경험을 공유합니다.")
                .board("해커톤")
                .category("해커톤후기")
                .status(PostStatus.PUBLISHED)
                .generation("13기")
                .build());

    // p4: DRAFT, 13기, 백엔드/DevOps, MOCK_USER
    p4 =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("Docker Compose 로컬 개발환경 (작성중)")
                .content("작성 중인 임시 저장 글입니다.")
                .board("백엔드")
                .category("DevOps")
                .status(PostStatus.DRAFT)
                .generation("13기")
                .build());

    // p5: PENDING_REVIEW, 13기, 백엔드/JPA, MOCK_USER
    p5 =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("JPA N+1 문제 해결 방법 (검토 대기)")
                .content("검토 대기 중인 포스트입니다.")
                .board("백엔드")
                .category("JPA")
                .status(PostStatus.PENDING_REVIEW)
                .generation("13기")
                .build());

    // p6: REJECTED, 13기, 백엔드/Redis, OTHER_USER — with rejectedReason
    p6 =
        postRepository.save(
            Post.builder()
                .userId(OTHER_USER_ID)
                .title("Redis 캐시 전략 (거부됨)")
                .content("거부된 포스트입니다.")
                .board("백엔드")
                .category("Redis")
                .status(PostStatus.REJECTED)
                .generation("13기")
                .build());
    p6.reject("내용이 너무 짧습니다");

    // 3 comments on p1: 2 roots + 1 reply
    Comment c1 =
        commentRepository.save(
            Comment.builder()
                .postId(p1.getId())
                .userId(MOCK_USER_ID)
                .content("정말 유익한 글이네요!")
                .build());
    commentRepository.save(
        Comment.builder()
            .postId(p1.getId())
            .userId(OTHER_USER_ID)
            .content("EC2 Runner 설정이 어렵던데 어떻게 하셨나요?")
            .build());
    commentRepository.save(
        Comment.builder()
            .postId(p1.getId())
            .userId(MOCK_USER_ID)
            .parent(c1)
            .content("github-hosted runner 쓰시면 편해요!")
            .build());
  }

  // ── GET /api/blog/admin/stats ────────────────────────────────────────────

  @Test
  @DisplayName("GET /admin/stats - 6개 필드 정확한 카운트 반환")
  void getStats_returnsCorrectCounts() throws Exception {
    mvc.perform(get("/api/blog/admin/stats").with(TestAuth.asAdmin(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPosts").value(6))
        .andExpect(jsonPath("$.publishedPosts").value(3))
        .andExpect(jsonPath("$.draftPosts").value(1))
        .andExpect(jsonPath("$.pendingReviewPosts").value(1))
        .andExpect(jsonPath("$.rejectedPosts").value(1))
        .andExpect(jsonPath("$.totalComments").value(3));
  }

  @Test
  @DisplayName("GET /admin/stats - 임시저장 추가 후 카운트 증가")
  void getStats_afterAddingDraft_incrementsDraftCount() throws Exception {
    postRepository.save(
        Post.builder()
            .userId(OTHER_USER_ID)
            .title("새 임시저장 글")
            .content("새로운 임시저장 글입니다.")
            .board("AI")
            .category("LLM")
            .status(PostStatus.DRAFT)
            .generation("12기")
            .build());

    mvc.perform(get("/api/blog/admin/stats").with(TestAuth.asAdmin(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPosts").value(7))
        .andExpect(jsonPath("$.draftPosts").value(2))
        .andExpect(jsonPath("$.publishedPosts").value(3));
  }

  @Test
  @DisplayName("GET /admin/stats - 토큰 없음 401")
  void getStats_noToken_returns401() throws Exception {
    mvc.perform(get("/api/blog/admin/stats")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /admin/stats - 잘못된 토큰 401")
  void getStats_wrongToken_returns401() throws Exception {
    mvc.perform(get("/api/blog/admin/stats").header("X-Admin-Token", "wrong-token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /admin/stats - 일반 유저 토큰 403")
  void getStats_withMemberToken_returns403() throws Exception {
    mvc.perform(get("/api/blog/admin/stats").with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /admin/stats - pendingReviewPosts/rejectedPosts 필드 존재")
  void getStats_includesPendingReviewAndRejectedFields() throws Exception {
    mvc.perform(get("/api/blog/admin/stats").with(TestAuth.asAdmin(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pendingReviewPosts").isNumber())
        .andExpect(jsonPath("$.rejectedPosts").isNumber());
  }

  @Test
  @DisplayName("GET /admin/stats - 소프트 삭제 댓글도 집계에 포함")
  void getStats_softDeletedComment_isCountedInTotalComments() throws Exception {
    Comment extra =
        commentRepository.save(
            Comment.builder()
                .postId(p1.getId())
                .userId(MOCK_USER_ID)
                .content("소프트 삭제될 댓글")
                .build());
    extra.softDelete();

    // commentRepository.count() has no deleted_at filter → soft-deleted rows included
    mvc.perform(get("/api/blog/admin/stats").with(TestAuth.asAdmin(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalComments").value(4)); // 3 from setUp + 1 soft-deleted
  }

  // ── GET /api/blog/admin/posts ────────────────────────────────────────────

  @Test
  @DisplayName("GET /admin/posts - 모든 상태 포함 전체 조회")
  void getAllPosts_includesAllStatuses() throws Exception {
    mvc.perform(get("/api/blog/admin/posts").with(TestAuth.asAdmin(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(6))
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @DisplayName("GET /admin/posts - 기본 페이지 크기 20")
  void getAllPosts_pagination_defaultPage20() throws Exception {
    mvc.perform(get("/api/blog/admin/posts").with(TestAuth.asAdmin(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.numberOfElements").value(6))
        .andExpect(jsonPath("$.totalPages").value(1));
  }

  @Test
  @DisplayName("GET /admin/posts - 커스텀 페이지 크기")
  void getAllPosts_customPageSize_paginatesCorrectly() throws Exception {
    mvc.perform(
            get("/api/blog/admin/posts")
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .param("size", "2")
                .param("page", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(6))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalPages").value(3));
  }

  @Test
  @DisplayName("GET /admin/posts - 포스트 필드 검증 (모든 상태 포함)")
  void getAllPosts_postFields_includeAllStatuses() throws Exception {
    mvc.perform(
            get("/api/blog/admin/posts")
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .param("size", "20")
                .param("page", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].status", hasItem("DRAFT")))
        .andExpect(jsonPath("$.content[*].status", hasItem("PENDING_REVIEW")))
        .andExpect(jsonPath("$.content[*].status", hasItem("REJECTED")));
  }

  @Test
  @DisplayName("GET /admin/posts?status=PENDING_REVIEW - 검토 대기 포스트만 조회")
  void getAllPosts_filterByPendingReview_returnsOnlyPendingReview() throws Exception {
    mvc.perform(
            get("/api/blog/admin/posts")
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .param("status", "PENDING_REVIEW"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].status").value("PENDING_REVIEW"))
        .andExpect(jsonPath("$.content[0].title").value("JPA N+1 문제 해결 방법 (검토 대기)"));
  }

  @Test
  @DisplayName("GET /admin/posts?status=REJECTED - 거부된 포스트만 조회")
  void getAllPosts_filterByRejected_returnsOnlyRejected() throws Exception {
    mvc.perform(
            get("/api/blog/admin/posts")
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .param("status", "REJECTED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].status").value("REJECTED"));
  }

  @Test
  @DisplayName("GET /admin/posts?status=PUBLISHED - 게시된 포스트만 조회")
  void getAllPosts_filterByPublished_returnsOnlyPublished() throws Exception {
    mvc.perform(
            get("/api/blog/admin/posts")
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .param("status", "PUBLISHED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(3));
  }

  @Test
  @DisplayName("GET /admin/posts - 토큰 없음 401")
  void getAllPosts_noToken_returns401() throws Exception {
    mvc.perform(get("/api/blog/admin/posts")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /admin/posts - 일반 유저 토큰 403")
  void getAllPosts_withMemberToken_returns403() throws Exception {
    mvc.perform(get("/api/blog/admin/posts").with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isForbidden());
  }

  // ── PATCH /api/blog/admin/posts/{id}/status ──────────────────────────────

  @Test
  @DisplayName("PATCH /admin/posts/status - 게시→임시저장")
  void changePostStatus_publishedToDraft_succeeds() throws Exception {
    String body =
        """
        {"status": "DRAFT"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p1.getId())
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(p1.getId()))
        .andExpect(jsonPath("$.status").value("DRAFT"));

    Post updated = postRepository.findById(p1.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(PostStatus.DRAFT);
  }

  @Test
  @DisplayName("PATCH /admin/posts/status - 검토대기→게시: publish() 호출로 rejectedReason null")
  void changePostStatus_pendingReviewToPublished_clearsRejectedReason() throws Exception {
    String body =
        """
        {"status": "PUBLISHED"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p5.getId())
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PUBLISHED"))
        .andExpect(jsonPath("$.rejectedReason").isEmpty());

    Post updated = postRepository.findById(p5.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(PostStatus.PUBLISHED);
    assertThat(updated.getRejectedReason()).isNull();
  }

  @Test
  @DisplayName("PATCH /admin/posts/status - 검토대기→거부: 사유 포함")
  void changePostStatus_pendingReviewToRejected_withReason_succeeds() throws Exception {
    String body =
        """
        {"status": "REJECTED", "reason": "내용이 너무 짧습니다"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p5.getId())
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJECTED"))
        .andExpect(jsonPath("$.rejectedReason").value("내용이 너무 짧습니다"));

    Post updated = postRepository.findById(p5.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(PostStatus.REJECTED);
    assertThat(updated.getRejectedReason()).isEqualTo("내용이 너무 짧습니다");
  }

  @Test
  @DisplayName("PATCH /admin/posts/status - 거부 사유 없으면 400")
  void changePostStatus_rejectedWithoutReason_returns400() throws Exception {
    String body =
        """
        {"status": "REJECTED"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p5.getId())
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PATCH /admin/posts/status - 임시저장→게시")
  void changePostStatus_draftToPublished_succeeds() throws Exception {
    String body =
        """
        {"status": "PUBLISHED"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p4.getId())
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(p4.getId()))
        .andExpect(jsonPath("$.status").value("PUBLISHED"));

    Post updated = postRepository.findById(p4.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(PostStatus.PUBLISHED);
  }

  @Test
  @DisplayName("PATCH /admin/posts/status - 존재하지 않는 포스트 404")
  void changePostStatus_notFound_returns404() throws Exception {
    String body =
        """
        {"status": "DRAFT"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", 999999L)
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PATCH /admin/posts/status - null 상태값 400")
  void changePostStatus_nullStatus_returns400() throws Exception {
    String body =
        """
        {"status": null}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p1.getId())
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PATCH /admin/posts/status - 유효하지 않은 상태값 400")
  void changePostStatus_invalidStatusValue_returns400() throws Exception {
    String body =
        """
        {"status": "INVALID_STATUS"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p1.getId())
                .with(TestAuth.asAdmin(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PATCH /admin/posts/status - 토큰 없음 401")
  void changePostStatus_noToken_returns401() throws Exception {
    String body =
        """
        {"status": "DRAFT"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("PATCH /admin/posts/status - 일반 유저 토큰 403")
  void changePostStatus_withMemberToken_returns403() throws Exception {
    String body =
        """
        {"status": "DRAFT"}
        """;
    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p1.getId())
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden());
  }

  // ── DELETE /api/blog/admin/posts/{id} ────────────────────────────────────

  @Test
  @DisplayName("DELETE /admin/posts - 의존 데이터 없음 204")
  void forceDeletePost_noDependents_returns204AndRemovesPost() throws Exception {
    // Create a fresh post with no tags, likes, or comments
    Post toDelete =
        postRepository.save(
            Post.builder()
                .userId(OTHER_USER_ID)
                .title("어드민 강제 삭제 대상 포스트")
                .content("어드민이 삭제할 포스트입니다. 의존 데이터가 없습니다.")
                .board("테스트")
                .category("기타")
                .status(PostStatus.DRAFT)
                .generation("12기")
                .build());
    Long toDeleteId = toDelete.getId();

    mvc.perform(
            delete("/api/blog/admin/posts/{id}", toDeleteId).with(TestAuth.asAdmin(MOCK_USER_ID)))
        .andExpect(status().isNoContent());

    assertThat(postRepository.findById(toDeleteId)).isEmpty();
  }

  @Test
  @DisplayName("DELETE /admin/posts - 태그 있는 포스트 204")
  void forceDeletePost_withTags_deleteTagsAndPost() throws Exception {
    Post toDelete =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("태그 있는 포스트 강제 삭제 테스트")
                .content("태그가 있는 포스트를 어드민이 삭제합니다.")
                .board("백엔드")
                .category("JPA")
                .status(PostStatus.PUBLISHED)
                .generation("13기")
                .build());
    postTagRepository.save(new PostTag(toDelete, "JPA"));
    postTagRepository.save(new PostTag(toDelete, "Hibernate"));
    Long toDeleteId = toDelete.getId();

    mvc.perform(
            delete("/api/blog/admin/posts/{id}", toDeleteId).with(TestAuth.asAdmin(MOCK_USER_ID)))
        .andExpect(status().isNoContent());

    assertThat(postRepository.findById(toDeleteId)).isEmpty();
    assertThat(postTagRepository.findByPost(toDelete)).isEmpty();
  }

  @Test
  @DisplayName("DELETE /admin/posts - 존재하지 않는 포스트 404")
  void forceDeletePost_notFound_returns404() throws Exception {
    mvc.perform(delete("/api/blog/admin/posts/{id}", 999999L).with(TestAuth.asAdmin(MOCK_USER_ID)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /admin/posts - 좋아요·댓글 있는 포스트 204")
  void forceDeletePost_withLikesAndComments_returns204() throws Exception {
    Post richPost =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("의존 데이터 총집합 (어드민 강제 삭제)")
                .content("태그, 좋아요, 댓글이 모두 달린 포스트입니다. 어드민이 삭제합니다.")
                .board("백엔드")
                .category("기타")
                .status(PostStatus.PUBLISHED)
                .generation("13기")
                .build());
    postTagRepository.save(new PostTag(richPost, "cascade-admin"));
    postLikeRepository.save(new PostLike(richPost, OTHER_USER_ID));
    commentRepository.save(
        Comment.builder()
            .postId(richPost.getId())
            .userId(OTHER_USER_ID)
            .content("어드민 삭제 시 함께 사라질 댓글입니다.")
            .build());

    mvc.perform(
            delete("/api/blog/admin/posts/{id}", richPost.getId())
                .with(TestAuth.asAdmin(MOCK_USER_ID)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /admin/posts - 토큰 없음 401")
  void forceDeletePost_noToken_returns401() throws Exception {
    mvc.perform(delete("/api/blog/admin/posts/{id}", p1.getId()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("DELETE /admin/posts - 일반 유저 토큰 403")
  void forceDeletePost_withMemberToken_returns403() throws Exception {
    mvc.perform(
            delete("/api/blog/admin/posts/{id}", p1.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isForbidden());
  }
}
