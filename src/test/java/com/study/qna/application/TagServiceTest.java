package com.study.qna.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.study.qna.application.dto.request.tag.TagCreateRequest;
import com.study.qna.application.dto.response.tag.TagResponse;
import com.study.qna.domain.Tag;
import com.study.qna.domain.exception.TagAlreadyExistsException;
import com.study.qna.domain.exception.TagNotFoundException;
import com.study.qna.infrastructure.TagRepository;
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
@DisplayName("TagService")
class TagServiceTest {

  static final Long TAG_ID = 1L;

  @Mock TagRepository tagRepository;
  @InjectMocks TagService tagService;

  private Tag tagWithId(Long id, String name) {
    Tag tag = Tag.create(name);
    ReflectionTestUtils.setField(tag, "id", id);
    return tag;
  }

  @Nested
  @DisplayName("getTags")
  class GetTags {

    @Test
    @DisplayName("이름 오름차순 목록 반환")
    void returnsSortedList() {
      Tag t1 = tagWithId(1L, "JPA");
      Tag t2 = tagWithId(2L, "Spring");
      when(tagRepository.findAllByOrderByNameAsc()).thenReturn(List.of(t1, t2));

      List<TagResponse> res = tagService.getTags();

      assertThat(res).hasSize(2);
      assertThat(res.get(0).name()).isEqualTo("JPA");
      assertThat(res.get(1).name()).isEqualTo("Spring");
    }

    @Test
    @DisplayName("태그 없으면 빈 목록 반환")
    void noTags_returnsEmpty() {
      when(tagRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

      assertThat(tagService.getTags()).isEmpty();
    }
  }

  @Nested
  @DisplayName("createTag")
  class CreateTag {

    @Test
    @DisplayName("정상 생성 → TagResponse 반환")
    void normal_returnsTagResponse() {
      Tag saved = tagWithId(TAG_ID, "JPA");
      when(tagRepository.existsByName("JPA")).thenReturn(false);
      when(tagRepository.save(any())).thenReturn(saved);

      TagResponse res = tagService.createTag(new TagCreateRequest("JPA"));

      assertThat(res.name()).isEqualTo("JPA");
      assertThat(res.id()).isEqualTo(TAG_ID);
    }

    @Test
    @DisplayName("이름 중복 → TagAlreadyExistsException")
    void duplicateName_throws() {
      when(tagRepository.existsByName("JPA")).thenReturn(true);

      assertThatThrownBy(() -> tagService.createTag(new TagCreateRequest("JPA")))
          .isInstanceOf(TagAlreadyExistsException.class);

      verify(tagRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("deleteTag")
  class DeleteTag {

    @Test
    @DisplayName("정상 삭제 → delete 호출")
    void normal_deletesTag() {
      Tag tag = tagWithId(TAG_ID, "JPA");
      when(tagRepository.findById(TAG_ID)).thenReturn(Optional.of(tag));

      tagService.deleteTag(TAG_ID);

      verify(tagRepository).delete(tag);
    }

    @Test
    @DisplayName("존재하지 않는 태그 → TagNotFoundException")
    void notFound_throws() {
      when(tagRepository.findById(TAG_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> tagService.deleteTag(TAG_ID))
          .isInstanceOf(TagNotFoundException.class);
    }
  }
}
