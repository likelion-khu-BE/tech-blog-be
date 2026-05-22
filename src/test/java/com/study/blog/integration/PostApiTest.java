package com.study.blog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.study.StudyBeApplication;
import com.study.auth.domain.User;
import com.study.auth.infrastructure.UserRepository;
import com.study.blog.domain.comment.Comment;
import com.study.blog.domain.comment.CommentLike;
import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostBookmark;
import com.study.blog.domain.post.PostLike;
import com.study.blog.domain.post.PostStatus;
import com.study.blog.domain.post.PostTag;
import com.study.blog.infrastructure.comment.CommentLikeRepository;
import com.study.blog.infrastructure.comment.CommentRepository;
import com.study.blog.infrastructure.post.PostBookmarkRepository;
import com.study.blog.infrastructure.post.PostLikeRepository;
import com.study.blog.infrastructure.post.PostRepository;
import com.study.blog.infrastructure.post.PostTagRepository;
import com.study.config.TestcontainersConfig;
import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.generation.GenerationRole;
import com.study.profile.domain.generation.MemberGeneration;
import com.study.profile.domain.member.Member;
import com.study.profile.domain.member.SessionType;
import com.study.profile.infrastructure.GenerationRepository;
import com.study.profile.infrastructure.MemberGenerationRepository;
import com.study.profile.infrastructure.MemberRepository;
import java.time.LocalDate;
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
 * Integration tests for POST API endpoints. Uses PostgreSQL Testcontainers. Each test runs in its
 * own transaction that is rolled back afterwards.
 *
 * <p>Test data:
 *
 * <ul>
 *   <li>postA: PUBLISHED, 백엔드/CI/CD, 13기, MOCK_USER — has like + bookmark from MOCK_USER
 *   <li>postB: PUBLISHED, AI/LLM, 12기, OTHER_USER — tags: ChatGPT, Python
 *   <li>postC: PUBLISHED, 해커톤/해커톤후기, 13기, OTHER_USER — no tags
 *   <li>postD: DRAFT, 백엔드/DevOps, 13기, MOCK_USER — no likes/bookmarks
 *   <li>postE: PUBLISHED, 백엔드/CI/CD, 13기, MOCK_USER — reply to postA, tag: Docker
 * </ul>
 */
@SpringBootTest(
    classes = StudyBeApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfig.class)
@DisplayName("포스트 API")
class PostApiTest {

  static final Long MOCK_USER_ID = 1L;
  static final Long OTHER_USER_ID = 2L;

  @Autowired MockMvc mvc;
  @Autowired PostRepository postRepository;
  @Autowired PostTagRepository postTagRepository;
  @Autowired PostLikeRepository postLikeRepository;
  @Autowired PostBookmarkRepository postBookmarkRepository;
  @Autowired CommentRepository commentRepository;
  @Autowired CommentLikeRepository commentLikeRepository;
  @Autowired UserRepository userRepository;
  @Autowired MemberRepository memberRepository;
  @Autowired GenerationRepository generationRepository;
  @Autowired MemberGenerationRepository memberGenerationRepository;

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

