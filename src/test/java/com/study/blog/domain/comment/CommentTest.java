package com.study.blog.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("Comment 도메인")
class CommentTest {

  private Comment comment(Long postId, Long userId, String content) {
    return Comment.builder().postId(postId).userId(userId).content(content).build();
  }

  @Test
  @DisplayName("생성 직후 isDeleted = false")
  void isDeleted_whenCreated_returnsFalse() {
    Comment c = comment(1L, 1L, "내용");
    assertThat(c.isDeleted()).isFalse();
  }

  @Test
  @DisplayName("softDelete 후 isDeleted = true")
  void softDelete_setsDeletedAt_isDeletedReturnsTrue() {
    Comment c = comment(1L, 1L, "내용");
    c.softDelete();
    assertThat(c.isDeleted()).isTrue();
  }

  @Test
  @DisplayName("updateContent 호출 시 content 변경")
  void updateContent_changesContent() {
    Comment c = comment(1L, 1L, "원본");
    c.updateContent("수정됨");
    assertThat(c.getContent()).isEqualTo("수정됨");
  }

  @Test
  @DisplayName("parent 없을 때 getParentId = null")
  void getParentId_withoutParent_returnsNull() {
    Comment c = comment(1L, 1L, "루트");
    assertThat(c.getParentId()).isNull();
  }

  @Test
  @DisplayName("parent 있을 때 getParentId = parent.id")
  void getParentId_withParent_returnsParentId() {
    Comment parent = comment(1L, 1L, "부모");
    ReflectionTestUtils.setField(parent, "id", 42L);

    Comment child = Comment.builder().postId(1L).userId(2L).parent(parent).content("자식").build();

    assertThat(child.getParentId()).isEqualTo(42L);
  }
}
