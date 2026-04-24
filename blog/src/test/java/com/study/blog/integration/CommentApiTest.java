package com.study.blog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.study.blog.BlogTestApplication;
import com.study.common.entity.Comment;
import com.study.common.entity.CommentLike;
import com.study.blog.comment.CommentLikeRepository;
import com.study.blog.comment.CommentRepository;
import com.study.common.entity.Post;
import com.study.blog.post.PostRepository;
import com.study.common.entity.PostStatus;
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
    classes = BlogTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class CommentApiTest {

  static final UUID MOCK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

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
  void getComments_rootCommentFields_includeCorrectData() throws Exception {
    mvc.perform(get("/api/blog/posts/{postId}/comments", postA.getId()))
        .andExpect(status().isOk())
        // root1 fields
        .andExpect(jsonPath("$[0].id").value(root1.getId()))
        .andExpect(jsonPath("$[0].content").value("정말 유익한 글이네요! CI/CD 파이프라인 설정이 이렇게 간단하다니 놀랍습니다."))
        .andExpect(jsonPath("$[0].userId").value(MOCK_USER_ID.toString()))
        .andExpect(jsonPath("$[0].likeCount").value(1))
        .andExpect(jsonPath("$[0].liked").value(true)) // MOCK_USER liked root1
        .andExpect(jsonPath("$[0].parentId").doesNotExist());
  }

  @Test
  void getComments_replyFields_includeParentId() throws Exception {
    mvc.perform(get("/api/blog/posts/{postId}/comments", postA.getId()))
        .andExpect(status().isOk())
        // reply1 is first reply of root1
        .andExpect(jsonPath("$[0].replies[0].parentId").value(root1.getId()))
        .andExpect(jsonPath("$[0].replies[0].userId").value(OTHER_USER_ID.toString()))
        .andExpect(jsonPath("$[0].replies[0].liked").value(false));
  }

  @Test
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
  void createComment_rootComment_returns201() throws Exception {
    String body =
        """
        {
          "content": "Nginx 리버스 프록시 설정도 함께 설명해주시면 좋겠습니다!"
        }
        """;

    mvc.perform(
            post("/api/blog/posts/{postId}/comments", postA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("Nginx 리버스 프록시 설정도 함께 설명해주시면 좋겠습니다!"))
        .andExpect(jsonPath("$.userId").value(MOCK_USER_ID.toString()))
        .andExpect(jsonPath("$.parentId").doesNotExist())
        .andExpect(jsonPath("$.likeCount").value(0))
        .andExpect(jsonPath("$.liked").value(false))
        .andExpect(jsonPath("$.replies.length()").value(0));
  }

  @Test
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.parentId").value(root2.getId()))
        .andExpect(jsonPath("$.content").value("저는 EC2 t2.micro 무료 티어로 돌리고 있어요."));
  }

  @Test
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  void createComment_blankContent_returns400() throws Exception {
    String body =
        """
        {
          "content": ""
        }
        """;

    mvc.perform(
            post("/api/blog/posts/{postId}/comments", postA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // ── PUT /api/blog/comments/{id} ──────────────────────────────────────────

  @Test
  void updateComment_ownComment_returnsUpdated() throws Exception {
    String body =
        """
        {
          "content": "수정된 댓글: self-hosted Runner보다 github-hosted가 안정적이에요!"
        }
        """;

    mvc.perform(
            put("/api/blog/comments/{id}", root1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.content")
                .value("수정된 댓글: self-hosted Runner보다 github-hosted가 안정적이에요!"))
        .andExpect(jsonPath("$.id").value(root1.getId()));
  }

  @Test
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateComment_notFound_returns404() throws Exception {
    String body =
        """
        {
          "content": "없는 댓글 수정 시도"
        }
        """;

    mvc.perform(
            put("/api/blog/comments/{id}", 999999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }

  // ── DELETE /api/blog/comments/{id} ───────────────────────────────────────

  @Test
  void deleteComment_ownLeafComment_returns204() throws Exception {
    // Create a fresh root comment with no replies or likes — safe to delete
    Comment toDelete =
        commentRepository.save(
            Comment.builder()
                .postId(postA.getId())
                .userId(MOCK_USER_ID)
                .content("삭제될 댓글입니다.")
                .build());

    mvc.perform(delete("/api/blog/comments/{id}", toDelete.getId()))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteComment_othersComment_returns403() throws Exception {
    // root2 is owned by OTHER_USER; MOCK_USER tries to delete it → 403
    mvc.perform(delete("/api/blog/comments/{id}", root2.getId()))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteComment_notFound_returns404() throws Exception {
    mvc.perform(delete("/api/blog/comments/{id}", 999999L)).andExpect(status().isNotFound());
  }

  // ── POST /api/blog/comments/{id}/like ────────────────────────────────────

  @Test
  void toggleCommentLike_noExistingLike_returnsLikedTrue() throws Exception {
    // root2 has no like from MOCK_USER
    mvc.perform(post("/api/blog/comments/{id}/like", root2.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(true));
  }

  @Test
  void toggleCommentLike_existingLike_returnsLikedFalse() throws Exception {
    // root1 is already liked by MOCK_USER (set in setUp)
    mvc.perform(post("/api/blog/comments/{id}/like", root1.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(false));
  }

  @Test
  void toggleCommentLike_likeAndUnlike_likeCountChanges() throws Exception {
    // reply2 has no like — like it, then unlike it, verify state
    mvc.perform(post("/api/blog/comments/{id}/like", reply2.getId()))
        .andExpect(jsonPath("$.liked").value(true));

    mvc.perform(post("/api/blog/comments/{id}/like", reply2.getId()))
        .andExpect(jsonPath("$.liked").value(false));
  }

  @Test
  void toggleCommentLike_notFound_returns404() throws Exception {
    mvc.perform(post("/api/blog/comments/{id}/like", 999999L)).andExpect(status().isNotFound());
  }

  // ── CASCADE DELETE via @OnDelete ─────────────────────────────────────────
  // A 204 response proves no FK-constraint violation occurred.
  // If cascade were missing, FK on comment_likes or parent_id would raise an error → 500.

  @Test
  void deleteComment_withReplies_returns204() throws Exception {
    // root1 has reply1 and reply2 via parent_id FK (ON DELETE CASCADE)
    // Without cascade, deleting root1 would violate the FK on replies → 500
    mvc.perform(delete("/api/blog/comments/{id}", root1.getId()))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteComment_withLike_returns204() throws Exception {
    // root1 has a like from MOCK_USER (setUp); comment_likes FK ON DELETE CASCADE
    mvc.perform(delete("/api/blog/comments/{id}", root1.getId()))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteComment_withRepliesAndLike_returns204() throws Exception {
    // root1 has: reply1, reply2 AND a like from MOCK_USER — all cascade-deleted
    // Also add a like on reply1 to verify nested cascade (reply cascade → its likes cascade)
    commentLikeRepository.save(new CommentLike(reply1, OTHER_USER_ID));

    mvc.perform(delete("/api/blog/comments/{id}", root1.getId()))
        .andExpect(status().isNoContent());

    assertThat(commentRepository.findById(root1.getId())).isEmpty();
    assertThat(commentRepository.findById(reply1.getId())).isEmpty();
    assertThat(commentRepository.findById(reply2.getId())).isEmpty();
  }
}
