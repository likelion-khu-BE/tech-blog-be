package com.study.blog.application.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.study.blog.application.comment.dto.CommentCreateRequest;
import com.study.blog.application.comment.dto.CommentResponse;
import com.study.blog.application.comment.dto.CommentUpdateRequest;
import com.study.blog.domain.comment.Comment;
import com.study.blog.domain.comment.CommentLike;
import com.study.blog.infrastructure.comment.CommentLikeRepository;
import com.study.blog.infrastructure.comment.CommentRepository;
import com.study.blog.shared.exception.BlogErrorCode;
import com.study.blog.shared.exception.BlogException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService")
class CommentServiceTest {

  static final Long POST_ID = 1L;
  static final Long USER_ID = 10L;
  static final Long OTHER_USER_ID = 20L;

  @Mock CommentRepository commentRepository;
  @Mock CommentLikeRepository commentLikeRepository;
  @InjectMocks CommentService commentService;

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Comment commentWithId(Long id, Long postId, Long userId, String content) {
    Comment c = Comment.builder().postId(postId).userId(userId).content(content).build();
    ReflectionTestUtils.setField(c, "id", id);
    return c;
  }

  private Comment replyWithId(
      Long id, Long postId, Long userId, String content, Comment parent) {
    Comment c =
        Comment.builder().postId(postId).userId(userId).parent(parent).content(content).build();
    ReflectionTestUtils.setField(c, "id", id);
    return c;
  }

  private void setCreatedAt(Comment c, LocalDateTime time) {
    ReflectionTestUtils.setField(c, "createdAt", time);
  }

  // ── getComments ────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getComments")
  class GetComments {

    @Test
    @DisplayName("댓글 없으면 빈 리스트 반환 + likeRepository 미호출")
    void emptyPost_returnsEmptyListWithoutLikeQuery() {
      when(commentRepository.findAllByPostIdOrderByCreatedAtAsc(POST_ID)).thenReturn(List.of());

      List<CommentResponse> result = commentService.getComments(POST_ID, USER_ID);

      assertThat(result).isEmpty();
      verify(commentLikeRepository, never()).countGroupedByCommentId(any());
      verify(commentLikeRepository, never()).findLikedCommentIds(any(), any());
    }

    @Test
    @DisplayName("requesterId=null 이면 findLikedCommentIds 미호출")
    void nullRequester_doesNotCallFindLikedCommentIds() {
      Comment root = commentWithId(1L, POST_ID, USER_ID, "루트");
      setCreatedAt(root, LocalDateTime.now());

      when(commentRepository.findAllByPostIdOrderByCreatedAtAsc(POST_ID))
          .thenReturn(List.of(root));
      when(commentLikeRepository.countGroupedByCommentId(List.of(1L))).thenReturn(List.of());

      commentService.getComments(POST_ID, null);

      verify(commentLikeRepository, never()).findLikedCommentIds(any(), any());
    }

    @Test
    @DisplayName("루트/대댓글 트리 구조 반환")
    void withComments_buildsTree() {
      Comment root1 = commentWithId(1L, POST_ID, USER_ID, "루트1");
      Comment root2 = commentWithId(2L, POST_ID, USER_ID, "루트2");
      Comment reply = replyWithId(3L, POST_ID, OTHER_USER_ID, "대댓글", root1);

      setCreatedAt(root1, LocalDateTime.of(2024, 1, 1, 10, 0));
      setCreatedAt(root2, LocalDateTime.of(2024, 1, 2, 10, 0));
      setCreatedAt(reply, LocalDateTime.of(2024, 1, 1, 11, 0));

      when(commentRepository.findAllByPostIdOrderByCreatedAtAsc(POST_ID))
          .thenReturn(List.of(root1, root2, reply));
      when(commentLikeRepository.countGroupedByCommentId(any())).thenReturn(List.of());
      when(commentLikeRepository.findLikedCommentIds(any(), eq(USER_ID))).thenReturn(List.of());

      List<CommentResponse> result = commentService.getComments(POST_ID, USER_ID);

      assertThat(result).hasSize(2);
      assertThat(result.get(0).replies()).hasSize(1);
      assertThat(result.get(1).replies()).isEmpty();
    }
  }

  // ── createComment ──────────────────────────────────────────────────────────

  @Nested
  @DisplayName("createComment")
  class CreateComment {

    @Test
    @DisplayName("루트 댓글 생성 — parentId=null, save 호출")
    void rootComment_savesWithNoParent() {
      CommentCreateRequest req = new CommentCreateRequest("내용", null);
      Comment saved = commentWithId(1L, POST_ID, USER_ID, "내용");
      when(commentRepository.save(any())).thenReturn(saved);

      CommentResponse res = commentService.createComment(POST_ID, req, USER_ID);

      assertThat(res.content()).isEqualTo("내용");
      assertThat(res.parentId()).isNull();
      verify(commentRepository).save(any());
    }

