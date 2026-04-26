package com.study.blog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.study.StudyBeApplication;
import com.study.blog.infrastructure.comment.CommentLikeRepository;
import com.study.blog.infrastructure.comment.CommentRepository;
import com.study.blog.infrastructure.post.PostBookmarkRepository;
import com.study.blog.infrastructure.post.PostLikeRepository;
import com.study.blog.infrastructure.post.PostRepository;
import com.study.blog.infrastructure.post.PostTagRepository;
import com.study.blog.domain.comment.Comment;
import com.study.blog.domain.comment.CommentLike;
import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostBookmark;
import com.study.blog.domain.post.PostLike;
import com.study.blog.domain.post.PostStatus;
import com.study.blog.domain.post.PostTag;
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
 * Integration tests for POST API endpoints. Uses H2 in-memory DB with PostgreSQL compatibility
 * mode. Each test runs in its own transaction that is rolled back afterwards.
 *
 * <p>Test data:
 *
 * <ul>
 *   <li>postA: PUBLISHED, 백엔드/CI/CD, 13기, MOCK_USER — has like + bookmark from MOCK_USER
 *   <li>postB: PUBLISHED, AI/LLM, 12기, OTHER_USER — tags: ChatGPT, Python
 *   <li>postC: PUBLISHED, 해커톤/해커톤후기, 13기, OTHER_USER — no tags
 *   <li>postD: DRAFT, 백엔드/DevOps, 13기, MOCK_USER — no likes/bookmarks
 *   <li>postE: PUBLISHED, 백엔드/CI/CD, 13기, MOCK_USER — repost of postA, tag: Docker
 * </ul>
 */
