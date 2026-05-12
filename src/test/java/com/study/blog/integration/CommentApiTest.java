package com.study.blog.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.study.StudyBeApplication;
import com.study.blog.domain.comment.Comment;
import com.study.blog.domain.comment.CommentLike;
import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostStatus;
import com.study.blog.infrastructure.comment.CommentLikeRepository;
import com.study.blog.infrastructure.comment.CommentRepository;
import com.study.blog.infrastructure.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for Comment API endpoints.
 *
 * <p>Test data per test: one published post (postA) with a two-level comment tree:
 *
 * <pre>
 *   root1 (MOCK_USER) ← liked by MOCK_USER
 *     └── reply1 (OTHER_USER)
 *     └── reply2 (MOCK_USER)
 *   root2 (OTHER_USER)
 * </pre>
 */
@SpringBootTest(
    classes = StudyBeApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
@DisplayName("댓글 API")
class CommentApiTest {

  static final Long MOCK_USER_ID = 1L;
  static final Long OTHER_USER_ID = 2L;

  @Autowired MockMvc mvc;
  @Autowired PostRepository postRepository;
  @Autowired CommentRepository commentRepository;
  @Autowired CommentLikeRepository commentLikeRepository;

  Post postA;
  Comment root1, root2, reply1, reply2;

  @BeforeEach
  void setUp() {
    postA =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("Spring Boot + GitHub Actions로 CI/CD 파이프라인 구축하기")
                .content("GitHub Actions와 AWS EC2를 활용해 자동 배포 파이프라인을 구성하는 방법입니다.")
                .board("백엔드")
                .category("CI/CD")
                .status(PostStatus.PUBLISHED)
                .generation("13기")
                .build());

    // Root comment 1 by MOCK_USER
    root1 =
        commentRepository.save(
            Comment.builder()
                .postId(postA.getId())
                .userId(MOCK_USER_ID)
                .content("정말 유익한 글이네요! CI/CD 파이프라인 설정이 이렇게 간단하다니 놀랍습니다.")
                .build());

    // Reply 1 under root1 by OTHER_USER
    reply1 =
        commentRepository.save(
            Comment.builder()
                .postId(postA.getId())
                .userId(OTHER_USER_ID)
                .parent(root1)
                .content("저도 이 방법으로 해봤는데 Runner 설정에서 약간 헤맸어요. 혹시 팁이 있으신가요?")
                .build());

    // Reply 2 under root1 by MOCK_USER
    reply2 =
        commentRepository.save(
            Comment.builder()
                .postId(postA.getId())
                .userId(MOCK_USER_ID)
                .parent(root1)
                .content("Runner는 self-hosted 대신 ubuntu-latest 사용하면 편해요!")
                .build());

    // Root comment 2 by OTHER_USER
    root2 =
        commentRepository.save(
            Comment.builder()
                .postId(postA.getId())
                .userId(OTHER_USER_ID)
                .content("EC2 비용 절감을 위해 스팟 인스턴스 쓰는 분 계신가요?")
                .build());

    // root1 liked by MOCK_USER
    commentLikeRepository.save(new CommentLike(root1, MOCK_USER_ID));
  }

  // ── GET /api/blog/posts/{postId}/comments ───────────────────────────────

  @Test
  @DisplayName("GET /comments - 트리 구조 반환")
  void getComments_returnsTreeStructure() throws Exception {
    mvc.perform(get("/api/blog/posts/{postId}/comments", postA.getId()))
        .andExpect(status().isOk())
        // 2 root comments
        .andExpect(jsonPath("$.length()").value(2))
        // root1 has 2 replies
        .andExpect(jsonPath("$[0].replies.length()").value(2))
        // root2 has 0 replies
        .andExpect(jsonPath("$[1].replies.length()").value(0));
  }

  @Test
  @DisplayName("GET /comments - 루트 댓글 필드 정확성")
  void getComments_rootCommentFields_includeCorrectData() throws Exception {
    mvc.perform(
            get("/api/blog/posts/{postId}/comments", postA.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        // root1 fields
        .andExpect(jsonPath("$[0].id").value(root1.getId()))
        .andExpect(jsonPath("$[0].content").value("정말 유익한 글이네요! CI/CD 파이프라인 설정이 이렇게 간단하다니 놀랍습니다."))
        .andExpect(jsonPath("$[0].userId").value(MOCK_USER_ID))
        .andExpect(jsonPath("$[0].likeCount").value(1))
        .andExpect(jsonPath("$[0].liked").value(true)) // MOCK_USER liked root1
        .andExpect(jsonPath("$[0].parentId").doesNotExist());
  }

  @Test
  @DisplayName("GET /comments - 대댓글 parentId 포함")
  void getComments_replyFields_includeParentId() throws Exception {
    mvc.perform(
            get("/api/blog/posts/{postId}/comments", postA.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        // reply1 is first reply of root1
        .andExpect(jsonPath("$[0].replies[0].parentId").value(root1.getId()))
        .andExpect(jsonPath("$[0].replies[0].userId").value(OTHER_USER_ID))
        .andExpect(jsonPath("$[0].replies[0].liked").value(false));
  }

  @Test
  @DisplayName("GET /comments - 댓글 없는 포스트 빈 배열")
  void getComments_emptyPost_returnsEmptyList() throws Exception {
    Post emptyPost =
        postRepository.save(
            Post.builder()
                .userId(MOCK_USER_ID)
                .title("댓글 없는 포스트")
                .content("댓글이 하나도 없는 포스트입니다.")
                .board("기타")
                .category("테스트")
                .status(PostStatus.PUBLISHED)
                .generation("13기")
                .build());

    mvc.perform(get("/api/blog/posts/{postId}/comments", emptyPost.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  // ── POST /api/blog/posts/{postId}/comments ───────────────────────────────

  @Test
  @DisplayName("POST /comments - 루트 댓글 생성 201")
  void createComment_rootComment_returns201() throws Exception {
    String body =
        """
        {
          "content": "Nginx 리버스 프록시 설정도 함께 설명해주시면 좋겠습니다!"
        }
        """;

    mvc.perform(
            post("/api/blog/posts/{postId}/comments", postA.getId())
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("Nginx 리버스 프록시 설정도 함께 설명해주시면 좋겠습니다!"))
        .andExpect(jsonPath("$.userId").value(MOCK_USER_ID))
        .andExpect(jsonPath("$.parentId").doesNotExist())
        .andExpect(jsonPath("$.likeCount").value(0))
        .andExpect(jsonPath("$.liked").value(false))
        .andExpect(jsonPath("$.replies.length()").value(0));
  }

  @Test
  @DisplayName("POST /comments - 대댓글 생성 201")
  void createComment_reply_returns201WithParentId() throws Exception {
    String body =
        String.format(
            """
            {
              "content": "저는 EC2 t2.micro 무료 티어로 돌리고 있어요.",
              "parentId": %d
            }
            """,
            root2.getId());

    mvc.perform(
            post("/api/blog/posts/{postId}/comments", postA.getId())
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.parentId").value(root2.getId()))
        .andExpect(jsonPath("$.content").value("저는 EC2 t2.micro 무료 티어로 돌리고 있어요."));
  }

  @Test
  @DisplayName("POST /comments - 존재하지 않는 부모 댓글 404")
  void createComment_invalidParentId_returns404() throws Exception {
    String body =
        """
        {
          "content": "존재하지 않는 부모 댓글에 대댓글",
          "parentId": 999999
        }
        """;

    mvc.perform(
            post("/api/blog/posts/{postId}/comments", postA.getId())
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /comments - 내용 공백 400")
  void createComment_blankContent_returns400() throws Exception {
    String body =
        """
        {
          "content": ""
        }
        """;

    mvc.perform(
            post("/api/blog/posts/{postId}/comments", postA.getId())
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // ── PUT /api/blog/comments/{id} ──────────────────────────────────────────

  @Test
  @DisplayName("PUT /comments - 본인 댓글 수정 성공")
  void updateComment_ownComment_returnsUpdated() throws Exception {
    String body =
        """
        {
          "content": "수정된 댓글: self-hosted Runner보다 github-hosted가 안정적이에요!"
        }
        """;

    mvc.perform(
            put("/api/blog/comments/{id}", root1.getId())
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.content").value("수정된 댓글: self-hosted Runner보다 github-hosted가 안정적이에요!"))
        .andExpect(jsonPath("$.id").value(root1.getId()));
  }

  @Test
  @DisplayName("PUT /comments - 타인 댓글 수정 403")
  void updateComment_othersComment_returns403() throws Exception {
    // root2 is owned by OTHER_USER; MOCK_USER tries to update it → 403
    String body =
        """
        {
          "content": "타인의 댓글 무단 수정 시도"
        }
        """;

    mvc.perform(
            put("/api/blog/comments/{id}", root2.getId())
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PUT /comments - 존재하지 않는 댓글 404")
  void updateComment_notFound_returns404() throws Exception {
    String body =
        """
        {
          "content": "없는 댓글 수정 시도"
        }
        """;

    mvc.perform(
            put("/api/blog/comments/{id}", 999999L)
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }

  // ── DELETE /api/blog/comments/{id} ───────────────────────────────────────

  @Test
  @DisplayName("DELETE /comments - 본인 리프 댓글 204")
  void deleteComment_ownLeafComment_returns204() throws Exception {
    // Create a fresh root comment with no replies or likes — safe to delete
    Comment toDelete =
        commentRepository.save(
            Comment.builder()
                .postId(postA.getId())
                .userId(MOCK_USER_ID)
                .content("삭제될 댓글입니다.")
                .build());

    mvc.perform(
            delete("/api/blog/comments/{id}", toDelete.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /comments - 타인 댓글 403")
  void deleteComment_othersComment_returns403() throws Exception {
    // root2 is owned by OTHER_USER; MOCK_USER tries to delete it → 403
    mvc.perform(
            delete("/api/blog/comments/{id}", root2.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("DELETE /comments - 존재하지 않는 댓글 404")
  void deleteComment_notFound_returns404() throws Exception {
    mvc.perform(delete("/api/blog/comments/{id}", 999999L).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNotFound());
  }

  // ── POST /api/blog/comments/{id}/like ────────────────────────────────────

  @Test
  @DisplayName("POST /comments/like - 좋아요 없는 상태 liked=true")
  void toggleCommentLike_noExistingLike_returnsLikedTrue() throws Exception {
    // root2 has no like from MOCK_USER
    mvc.perform(
            post("/api/blog/comments/{id}/like", root2.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(true));
  }

  @Test
  @DisplayName("POST /comments/like - 좋아요 있는 상태 liked=false")
  void toggleCommentLike_existingLike_returnsLikedFalse() throws Exception {
    // root1 is already liked by MOCK_USER (set in setUp)
    mvc.perform(
            post("/api/blog/comments/{id}/like", root1.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(false));
  }

  @Test
  @DisplayName("POST /comments/like - 좋아요/취소 반복")
  void toggleCommentLike_likeAndUnlike_likeCountChanges() throws Exception {
    // reply2 has no like — like it, then unlike it, verify state
    mvc.perform(
            post("/api/blog/comments/{id}/like", reply2.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(jsonPath("$.liked").value(true));

    mvc.perform(
            post("/api/blog/comments/{id}/like", reply2.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(jsonPath("$.liked").value(false));
  }

  @Test
  @DisplayName("POST /comments/like - 존재하지 않는 댓글 404")
  void toggleCommentLike_notFound_returns404() throws Exception {
    mvc.perform(post("/api/blog/comments/{id}/like", 999999L).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNotFound());
  }

  // ── SOFT DELETE ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("소프트 삭제 - 루트 삭제 시 플레이스홀더 표시, 대댓글 유지")
  void deleteComment_softDeletes_rootShowsPlaceholderAndRepliesSurvive() throws Exception {
    mvc.perform(
            delete("/api/blog/comments/{id}", root1.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNoContent());

    mvc.perform(get("/api/blog/posts/{postId}/comments", postA.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].content").value("삭제된 댓글입니다."))
        .andExpect(jsonPath("$[0].userId").doesNotExist())
        .andExpect(jsonPath("$[0].likeCount").value(0))
        .andExpect(jsonPath("$[0].liked").value(false))
        .andExpect(jsonPath("$[0].replies.length()").value(2));
  }

  @Test
  @DisplayName("소프트 삭제 - 대댓글 삭제 시 트리에 플레이스홀더")
  void deleteComment_softDeletes_replyShowsPlaceholderInTree() throws Exception {
    // reply1 (OTHER_USER) is soft-deleted; root1 still shows with 1 remaining reply
    mvc.perform(
            delete("/api/blog/comments/{id}", reply1.getId())
                .with(TestAuth.asMember(OTHER_USER_ID)))
        .andExpect(status().isNoContent());

    mvc.perform(get("/api/blog/posts/{postId}/comments", postA.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].replies.length()").value(2))
        .andExpect(jsonPath("$[0].replies[0].content").value("삭제된 댓글입니다."))
        .andExpect(jsonPath("$[0].replies[0].userId").doesNotExist())
        .andExpect(
            jsonPath("$[0].replies[1].content")
                .value("Runner는 self-hosted 대신 ubuntu-latest 사용하면 편해요!"));
  }

  @Test
  @DisplayName("소프트 삭제 - 삭제된 댓글 수정 404")
  void updateComment_deletedComment_returns404() throws Exception {
    mvc.perform(
            delete("/api/blog/comments/{id}", root1.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNoContent());

    String body =
        """
        { "content": "삭제된 댓글 수정 시도" }
        """;

    mvc.perform(
            put("/api/blog/comments/{id}", root1.getId())
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("소프트 삭제 - 이미 삭제된 댓글 재삭제 404")
  void deleteComment_alreadyDeleted_returns404() throws Exception {
    mvc.perform(
            delete("/api/blog/comments/{id}", root1.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNoContent());

    mvc.perform(
            delete("/api/blog/comments/{id}", root1.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("소프트 삭제 - 삭제된 댓글 좋아요 404")
  void toggleCommentLike_deletedComment_returns404() throws Exception {
    mvc.perform(
            delete("/api/blog/comments/{id}", root1.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNoContent());

    mvc.perform(
            post("/api/blog/comments/{id}/like", root1.getId())
                .with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("소프트 삭제 - 삭제된 댓글에 대댓글 404")
  void createComment_replyToDeletedParent_returns404() throws Exception {
    mvc.perform(
            delete("/api/blog/comments/{id}", root1.getId()).with(TestAuth.asMember(MOCK_USER_ID)))
        .andExpect(status().isNoContent());

    String body =
        String.format(
            """
            {
              "content": "삭제된 댓글에 대댓글 시도",
              "parentId": %d
            }
            """,
            root1.getId());

    mvc.perform(
            post("/api/blog/posts/{postId}/comments", postA.getId())
                .with(TestAuth.asMember(MOCK_USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }
}
