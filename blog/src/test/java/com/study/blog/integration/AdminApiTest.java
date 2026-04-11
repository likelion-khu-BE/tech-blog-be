package com.study.blog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.study.blog.BlogTestApplication;
import com.study.blog.comment.Comment;
import com.study.blog.comment.CommentRepository;
import com.study.blog.post.Post;
import com.study.blog.post.PostLike;
import com.study.blog.post.PostLikeRepository;
import com.study.blog.post.PostRepository;
import com.study.blog.post.PostStatus;
import com.study.blog.post.PostTag;
import com.study.blog.post.PostTagRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    classes = BlogTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class AdminApiTest {

  static final UUID MOCK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Autowired MockMvc mvc;
  @Autowired PostRepository postRepository;
  @Autowired PostTagRepository postTagRepository;
  @Autowired PostLikeRepository postLikeRepository;
  @Autowired CommentRepository commentRepository;

  Post p1, p2, p3, p4;

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
  void getStats_returnsCorrectCounts() throws Exception {
    mvc.perform(get("/api/blog/admin/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalPosts").value(4))
        .andExpect(jsonPath("$.data.publishedPosts").value(3))
        .andExpect(jsonPath("$.data.draftPosts").value(1))
        .andExpect(jsonPath("$.data.totalComments").value(3));
  }

  @Test
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

    mvc.perform(get("/api/blog/admin/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalPosts").value(5))
        .andExpect(jsonPath("$.data.draftPosts").value(2))
        .andExpect(jsonPath("$.data.publishedPosts").value(3));
  }

  // ── GET /api/blog/admin/posts ────────────────────────────────────────────

  @Test
  void getAllPosts_includesDrafts() throws Exception {
    mvc.perform(get("/api/blog/admin/posts"))
        .andExpect(status().isOk())
        // Admin sees all 4 posts (including DRAFT)
        .andExpect(jsonPath("$.data.totalElements").value(4))
        .andExpect(jsonPath("$.data.content").isArray());
  }

  @Test
  void getAllPosts_pagination_defaultPage20() throws Exception {
    // Default page size is 20 → all 4 fit on first page
    mvc.perform(get("/api/blog/admin/posts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.size").value(20))
        .andExpect(jsonPath("$.data.numberOfElements").value(4))
        .andExpect(jsonPath("$.data.totalPages").value(1));
  }

  @Test
  void getAllPosts_customPageSize_paginatesCorrectly() throws Exception {
    mvc.perform(get("/api/blog/admin/posts").param("size", "2").param("page", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalElements").value(4))
        .andExpect(jsonPath("$.data.content.length()").value(2))
        .andExpect(jsonPath("$.data.totalPages").value(2));
  }

  @Test
  void getAllPosts_postFields_includeTagsAndLikeCount() throws Exception {
    // Admin post list should include tags and likeCount
    // p1 is the most recent PUBLISHED post with tags
    mvc.perform(get("/api/blog/admin/posts").param("size", "10").param("page", "0"))
        .andExpect(status().isOk())
        // p4 (DRAFT) is most recent, then p3, p2, p1 — sorted by createdAt desc
        .andExpect(jsonPath("$.data.content[0].status").value("DRAFT")); // p4 is most recent
  }

  // ── PATCH /api/blog/admin/posts/{id}/status ──────────────────────────────

  @Test
  void changePostStatus_publishedToDraft_succeeds() throws Exception {
    String body = """
        {"status": "DRAFT"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk());

    // Verify status actually changed in DB
    Post updated = postRepository.findById(p1.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(PostStatus.DRAFT);
  }

  @Test
  void changePostStatus_draftToPublished_succeeds() throws Exception {
    String body = """
        {"status": "PUBLISHED"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p4.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk());

    Post updated = postRepository.findById(p4.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(PostStatus.PUBLISHED);
  }

  @Test
  void changePostStatus_notFound_returns404() throws Exception {
    String body = """
        {"status": "DRAFT"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", 999999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  void changePostStatus_nullStatus_returns400() throws Exception {
    String body = """
        {"status": null}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void changePostStatus_invalidStatusValue_returns400() throws Exception {
    String body = """
        {"status": "INVALID_STATUS"}
        """;

    mvc.perform(
            patch("/api/blog/admin/posts/{id}/status", p1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // ── DELETE /api/blog/admin/posts/{id} ────────────────────────────────────

  @Test
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

    mvc.perform(delete("/api/blog/admin/posts/{id}", toDeleteId))
        .andExpect(status().isNoContent());

    assertThat(postRepository.findById(toDeleteId)).isEmpty();
  }

  @Test
  void forceDeletePost_withTags_deleteTagsAndPost() throws Exception {
    // p2 has no likes or comments — only need to delete it (tags are handled by service)
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

    mvc.perform(delete("/api/blog/admin/posts/{id}", toDeleteId))
        .andExpect(status().isNoContent());

    assertThat(postRepository.findById(toDeleteId)).isEmpty();
    assertThat(postTagRepository.findByPost(toDelete)).isEmpty();
  }

  @Test
  void forceDeletePost_notFound_returns404() throws Exception {
    mvc.perform(delete("/api/blog/admin/posts/{id}", 999999L)).andExpect(status().isNotFound());
  }

  @Test
  void forceDeletePost_withLikesAndComments_returns204() throws Exception {
    // Create a post with a tag, a like, and a comment to cover all FK cascade paths
    // Without ON DELETE CASCADE any of these would raise DataIntegrityViolationException → 500
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

    mvc.perform(delete("/api/blog/admin/posts/{id}", richPost.getId()))
        .andExpect(status().isNoContent());
  }
}