@SpringBootTest(
    classes = StudyBeApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
@org.junit.jupiter.api.Disabled(
    "H2 호환성 이슈로 전체 실패. PostgreSQL 전용 기능(jsonb, ENUM 등) 사용."
        + " Testcontainers(PostgreSQL) 도입 후 재활성화 예정.")
class PostApiTest {

  static final UUID MOCK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Autowired MockMvc mvc;
  @Autowired PostRepository postRepository;
  @Autowired PostTagRepository postTagRepository;
  @Autowired PostLikeRepository postLikeRepository;
  @Autowired PostBookmarkRepository postBookmarkRepository;
  @Autowired CommentRepository commentRepository;
  @Autowired CommentLikeRepository commentLikeRepository;

  Post postA, postB, postC, postD, postE;

  @BeforeEach
  void setUp() {
    // Post A: flagship CI/CD post by MOCK_USER, liked + bookmarked
    postA =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("Spring Boot + GitHub Actions로 CI/CD 파이프라인 구축하기")
                .content(
                    "GitHub Actions와 AWS EC2를 활용해 자동 배포 파이프라인을 구성하는 방법을 소개합니다. "
                        + "Runner 설정부터 Docker 빌드, EC2 SSH 배포까지 전 과정을 다룹니다.")
                .board("백엔드")
                .category("CI/CD")
                .status(PostStatus.PUBLISHED)
                .generation("13기")
                .build());
    postTagRepository.save(new PostTag(postA, "Spring Boot"));
    postTagRepository.save(new PostTag(postA, "GitHub Actions"));
    postTagRepository.save(new PostTag(postA, "AWS EC2"));
    postLikeRepository.save(new PostLike(postA, MOCK_USER_ID));
    postBookmarkRepository.save(new PostBookmark(postA, MOCK_USER_ID));

    // Post B: AI/LLM post by OTHER_USER
    postB =
        postRepository.save(
            Post.builder()
                .userId(OTHER_USER_ID)
                .title("ChatGPT API 활용한 실시간 번역 서비스 구축")
                .content(
                    "OpenAI ChatGPT API를 Python FastAPI로 연동하여 실시간 번역 서비스를 만드는 방법입니다. "
                        + "스트리밍 응답 처리와 에러 핸들링을 포함합니다.")
                .board("AI")
                .category("LLM")
                .status(PostStatus.PUBLISHED)
                .generation("12기")
                .build());
    postTagRepository.save(new PostTag(postB, "ChatGPT"));
    postTagRepository.save(new PostTag(postB, "Python"));

    // Post C: Hackathon review by OTHER_USER (no tags)
    postC =
        postRepository.save(
            Post.builder()
                .userId(OTHER_USER_ID)
                .title("멋쟁이사자처럼 13기 해커톤 48시간 도전기")
                .content("팀원 4명과 함께한 48시간 해커톤 경험과 개발 회고를 공유합니다.")
                .board("해커톤")
                .category("해커톤후기")
                .status(PostStatus.PUBLISHED)
                .generation("13기")
                .build());

    // Post D: DRAFT by MOCK_USER (no likes/bookmarks — safe to delete in tests)
    postD =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("Docker Compose 기반 로컬 개발환경 세팅 가이드")
                .content("Docker Compose로 DB, Redis, 앱 서버를 한번에 올리는 로컬 개발환경 구성 방법입니다.")
                .board("백엔드")
                .category("DevOps")
                .status(PostStatus.DRAFT)
                .generation("13기")
                .build());

    // Post E: PUBLISHED repost of A by MOCK_USER
    postE =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("AWS EC2 CI/CD 자동 배포 파이프라인 심화편")
                .content("이전 CI/CD 글의 심화 내용입니다. Blue-Green 배포 전략을 추가로 다룹니다.")
                .board("백엔드")
                .category("CI/CD")
                .status(PostStatus.PUBLISHED)
                .generation("13기")
                .repostFromId(postA.getId())
                .build());
    postTagRepository.save(new PostTag(postE, "Docker"));
  }

  // ── GET /api/blog/posts ──────────────────────────────────────────────────

  @Test
  void getPosts_returnsOnlyPublished() throws Exception {
    // A, B, C, E are PUBLISHED; D is DRAFT → expect 4
    mvc.perform(get("/api/blog/posts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(4))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.number").value(0));
  }

  @Test
  void getPosts_filterByBoard_returnsMatchingPublishedOnly() throws Exception {
    // A and E are PUBLISHED board=백엔드; D is DRAFT → expect 2
    mvc.perform(get("/api/blog/posts").param("board", "백엔드"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  void getPosts_filterByGeneration_returnsMatching() throws Exception {
    // A, C, E are PUBLISHED 13기; D is DRAFT → expect 3
    mvc.perform(get("/api/blog/posts").param("generation", "13기"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(3));
  }

  @Test
  void getPosts_filterByKeyword_searchesTitleAndContent() throws Exception {
    // "CI/CD" appears in titles of A and E
    mvc.perform(get("/api/blog/posts").param("keyword", "CI/CD"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  void getPosts_filterByKeyword_contentMatch() throws Exception {
    // "Blue-Green" only appears in postE content
    mvc.perform(get("/api/blog/posts").param("keyword", "Blue-Green"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].title").value("AWS EC2 CI/CD 자동 배포 파이프라인 심화편"));
  }

  @Test
  void getPosts_filterByAuthorId_returnsAuthorPublishedPosts() throws Exception {
    // MOCK_USER has A and E published (D is draft)
    mvc.perform(get("/api/blog/posts").param("authorId", MOCK_USER_ID.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  void getPosts_pagination_respectsSizeAndPageParams() throws Exception {
    mvc.perform(get("/api/blog/posts").param("size", "2").param("page", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(4))
        .andExpect(jsonPath("$.totalPages").value(2))
        .andExpect(jsonPath("$.first").value(true));

    mvc.perform(get("/api/blog/posts").param("size", "2").param("page", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  void getPosts_combinedFilters_boardAndGeneration() throws Exception {
    // board=백엔드 AND generation=13기 AND PUBLISHED → A, E
    mvc.perform(get("/api/blog/posts").param("board", "백엔드").param("generation", "13기"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  // ── GET /api/blog/posts/{id} ─────────────────────────────────────────────

  @Test
  void getPost_publishedPost_returnsFullDetails() throws Exception {
    mvc.perform(get("/api/blog/posts/{id}", postA.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(postA.getId()))
        .andExpect(jsonPath("$.title").value("Spring Boot + GitHub Actions로 CI/CD 파이프라인 구축하기"))
        .andExpect(jsonPath("$.status").value("PUBLISHED"))
        .andExpect(jsonPath("$.board").value("백엔드"))
        .andExpect(jsonPath("$.category").value("CI/CD"))
        .andExpect(jsonPath("$.generation").value("13기"))
        .andExpect(jsonPath("$.content").isString())
        .andExpect(jsonPath("$.tags.length()").value(3))
        .andExpect(jsonPath("$.likeCount").value(1))
        .andExpect(jsonPath("$.bookmarkCount").value(1))
        .andExpect(jsonPath("$.liked").value(true))
        .andExpect(jsonPath("$.bookmarked").value(true))
        .andExpect(jsonPath("$.authorId").value(MOCK_USER_ID.toString()))
        .andExpect(jsonPath("$.createdAt").isString())
        .andExpect(jsonPath("$.updatedAt").isString());
  }

  @Test
  void getPost_repostedPost_includesRepostFromId() throws Exception {
    mvc.perform(get("/api/blog/posts/{id}", postE.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.repostFromId").value(postA.getId()))
        .andExpect(jsonPath("$.tags.length()").value(1));
  }

  @Test
  void getPost_noLikeOrBookmark_returnsFalseFlags() throws Exception {
    // postB has no like/bookmark from MOCK_USER
    mvc.perform(get("/api/blog/posts/{id}", postB.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(false))
        .andExpect(jsonPath("$.bookmarked").value(false))
        .andExpect(jsonPath("$.likeCount").value(0))
        .andExpect(jsonPath("$.bookmarkCount").value(0));
  }

  @Test
  void getPost_ownDraft_returnsPost() throws Exception {
    // MOCK_USER requests their own DRAFT → allowed
    mvc.perform(get("/api/blog/posts/{id}", postD.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DRAFT"));
  }

  @Test
  void getPost_othersDraft_returns403() throws Exception {
    // Create a DRAFT owned by OTHER_USER
    Post otherDraft =
        postRepository.save(
            Post.builder()
                .userId(OTHER_USER_ID)
                .title("타인의 임시저장 포스트")
                .content("비공개 임시저장 포스트입니다.")
                .board("AI")
                .category("기초")
                .status(PostStatus.DRAFT)
                .generation("12기")
                .build());

    // MOCK_USER (via MockAuth) tries to read OTHER_USER's DRAFT → 403
    mvc.perform(get("/api/blog/posts/{id}", otherDraft.getId())).andExpect(status().isForbidden());
  }

  @Test
  void getPost_notFound_returns404() throws Exception {
    mvc.perform(get("/api/blog/posts/{id}", 999999L)).andExpect(status().isNotFound());
  }

  // ── POST /api/blog/posts ─────────────────────────────────────────────────

  @Test
  void createPost_publishedWithTags_returns201() throws Exception {
    String body =
        """
        {
          "title": "JPA N+1 문제 완벽 해결 가이드",
          "content": "Fetch Join과 @EntityGraph를 사용해 N+1 문제를 해결하는 방법을 정리했습니다. 각 전략의 장단점도 분석합니다.",
          "board": "백엔드",
          "category": "JPA",
          "status": "PUBLISHED",
          "generation": "13기",
          "tags": ["JPA", "Hibernate", "Performance", "Spring Data"]
        }
        """;

    mvc.perform(post("/api/blog/posts").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("JPA N+1 문제 완벽 해결 가이드"))
        .andExpect(jsonPath("$.status").value("PUBLISHED"))
        .andExpect(jsonPath("$.tags.length()").value(4))
        .andExpect(jsonPath("$.authorId").value(MOCK_USER_ID.toString()))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.liked").value(false))
        .andExpect(jsonPath("$.bookmarked").value(false));
  }

  @Test
  void createPost_asDraft_returns201WithDraftStatus() throws Exception {
    String body =
        """
        {
          "title": "작성 중인 임시 포스트",
          "content": "아직 완성되지 않은 글입니다. 나중에 마저 작성할 예정입니다.",
          "board": "AI",
          "category": "기초",
          "status": "DRAFT",
          "generation": "13기"
        }
        """;

    mvc.perform(post("/api/blog/posts").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.tags.length()").value(0));
  }

  @Test
  void createPost_withRepostFromId_returns201() throws Exception {
    String body =
        String.format(
            """
            {
              "title": "재게시 테스트 포스트",
              "content": "원본 포스트를 참조하는 재게시 글입니다.",
              "board": "백엔드",
              "category": "CI/CD",
              "status": "PUBLISHED",
              "generation": "13기",
              "repostFromId": %d
            }
            """,
            postA.getId());

    mvc.perform(post("/api/blog/posts").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.repostFromId").value(postA.getId()));
  }

  @Test
  void createPost_missingRequiredField_returns400() throws Exception {
    String body =
        """
        {
          "content": "제목 없는 포스트",
          "board": "백엔드",
          "category": "CI/CD",
          "status": "PUBLISHED",
          "generation": "13기"
        }
        """;

    mvc.perform(post("/api/blog/posts").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  // ── PUT /api/blog/posts/{id} ─────────────────────────────────────────────

  @Test
  void updatePost_ownPost_updatesFieldsAndTags() throws Exception {
    String body =
        """
        {
          "title": "[수정] Spring Boot + GitHub Actions CI/CD 완전판",
          "content": "기존 내용에 Blue-Green 배포 전략을 추가로 보완했습니다.",
          "board": "백엔드",
          "category": "CI/CD",
          "status": "PUBLISHED",
          "tags": ["Spring Boot", "CI/CD", "Blue-Green", "AWS"]
        }
        """;

    mvc.perform(
            put("/api/blog/posts/{id}", postA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("[수정] Spring Boot + GitHub Actions CI/CD 완전판"))
        .andExpect(jsonPath("$.tags.length()").value(4));
  }

  @Test
  void updatePost_draftToPublished_changesStatus() throws Exception {
    String body =
        """
        {
          "title": "Docker Compose 기반 로컬 개발환경 세팅 가이드 (완성)",
          "content": "작성을 완료하고 발행합니다.",
          "board": "백엔드",
          "category": "DevOps",
          "status": "PUBLISHED"
        }
        """;

    mvc.perform(
            put("/api/blog/posts/{id}", postD.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PUBLISHED"));
  }

  @Test
  void updatePost_othersPost_returns403() throws Exception {
    String body =
        """
        {
          "title": "무단 수정 시도",
          "content": "타인의 글을 수정하려는 시도입니다.",
          "board": "AI",
          "category": "LLM",
          "status": "PUBLISHED"
        }
        """;

    mvc.perform(
            put("/api/blog/posts/{id}", postB.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  void updatePost_notFound_returns404() throws Exception {
    String body =
        """
        {
          "title": "존재하지 않는 포스트 수정",
          "content": "이 포스트는 없습니다.",
          "board": "백엔드",
          "category": "기타",
          "status": "PUBLISHED"
        }
        """;

    mvc.perform(
            put("/api/blog/posts/{id}", 999999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }

  // ── DELETE /api/blog/posts/{id} ──────────────────────────────────────────

  @Test
  void deletePost_ownDraftWithNoDependents_returns204() throws Exception {
    // postD: MOCK_USER's DRAFT with no likes/bookmarks/comments
    mvc.perform(delete("/api/blog/posts/{id}", postD.getId())).andExpect(status().isNoContent());
  }

  @Test
  void deletePost_othersPost_returns403() throws Exception {
    mvc.perform(delete("/api/blog/posts/{id}", postB.getId())).andExpect(status().isForbidden());
  }

  @Test
  void deletePost_notFound_returns404() throws Exception {
    mvc.perform(delete("/api/blog/posts/{id}", 999999L)).andExpect(status().isNotFound());
  }

  // ── POST /api/blog/posts/{id}/like ───────────────────────────────────────

  @Test
  void toggleLike_noExistingLike_returnsLikedTrue() throws Exception {
    // postB has no like from MOCK_USER
    mvc.perform(post("/api/blog/posts/{id}/like", postB.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(true));
  }

  @Test
  void toggleLike_existingLike_returnsLikedFalse() throws Exception {
    // postA already liked by MOCK_USER in setUp
    mvc.perform(post("/api/blog/posts/{id}/like", postA.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(false));
  }

  @Test
  void toggleLike_twice_backToLiked() throws Exception {
    // Like postC (not liked), then like again → liked=true
    mvc.perform(post("/api/blog/posts/{id}/like", postC.getId()))
        .andExpect(jsonPath("$.liked").value(true));

    mvc.perform(post("/api/blog/posts/{id}/like", postC.getId()))
        .andExpect(jsonPath("$.liked").value(false));

    mvc.perform(post("/api/blog/posts/{id}/like", postC.getId()))
        .andExpect(jsonPath("$.liked").value(true));
  }

  // ── POST /api/blog/posts/{id}/bookmark ──────────────────────────────────

  @Test
  void toggleBookmark_noExistingBookmark_returnsBookmarkedTrue() throws Exception {
    mvc.perform(post("/api/blog/posts/{id}/bookmark", postB.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookmarked").value(true));
  }

  @Test
  void toggleBookmark_existingBookmark_returnsBookmarkedFalse() throws Exception {
    // postA already bookmarked by MOCK_USER in setUp
    mvc.perform(post("/api/blog/posts/{id}/bookmark", postA.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookmarked").value(false));
  }

  @Test
  void likeCount_reflectsMultipleUsers() throws Exception {
    // Add like from OTHER_USER manually, verify likeCount=2
    postLikeRepository.save(new PostLike(postA, OTHER_USER_ID));

    mvc.perform(get("/api/blog/posts/{id}", postA.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.likeCount").value(2));
  }

  // ── CASCADE DELETE via @OnDelete ─────────────────────────────────────────
  // A 204 response proves no FK-constraint violation occurred.
  // If cascade were missing, post_likes/post_bookmarks/comments FK would raise an error → 500.

  @Test
  void deletePost_withLike_returns204() throws Exception {
    // postA is owned by MOCK_USER and already has a like from MOCK_USER (setUp)
    // Without ON DELETE CASCADE this would fail with FK constraint violation
    mvc.perform(delete("/api/blog/posts/{id}", postA.getId())).andExpect(status().isNoContent());
  }

  @Test
  void deletePost_withBookmark_returns204() throws Exception {
    // postA is owned by MOCK_USER and already has a bookmark from MOCK_USER (setUp)
    mvc.perform(delete("/api/blog/posts/{id}", postA.getId())).andExpect(status().isNoContent());
  }

  @Test
  void deletePost_withAllDependents_returns204() throws Exception {
    // Create a MOCK_USER post with every type of dependent attached
    Post rich =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("의존 데이터 총집합 포스트")
                .content("태그, 좋아요, 북마크, 댓글, 댓글 좋아요가 모두 달린 포스트입니다.")
                .board("백엔드")
                .category("기타")
                .status(PostStatus.PUBLISHED)
                .generation("13기")
                .build());
    postTagRepository.save(new PostTag(rich, "cascade"));
    postLikeRepository.save(new PostLike(rich, OTHER_USER_ID));
    postBookmarkRepository.save(new PostBookmark(rich, OTHER_USER_ID));
    Comment comment =
        commentRepository.save(
            Comment.builder()
                .postId(rich.getId())
                .userId(OTHER_USER_ID)
                .content("삭제될 댓글입니다.")
                .build());
    commentLikeRepository.save(new CommentLike(comment, MOCK_USER_ID));

    // If any FK cascade is missing the delete raises a DataIntegrityViolationException → 500
    Long richId = rich.getId();
    mvc.perform(delete("/api/blog/posts/{id}", richId)).andExpect(status().isNoContent());

    assertThat(postRepository.findById(richId)).isEmpty();
    assertThat(postTagRepository.findByPost(rich)).isEmpty();
    assertThat(commentRepository.findById(comment.getId())).isEmpty();
  }
}