    @Test
    @DisplayName("대댓글 생성 — 유효한 parentId, save 호출")
    void reply_savesWithParent() {
      Comment parent = commentWithId(5L, POST_ID, OTHER_USER_ID, "부모");
      CommentCreateRequest req = new CommentCreateRequest("대댓글", 5L);
      Comment saved = replyWithId(6L, POST_ID, USER_ID, "대댓글", parent);

      when(commentRepository.findById(5L)).thenReturn(Optional.of(parent));
      when(commentRepository.save(any())).thenReturn(saved);

      CommentResponse res = commentService.createComment(POST_ID, req, USER_ID);

      assertThat(res.parentId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("존재하지 않는 parentId → PARENT_COMMENT_NOT_FOUND")
    void parentNotFound_throwsParentCommentNotFound() {
      CommentCreateRequest req = new CommentCreateRequest("내용", 999L);
      when(commentRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> commentService.createComment(POST_ID, req, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(e -> assertThat(((BlogException) e).getErrorCode())
              .isEqualTo(BlogErrorCode.PARENT_COMMENT_NOT_FOUND));
    }

    @Test
    @DisplayName("삭제된 부모 댓글에 대댓글 → PARENT_COMMENT_NOT_FOUND")
    void deletedParent_throwsParentCommentNotFound() {
      Comment parent = commentWithId(5L, POST_ID, OTHER_USER_ID, "부모");
      parent.softDelete();
      CommentCreateRequest req = new CommentCreateRequest("대댓글", 5L);
      when(commentRepository.findById(5L)).thenReturn(Optional.of(parent));

      assertThatThrownBy(() -> commentService.createComment(POST_ID, req, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(e -> assertThat(((BlogException) e).getErrorCode())
              .isEqualTo(BlogErrorCode.PARENT_COMMENT_NOT_FOUND));
    }

    @Test
    @DisplayName("다른 포스트의 댓글을 부모로 지정 → PARENT_COMMENT_NOT_FOUND")
    void parentInDifferentPost_throwsParentCommentNotFound() {
      Comment parent = commentWithId(5L, 999L /* 다른 포스트 */, OTHER_USER_ID, "부모");
      CommentCreateRequest req = new CommentCreateRequest("대댓글", 5L);
      when(commentRepository.findById(5L)).thenReturn(Optional.of(parent));

      assertThatThrownBy(() -> commentService.createComment(POST_ID, req, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(e -> assertThat(((BlogException) e).getErrorCode())
              .isEqualTo(BlogErrorCode.PARENT_COMMENT_NOT_FOUND));
    }
  }

  // ── updateComment ──────────────────────────────────────────────────────────

  @Nested
  @DisplayName("updateComment")
  class UpdateComment {

    @Test
    @DisplayName("본인 댓글 수정 → content 변경 후 반환")
    void owner_updatesContentAndReturns() {
      Comment c = commentWithId(1L, POST_ID, USER_ID, "원본");
      CommentUpdateRequest req = new CommentUpdateRequest("수정됨");

      when(commentRepository.findById(1L)).thenReturn(Optional.of(c));
      when(commentLikeRepository.countByIdCommentId(1L)).thenReturn(3L);
      when(commentLikeRepository.findByIdCommentIdAndIdUserId(1L, USER_ID))
          .thenReturn(Optional.of(new CommentLike(c, USER_ID)));

      CommentResponse res = commentService.updateComment(1L, req, USER_ID);

      assertThat(res.content()).isEqualTo("수정됨");
      assertThat(res.likeCount()).isEqualTo(3L);
      assertThat(res.liked()).isTrue();
    }

    @Test
    @DisplayName("타인 댓글 수정 → FORBIDDEN")
    void notOwner_throwsForbidden() {
      Comment c = commentWithId(1L, POST_ID, OTHER_USER_ID, "내용");
      when(commentRepository.findById(1L)).thenReturn(Optional.of(c));

      assertThatThrownBy(() -> commentService.updateComment(1L, new CommentUpdateRequest("x"), USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(e -> assertThat(((BlogException) e).getErrorCode())
              .isEqualTo(BlogErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정 → COMMENT_NOT_FOUND")
    void notFound_throwsCommentNotFound() {
      when(commentRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> commentService.updateComment(999L, new CommentUpdateRequest("x"), USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(e -> assertThat(((BlogException) e).getErrorCode())
              .isEqualTo(BlogErrorCode.COMMENT_NOT_FOUND));
    }

    @Test
    @DisplayName("삭제된 댓글 수정 → COMMENT_NOT_FOUND")
    void deletedComment_throwsCommentNotFound() {
      Comment c = commentWithId(1L, POST_ID, USER_ID, "내용");
      c.softDelete();
      when(commentRepository.findById(1L)).thenReturn(Optional.of(c));

      assertThatThrownBy(() -> commentService.updateComment(1L, new CommentUpdateRequest("x"), USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(e -> assertThat(((BlogException) e).getErrorCode())
              .isEqualTo(BlogErrorCode.COMMENT_NOT_FOUND));
    }
  }

  // ── deleteComment ──────────────────────────────────────────────────────────

  @Nested
  @DisplayName("deleteComment")
  class DeleteComment {

    @Test
    @DisplayName("본인 댓글 삭제 → softDelete 호출")
    void owner_softDeletesComment() {
      Comment c = commentWithId(1L, POST_ID, USER_ID, "내용");
      when(commentRepository.findById(1L)).thenReturn(Optional.of(c));

      commentService.deleteComment(1L, USER_ID);

      assertThat(c.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("타인 댓글 삭제 → FORBIDDEN")
    void notOwner_throwsForbidden() {
      Comment c = commentWithId(1L, POST_ID, OTHER_USER_ID, "내용");
      when(commentRepository.findById(1L)).thenReturn(Optional.of(c));

      assertThatThrownBy(() -> commentService.deleteComment(1L, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(e -> assertThat(((BlogException) e).getErrorCode())
              .isEqualTo(BlogErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 → COMMENT_NOT_FOUND")
    void notFound_throwsCommentNotFound() {
      when(commentRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> commentService.deleteComment(999L, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(e -> assertThat(((BlogException) e).getErrorCode())
              .isEqualTo(BlogErrorCode.COMMENT_NOT_FOUND));
    }

    @Test
    @DisplayName("이미 삭제된 댓글 재삭제 → COMMENT_NOT_FOUND")
    void alreadyDeleted_throwsCommentNotFound() {
      Comment c = commentWithId(1L, POST_ID, USER_ID, "내용");
      c.softDelete();
      when(commentRepository.findById(1L)).thenReturn(Optional.of(c));

      assertThatThrownBy(() -> commentService.deleteComment(1L, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(e -> assertThat(((BlogException) e).getErrorCode())
              .isEqualTo(BlogErrorCode.COMMENT_NOT_FOUND));
    }
  }

  // ── toggleLike ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("toggleLike")
  class ToggleLike {

    @Test
    @DisplayName("좋아요 없는 상태 → 저장 후 true 반환")
    void noExistingLike_savesAndReturnsTrue() {
      Comment c = commentWithId(1L, POST_ID, USER_ID, "내용");
      when(commentRepository.findById(1L)).thenReturn(Optional.of(c));
      when(commentLikeRepository.findByIdCommentIdAndIdUserId(1L, USER_ID))
          .thenReturn(Optional.empty());

      boolean result = commentService.toggleLike(1L, USER_ID);

      assertThat(result).isTrue();
      verify(commentLikeRepository).saveAndFlush(any());
    }

    @Test
    @DisplayName("좋아요 있는 상태 → 삭제 후 false 반환")
    void existingLike_deletesAndReturnsFalse() {
      Comment c = commentWithId(1L, POST_ID, USER_ID, "내용");
      CommentLike existing = new CommentLike(c, USER_ID);
      when(commentRepository.findById(1L)).thenReturn(Optional.of(c));
      when(commentLikeRepository.findByIdCommentIdAndIdUserId(1L, USER_ID))
          .thenReturn(Optional.of(existing));

      boolean result = commentService.toggleLike(1L, USER_ID);

      assertThat(result).isFalse();
      verify(commentLikeRepository).delete(existing);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 좋아요 → COMMENT_NOT_FOUND")
    void commentNotFound_throwsCommentNotFound() {
      when(commentRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> commentService.toggleLike(999L, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(e -> assertThat(((BlogException) e).getErrorCode())
              .isEqualTo(BlogErrorCode.COMMENT_NOT_FOUND));
    }

    @Test
    @DisplayName("삭제된 댓글 좋아요 → COMMENT_NOT_FOUND")
    void deletedComment_throwsCommentNotFound() {
      Comment c = commentWithId(1L, POST_ID, USER_ID, "내용");
      c.softDelete();
      when(commentRepository.findById(1L)).thenReturn(Optional.of(c));

      assertThatThrownBy(() -> commentService.toggleLike(1L, USER_ID))
          .isInstanceOf(BlogException.class)
          .satisfies(e -> assertThat(((BlogException) e).getErrorCode())
              .isEqualTo(BlogErrorCode.COMMENT_NOT_FOUND));
    }
  }
}