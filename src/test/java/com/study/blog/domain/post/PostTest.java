package com.study.blog.domain.post;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Post 도메인")
class PostTest {

  Post post;

  @BeforeEach
  void setUp() {
    post =
        Post.builder()
            .userId(1L)
            .title("원본 제목")
            .content("원본 내용")
            .board("백엔드")
            .category("Spring")
            .status(PostStatus.DRAFT)
            .generation("13기")
            .build();
  }

  @Test
  @DisplayName("update() - 제목·내용·게시판·카테고리·상태 모두 변경")
  void update_changesAllFields() {
    post.update("새 제목", "새 내용", "프론트", "React", PostStatus.PUBLISHED);

    assertThat(post.getTitle()).isEqualTo("새 제목");
    assertThat(post.getContent()).isEqualTo("새 내용");
    assertThat(post.getBoard()).isEqualTo("프론트");
    assertThat(post.getCategory()).isEqualTo("React");
    assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
  }

  @Test
  @DisplayName("changeStatus() - 상태만 변경")
  void changeStatus_updatesStatus() {
    post.changeStatus(PostStatus.PUBLISHED);

    assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
    assertThat(post.getTitle()).isEqualTo("원본 제목");
  }
}
