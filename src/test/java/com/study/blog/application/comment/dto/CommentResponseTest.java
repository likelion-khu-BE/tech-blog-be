package com.study.blog.application.comment.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.blog.domain.comment.Comment;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("CommentResponse.of()")
class CommentResponseTest {

  Comment comment;

  @BeforeEach
  void setUp() {
    comment = Comment.builder().postId(1L).userId(10L).content("원본 내용").build();
    ReflectionTestUtils.setField(comment, "id", 1L);
  }

  @Test
  @DisplayName("정상 댓글 — 모든 필드 그대로 매핑")
  void normalComment_mapsAllFields() {
    CommentResponse res = CommentResponse.of(comment, 5L, true, List.of());

    assertThat(res.id()).isEqualTo(1L);
    assertThat(res.content()).isEqualTo("원본 내용");
    assertThat(res.userId()).isEqualTo(10L);
    assertThat(res.likeCount()).isEqualTo(5L);
    assertThat(res.liked()).isTrue();
    assertThat(res.parentId()).isNull();
    assertThat(res.replies()).isEmpty();
  }

  @Test
  @DisplayName("삭제된 댓글 — 플레이스홀더 content, userId=null, likeCount=0, liked=false")
  void deletedComment_returnsPlaceholder() {
    comment.softDelete();

    CommentResponse res = CommentResponse.of(comment, 5L, true, List.of());

    assertThat(res.content()).isEqualTo("삭제된 댓글입니다.");
    assertThat(res.userId()).isNull();
    assertThat(res.likeCount()).isZero();
    assertThat(res.liked()).isFalse();
  }

  @Test
  @DisplayName("삭제된 댓글 — replies는 그대로 유지")
  void deletedComment_preservesReplies() {
    comment.softDelete();
    CommentResponse reply = CommentResponse.of(
        Comment.builder().postId(1L).userId(20L).content("대댓글").build(), 0, false, List.of());

    CommentResponse res = CommentResponse.of(comment, 0, false, List.of(reply));

    assertThat(res.replies()).hasSize(1);
    assertThat(res.replies().get(0).content()).isEqualTo("대댓글");
  }

  @Test
  @DisplayName("대댓글 — parentId 정상 매핑")
  void reply_mapsParentId() {
    Comment parent = Comment.builder().postId(1L).userId(10L).content("부모").build();
    ReflectionTestUtils.setField(parent, "id", 99L);

    Comment child = Comment.builder().postId(1L).userId(10L).parent(parent).content("자식").build();
    CommentResponse res = CommentResponse.of(child, 0, false, List.of());

    assertThat(res.parentId()).isEqualTo(99L);
  }

  @Test
  @DisplayName("삭제된 대댓글 — parentId 유지")
  void deletedReply_preservesParentId() {
    Comment parent = Comment.builder().postId(1L).userId(10L).content("부모").build();
    ReflectionTestUtils.setField(parent, "id", 99L);

    Comment child = Comment.builder().postId(1L).userId(10L).parent(parent).content("자식").build();
    child.softDelete();

    CommentResponse res = CommentResponse.of(child, 0, false, List.of());

    assertThat(res.parentId()).isEqualTo(99L);
    assertThat(res.content()).isEqualTo("삭제된 댓글입니다.");
  }
}