    // Post E: PUBLISHED reply to A by MOCK_USER
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
                .replyToId(postA.getId())
                .build());
    postTagRepository.save(new PostTag(postE, "Docker"));
  }

  // ── GET /api/blog/posts ──────────────────────────────────────────────────

  @Test
  @DisplayName("GET /posts - 게시된 포스트만 반환")
  void getPosts_returnsOnlyPublished() throws Exception {
    // A, B, C, E are PUBLISHED; D is DRAFT → expect 4
    mvc.perform(get("/api/blog/posts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(4))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.number").value(0));
  }

  @Test
  @DisplayName("GET /posts - 게시판 필터")
  void getPosts_filterByBoard_returnsMatchingPublishedOnly() throws Exception {
    // A and E are PUBLISHED board=백엔드; D is DRAFT → expect 2
    mvc.perform(get("/api/blog/posts").param("board", "백엔드"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  @DisplayName("GET /posts - 기수 필터")
  void getPosts_filterByGeneration_returnsMatching() throws Exception {
    // A, C, E are PUBLISHED 13기; D is DRAFT → expect 3
    mvc.perform(get("/api/blog/posts").param("generation", "13기"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(3));
  }

  @Test
  @DisplayName("GET /posts - 키워드 검색 (제목+내용)")
  void getPosts_filterByKeyword_searchesTitleAndContent() throws Exception {
    // "CI/CD" appears in titles of A and E
    mvc.perform(get("/api/blog/posts").param("keyword", "CI/CD"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  @DisplayName("GET /posts - 키워드 검색 내용 매칭")
  void getPosts_filterByKeyword_contentMatch() throws Exception {
    // "Blue-Green" only appears in postE content
    mvc.perform(get("/api/blog/posts").param("keyword", "Blue-Green"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].title").value("AWS EC2 CI/CD 자동 배포 파이프라인 심화편"));
  }

  @Test
  @DisplayName("GET /posts - 작성자 ID 필터")
  void getPosts_filterByAuthorId_returnsAuthorPublishedPosts() throws Exception {
    // MOCK_USER has A and E published (D is draft)
    mvc.perform(get("/api/blog/posts").param("authorId", String.valueOf(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  @DisplayName("GET /posts - 페이지네이션")
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
  @DisplayName("GET /posts - 게시판+기수 복합 필터")
  void getPosts_combinedFilters_boardAndGeneration() throws Exception {
    // board=백엔드 AND generation=13기 AND PUBLISHED → A, E
    mvc.perform(get("/api/blog/posts").param("board", "백엔드").param("generation", "13기"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  @DisplayName("GET /posts - 카테고리 필터")
  void getPosts_filterByCategory_returnsMatching() throws Exception {
    // postA and postE share category=CI/CD (both PUBLISHED); D is DRAFT → excluded
    mvc.perform(get("/api/blog/posts").param("category", "CI/CD"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  // ── GET /api/blog/posts/{id} ─────────────────────────────────────────────

  @Test
  @DisplayName("GET /posts/{id} - 게시된 포스트 상세 필드")
  void getPost_publishedPost_returnsFullDetails() throws Exception {
    mvc.perform(get("/api/blog/posts/{id}", postA.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
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
        .andExpect(jsonPath("$.authorId").value(MOCK_USER_ID))
        .andExpect(jsonPath("$.createdAt").isString())
        .andExpect(jsonPath("$.updatedAt").isString());
  }

  @Test
  @DisplayName("GET /posts/{id} - 답글 포스트 replyToId 포함")
  void getPost_replyPost_includesReplyToId() throws Exception {
    mvc.perform(get("/api/blog/posts/{id}", postE.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.replyToId").value(postA.getId()))
        .andExpect(jsonPath("$.tags.length()").value(1));
  }

  @Test
  @DisplayName("GET /posts/{id} - 좋아요·북마크 없으면 false")
  void getPost_noLikeOrBookmark_returnsFalseFlags() throws Exception {
    // postB has no like/bookmark from MOCK_USER
    mvc.perform(get("/api/blog/posts/{id}", postB.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(false))
        .andExpect(jsonPath("$.bookmarked").value(false))
        .andExpect(jsonPath("$.likeCount").value(0))
        .andExpect(jsonPath("$.bookmarkCount").value(0));
  }

  @Test
  @DisplayName("GET /posts/{id} - 본인 임시저장 조회 가능")
  void getPost_ownDraft_returnsPost() throws Exception {
    // MOCK_USER requests their own DRAFT → allowed
    mvc.perform(get("/api/blog/posts/{id}", postD.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DRAFT"));
  }

  @Test
  @DisplayName("GET /posts/{id} - 타인 임시저장 403")
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
    mvc.perform(
            get("/api/blog/posts/{id}", otherDraft.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /posts/{id} - 존재하지 않는 포스트 404")
  void getPost_notFound_returns404() throws Exception {
    mvc.perform(get("/api/blog/posts/{id}", 999999L)).andExpect(status().isNotFound());
  }

  // ── POST /api/blog/posts ─────────────────────────────────────────────────

  @Test
  @DisplayName("POST /posts - 태그 포함 임시저장 201")
  void createPost_withTags_returnsDraft201() throws Exception {
    String body =
        """
        {
          "title": "JPA N+1 문제 완벽 해결 가이드",
          "content": "Fetch Join과 @EntityGraph를 사용해 N+1 문제를 해결하는 방법을 정리했습니다. 각 전략의 장단점도 분석합니다.",
          "board": "백엔드",
          "category": "JPA",
          "generation": "13기",
          "tags": ["JPA", "Hibernate", "Performance", "Spring Data"]
        }
        """;

    mvc.perform(
            post("/api/blog/posts")
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("JPA N+1 문제 완벽 해결 가이드"))
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.tags.length()").value(4))
        .andExpect(jsonPath("$.authorId").value(MOCK_USER_ID))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.liked").value(false))
        .andExpect(jsonPath("$.bookmarked").value(false));
  }

  @Test
  @DisplayName("POST /posts - 임시저장 201")
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

    mvc.perform(
            post("/api/blog/posts")
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.tags.length()").value(0));
  }

  @Test
  @DisplayName("POST /posts - 답글 작성 201")
  void createPost_withReplyToId_returns201() throws Exception {
    String body =
        String.format(
            """
            {
              "title": "답글 테스트 포스트",
              "content": "원글을 참조하는 답글입니다.",
              "board": "백엔드",
              "category": "CI/CD",
              "status": "PUBLISHED",
              "generation": "13기",
              "replyToId": %d
            }
            """,
            postA.getId());

    mvc.perform(
            post("/api/blog/posts")
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.replyToId").value(postA.getId()));
  }

  @Test
  @DisplayName("POST /posts - 필수 필드 누락 400")
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

    mvc.perform(
            post("/api/blog/posts")
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /posts - 비인증 401")
  void createPost_unauthenticated_returns401() throws Exception {
    String body =
        """
        {
          "title": "제목", "content": "내용", "board": "백엔드",
          "category": "CI/CD", "status": "PUBLISHED", "generation": "13기"
        }
        """;
    mvc.perform(post("/api/blog/posts").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnauthorized());
  }

  // ── PUT /api/blog/posts/{id} ─────────────────────────────────────────────

  @Test
  @DisplayName("PUT /posts/{id} - 본인 포스트 수정")
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
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("[수정] Spring Boot + GitHub Actions CI/CD 완전판"))
        .andExpect(jsonPath("$.tags.length()").value(4));
  }

  @Test
  @DisplayName("PUT /posts/{id} - status 필드 무시, 기존 상태 유지")
  void updatePost_statusIgnored_retainsExistingStatus() throws Exception {
    String body =
        """
        {
          "title": "Docker Compose 기반 로컬 개발환경 세팅 가이드 (완성)",
          "content": "작성을 완료하고 발행합니다.",
          "board": "백엔드",
          "category": "DevOps"
        }
        """;

    mvc.perform(
            put("/api/blog/posts/{id}", postD.getId())
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DRAFT"));
  }

  @Test
  @DisplayName("PUT /posts/{id} - 타인 포스트 403")
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
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PUT /posts/{id} - 존재하지 않는 포스트 404")
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
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PUT /posts/{id} - 비인증 401")
  void updatePost_unauthenticated_returns401() throws Exception {
    String body =
        """
        {
          "title": "제목", "content": "내용", "board": "백엔드",
          "category": "CI/CD", "status": "PUBLISHED"
        }
        """;
    mvc.perform(
            put("/api/blog/posts/{id}", postA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isUnauthorized());
  }

  // ── DELETE /api/blog/posts/{id} ──────────────────────────────────────────

  @Test
  @DisplayName("DELETE /posts/{id} - 본인 임시저장 204")
  void deletePost_ownDraftWithNoDependents_returns204() throws Exception {
    // postD: MOCK_USER's DRAFT with no likes/bookmarks/comments
    mvc.perform(delete("/api/blog/posts/{id}", postD.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /posts/{id} - 타인 포스트 403")
  void deletePost_othersPost_returns403() throws Exception {
    mvc.perform(delete("/api/blog/posts/{id}", postB.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("DELETE /posts/{id} - 존재하지 않는 포스트 404")
  void deletePost_notFound_returns404() throws Exception {
    mvc.perform(delete("/api/blog/posts/{id}", 999999L).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /posts/{id} - 비인증 401")
  void deletePost_unauthenticated_returns401() throws Exception {
    mvc.perform(delete("/api/blog/posts/{id}", postD.getId())).andExpect(status().isUnauthorized());
  }

  // ── POST /api/blog/posts/{id}/like ───────────────────────────────────────

  @Test
  @DisplayName("POST /posts/like - 좋아요 없는 상태 liked=true")
  void toggleLike_noExistingLike_returnsLikedTrue() throws Exception {
    // postB has no like from MOCK_USER
    mvc.perform(
            post("/api/blog/posts/{id}/like", postB.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(true));
  }

  @Test
  @DisplayName("POST /posts/like - 좋아요 있는 상태 liked=false")
  void toggleLike_existingLike_returnsLikedFalse() throws Exception {
    // postA already liked by MOCK_USER in setUp
    mvc.perform(
            post("/api/blog/posts/{id}/like", postA.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(false));
  }

  @Test
  @DisplayName("POST /posts/like - 두 번 토글 후 liked=true")
  void toggleLike_twice_backToLiked() throws Exception {
    // Like postC (not liked), then like again → liked=true
    mvc.perform(
            post("/api/blog/posts/{id}/like", postC.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(jsonPath("$.liked").value(true));

    mvc.perform(
            post("/api/blog/posts/{id}/like", postC.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(jsonPath("$.liked").value(false));

    mvc.perform(
            post("/api/blog/posts/{id}/like", postC.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(jsonPath("$.liked").value(true));
  }

  @Test
  @DisplayName("POST /posts/like - 비인증 401")
  void toggleLike_unauthenticated_returns401() throws Exception {
    mvc.perform(post("/api/blog/posts/{id}/like", postA.getId()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /posts/like - 존재하지 않는 포스트 404")
  void toggleLike_notFound_returns404() throws Exception {
    mvc.perform(post("/api/blog/posts/{id}/like", 999999L).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNotFound());
  }

  // ── POST /api/blog/posts/{id}/bookmark ──────────────────────────────────

  @Test
  @DisplayName("POST /posts/bookmark - 북마크 없는 상태 bookmarked=true")
  void toggleBookmark_noExistingBookmark_returnsBookmarkedTrue() throws Exception {
    mvc.perform(
            post("/api/blog/posts/{id}/bookmark", postB.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookmarked").value(true));
  }

  @Test
  @DisplayName("POST /posts/bookmark - 북마크 있는 상태 bookmarked=false")
  void toggleBookmark_existingBookmark_returnsBookmarkedFalse() throws Exception {
    // postA already bookmarked by MOCK_USER in setUp
    mvc.perform(
            post("/api/blog/posts/{id}/bookmark", postA.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookmarked").value(false));
  }

  @Test
  @DisplayName("POST /posts/bookmark - 비인증 401")
  void toggleBookmark_unauthenticated_returns401() throws Exception {
    mvc.perform(post("/api/blog/posts/{id}/bookmark", postA.getId()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /posts/bookmark - 존재하지 않는 포스트 404")
  void toggleBookmark_notFound_returns404() throws Exception {
    mvc.perform(
            post("/api/blog/posts/{id}/bookmark", 999999L).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /posts/bookmark - DRAFT 포스트 400")
  void toggleBookmark_draftPost_returns400() throws Exception {
    mvc.perform(
            post("/api/blog/posts/{id}/bookmark", postD.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("포스트 좋아요 수 - 여러 사용자 좋아요 집계")
  void likeCount_reflectsMultipleUsers() throws Exception {
    // Add like from OTHER_USER manually, verify likeCount=2
    postLikeRepository.save(new PostLike(postA, OTHER_USER_ID));

    mvc.perform(get("/api/blog/posts/{id}", postA.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.likeCount").value(2));
  }

  // ── CASCADE DELETE via @OnDelete ─────────────────────────────────────────
  // A 204 response proves no FK-constraint violation occurred.
  // If cascade were missing, post_likes/post_bookmarks/comments FK would raise an error → 500.

  @Test
  @DisplayName("DELETE /posts/{id} (CASCADE) - 좋아요 있는 포스트 204")
  void deletePost_withLike_returns204() throws Exception {
    // postA is owned by MOCK_USER and already has a like from MOCK_USER (setUp)
    // Without ON DELETE CASCADE this would fail with FK constraint violation
    mvc.perform(delete("/api/blog/posts/{id}", postA.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /posts/{id} (CASCADE) - 북마크 있는 포스트 204")
  void deletePost_withBookmark_returns204() throws Exception {
    // postA is owned by MOCK_USER and already has a bookmark from MOCK_USER (setUp)
    mvc.perform(delete("/api/blog/posts/{id}", postA.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /posts/{id} (CASCADE) - 모든 의존 데이터 있는 포스트 204")
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
    mvc.perform(delete("/api/blog/posts/{id}", richId).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNoContent());

    assertThat(postRepository.findById(richId)).isEmpty();
    assertThat(postTagRepository.findByPost(rich)).isEmpty();
  }

  // ── GET /api/blog/posts/bookmarks ───────────────────────────────────────

  @Test
  @DisplayName("GET /posts/bookmarks - 비인증 요청 401")
  void getBookmarkedPosts_unauthenticated_returns401() throws Exception {
    mvc.perform(get("/api/blog/posts/bookmarks")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /posts/bookmarks - 인증된 유저 본인 북마크만 반환")
  void getBookmarkedPosts_authenticated_returnsOnlyCallerBookmarks() throws Exception {
    // setUp: MOCK_USER → postA 북마크, OTHER_USER 북마크 없음
    mvc.perform(get("/api/blog/posts/bookmarks").with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].id").value(postA.getId()));
  }

  @Test
  @DisplayName("GET /posts/bookmarks - 다른 유저의 북마크는 포함되지 않음")
  void getBookmarkedPosts_authenticated_doesNotIncludeOtherUsersBookmarks() throws Exception {
    // OTHER_USER has no bookmarks → empty page
    mvc.perform(get("/api/blog/posts/bookmarks").with(TestAuth.asMember(OTHER_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  @DisplayName("GET /posts/bookmarks - 북마크된 DRAFT 포스트는 목록에서 제외")
  void getBookmarkedPosts_draftBookmarked_notIncluded() throws Exception {
    // toggleBookmark API로는 DRAFT에 북마크 불가 → 직접 저장
    postBookmarkRepository.save(new PostBookmark(postD, MOCK_USER_ID));

    mvc.perform(get("/api/blog/posts/bookmarks").with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1)) // postA만 (postD 제외)
        .andExpect(jsonPath("$.content[0].id").value(postA.getId()));
  }

  // ── authorName 표시 ──────────────────────────────────────────────────────

  @Test
  @DisplayName("GET /posts/{id} - Member가 있으면 authorName이 반환된다")
  void getPost_withMember_returnsAuthorName() throws Exception {
    User user = userRepository.save(User.create("author-detail@test.com", "hash"));
    memberRepository.save(
        Member.create(user, "홍길동", SessionType.backend, null, null, null, null, null, null));

    Post post =
        postRepository.save(
            Post.builder()
                .userId(user.getId())
                .title("작성자 이름 상세 테스트")
                .content("내용")
                .board("백엔드")
                .category("CI/CD")
                .status(PostStatus.PUBLISHED)
                .build());

    mvc.perform(get("/api/blog/posts/{id}", post.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authorName").value("홍길동"));
  }

  @Test
  @DisplayName("GET /posts - Member가 있으면 목록에서 authorName이 반환된다")
  void getPosts_withMember_returnsAuthorNameInList() throws Exception {
    User user = userRepository.save(User.create("author-list@test.com", "hash"));
    memberRepository.save(
        Member.create(user, "김철수", SessionType.backend, null, null, null, null, null, null));

    Post post =
        postRepository.save(
            Post.builder()
                .userId(user.getId())
                .title("목록 작성자 이름 테스트")
                .content("내용")
                .board("백엔드")
                .category("CI/CD")
                .status(PostStatus.PUBLISHED)
                .build());

    mvc.perform(get("/api/blog/posts").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == " + post.getId() + ")].authorName").value("김철수"));
  }

  @Test
  @DisplayName("GET /posts/{id} - Member가 없으면 authorName이 null이다")
  void getPost_withoutMember_authorNameIsNull() throws Exception {
    mvc.perform(get("/api/blog/posts/{id}", postA.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authorName").doesNotExist());
  }

  // ── replyToTitle 표시 ────────────────────────────────────────────────────

  @Test
  @DisplayName("GET /posts - 답글 포스트는 목록에서 replyToTitle을 포함한다")
  void getPosts_replyPost_includesReplyToTitle() throws Exception {
    mvc.perform(get("/api/blog/posts").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.content[?(@.id == " + postE.getId() + ")].replyToTitle")
                .value(postA.getTitle()));
  }

  @Test
  @DisplayName("GET /posts - 답글이 아닌 포스트는 replyToTitle이 null이다")
  void getPosts_normalPost_replyToTitleIsNull() throws Exception {
    mvc.perform(get("/api/blog/posts").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.content[?(@.id == " + postA.getId() + ")].replyToTitle")
                .value((Object) null));
  }

  // ── generation 자동 주입 ─────────────────────────────────────────────────

  @Test
  @DisplayName("POST /posts - Member와 Generation이 있으면 generation이 자동 저장된다")
  void createPost_withMemberAndGeneration_generationAutoInjected() throws Exception {
    User user = userRepository.save(User.create("gen-test@test.com", "hash"));
    Member member =
        memberRepository.save(
            Member.create(user, "기수테스터", SessionType.backend, null, null, null, null, null, null));
    Generation gen =
        generationRepository.save(Generation.create(17, LocalDate.of(2024, 3, 1), null, true));
    memberGenerationRepository.save(MemberGeneration.create(member, gen, GenerationRole.member));

    String body =
        """
        {"title":"기수자동주입테스트","content":"내용","board":"백엔드","category":"CI/CD"}
        """;

    mvc.perform(
            post("/api/blog/posts")
                .with(TestAuth.asMember(user.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.generation").value("17기"));
  }

  @Test
  @DisplayName("POST /posts - Member가 없으면 generation이 null이다")
  void createPost_withoutMember_generationIsNull() throws Exception {
    String body =
        """
        {"title":"기수없음테스트","content":"내용","board":"백엔드","category":"CI/CD"}
        """;

    mvc.perform(
            post("/api/blog/posts")
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.generation").doesNotExist());
  }

  // ── POST /api/blog/posts/{id}/submit ────────────────────────────────────

  @Test
  @DisplayName("POST /posts/{id}/submit - DRAFT → PENDING_REVIEW 성공")
  void submitPost_draft_returnsPendingReview() throws Exception {
    mvc.perform(
            post("/api/blog/posts/{id}/submit", postD.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING_REVIEW"));

    Post updated = postRepository.findById(postD.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(PostStatus.PENDING_REVIEW);
  }

  @Test
  @DisplayName("POST /posts/{id}/submit - PUBLISHED 포스트 → 400")
  void submitPost_published_returns400() throws Exception {
    mvc.perform(
            post("/api/blog/posts/{id}/submit", postA.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /posts/{id}/submit - 타인 포스트 → 403")
  void submitPost_othersPost_returns403() throws Exception {
    mvc.perform(
            post("/api/blog/posts/{id}/submit", postD.getId())
                .with(TestAuth.asMember(OTHER_USER_ID)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /posts/{id}/submit - 비인증 → 401")
  void submitPost_unauthenticated_returns401() throws Exception {
    mvc.perform(post("/api/blog/posts/{id}/submit", postD.getId()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /posts/{id}/submit - PENDING_REVIEW 포스트 재제출 → 400")
  void submitPost_pendingReview_returns400() throws Exception {
    // Submit once first
    mvc.perform(
            post("/api/blog/posts/{id}/submit", postD.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk());

    // Submit again → INVALID_STATUS_TRANSITION
    mvc.perform(
            post("/api/blog/posts/{id}/submit", postD.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isBadRequest());
  }

  // ── GET /api/blog/posts/me ───────────────────────────────────────────────

  @Test
  @DisplayName("GET /posts/me - 본인 포스트 전체 조회 (DRAFT 포함)")
  void getMyPosts_returnsAllOwnPosts() throws Exception {
    // MOCK_USER: postA (PUBLISHED), postD (DRAFT), postE (PUBLISHED)
    mvc.perform(get("/api/blog/posts/me").with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(3));
  }

  @Test
  @DisplayName("GET /posts/me?status=DRAFT - DRAFT 포스트만 조회")
  void getMyPosts_filterByDraft_returnsDraftOnly() throws Exception {
    mvc.perform(
            get("/api/blog/posts/me")
                .with(TestAuth.asMember(MOCK_USER_ID))
                .param("status", "DRAFT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].status").value("DRAFT"));
  }

  @Test
  @DisplayName("GET /posts/me?status=PENDING_REVIEW - 검토 대기 포스트 조회")
  void getMyPosts_filterByPendingReview_returnsPendingReviewOnly() throws Exception {
    // Submit postD to PENDING_REVIEW first
    mvc.perform(
        post("/api/blog/posts/{id}/submit", postD.getId())
            .with(TestAuth.asMember(MOCK_USER_ID)));

    mvc.perform(
            get("/api/blog/posts/me")
                .with(TestAuth.asMember(MOCK_USER_ID))
                .param("status", "PENDING_REVIEW"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].status").value("PENDING_REVIEW"));
  }

  @Test
  @DisplayName("GET /posts/me - 비인증 → 401")
  void getMyPosts_unauthenticated_returns401() throws Exception {
    mvc.perform(get("/api/blog/posts/me")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /posts/me - 타인 포스트는 포함되지 않음")
  void getMyPosts_doesNotIncludeOtherUsersPosts() throws Exception {
    // OTHER_USER: postB (PUBLISHED), postC (PUBLISHED)
    mvc.perform(get("/api/blog/posts/me").with(TestAuth.asMember(OTHER_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  // ── GET /posts/{id} - PENDING_REVIEW / REJECTED 가시성 ──────────────────

  @Test
  @DisplayName("GET /posts/{id} - PENDING_REVIEW 포스트 - 본인 조회 가능")
  void getPost_pendingReviewPost_ownerCanView() throws Exception {
    // Submit postD to PENDING_REVIEW
    mvc.perform(
        post("/api/blog/posts/{id}/submit", postD.getId())
            .with(TestAuth.asMember(MOCK_USER_ID)));

    mvc.perform(get("/api/blog/posts/{id}", postD.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING_REVIEW"));
  }

  @Test
  @DisplayName("GET /posts/{id} - PENDING_REVIEW 포스트 - 타인 403")
  void getPost_pendingReviewPost_otherUserForbidden() throws Exception {
    mvc.perform(
        post("/api/blog/posts/{id}/submit", postD.getId())
            .with(TestAuth.asMember(MOCK_USER_ID)));

    mvc.perform(get("/api/blog/posts/{id}", postD.getId()).with(TestAuth.asMember(OTHER_USER_ID)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PUT /posts/{id} - REJECTED 포스트 수정 → DRAFT로 전환")
  void updatePost_rejectedPost_resetsToDraft() throws Exception {
    // Create a REJECTED post for MOCK_USER
    Post rejected =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("거부된 포스트")
                .content("검토 거부된 내용입니다.")
                .board("백엔드")
                .category("JPA")
                .status(PostStatus.REJECTED)
                .generation("13기")
                .build());
    rejected.reject("내용이 부족합니다");

    String body =
        """
        {
          "title": "수정된 거부 포스트",
          "content": "내용을 보강했습니다.",
          "board": "백엔드",
          "category": "JPA"
        }
        """;

    mvc.perform(
            put("/api/blog/posts/{id}", rejected.getId())
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DRAFT"));
  }
}
