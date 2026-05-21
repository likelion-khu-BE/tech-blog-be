package com.study.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.study.profile.application.dto.TechStackCreateRequest;
import com.study.profile.application.dto.TechStackDto.IdResponse;
import com.study.profile.domain.exception.TechStackNameDuplicateException;
import com.study.profile.domain.exception.TechStackNotFoundException;
import com.study.profile.domain.techstack.TechStack;
import com.study.profile.domain.techstack.TechStackCategory;
import com.study.profile.infrastructure.TechStackRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TechStackServiceTest {

  @Mock private TechStackRepository techStackRepository;

  @InjectMocks private TechStackService techStackService;

  private TechStack techStackWithId(Long id, String name, TechStackCategory category) {
    TechStack techStack = TechStack.create(name, category, "https://logo");
    ReflectionTestUtils.setField(techStack, "id", id);
    return techStack;
  }

  // ---------- 등록 (§3-2) ----------

  @Test
  @DisplayName("기술 스택 등록 성공 시 저장된 id를 반환한다")
  void create_Success() {
    // given
    TechStackCreateRequest req =
        new TechStackCreateRequest("Bun", TechStackCategory.framework, "https://logo");
    when(techStackRepository.existsByName("Bun")).thenReturn(false);
    when(techStackRepository.save(any(TechStack.class)))
        .thenReturn(techStackWithId(100L, "Bun", TechStackCategory.framework));

    // when
    IdResponse result = techStackService.create(req);

    // then
    assertThat(result.id()).isEqualTo(100L);
    verify(techStackRepository).save(any(TechStack.class));
  }

  @Test
  @DisplayName("이미 존재하는 이름으로 등록 시 TechStackNameDuplicateException이 발생한다")
  void create_Fail_DuplicateName() {
    // given
    TechStackCreateRequest req =
        new TechStackCreateRequest("Java", TechStackCategory.language, "https://logo");
    when(techStackRepository.existsByName("Java")).thenReturn(true);

    // when & then
    assertThrows(TechStackNameDuplicateException.class, () -> techStackService.create(req));
    verify(techStackRepository, never()).save(any(TechStack.class));
  }

  // ---------- 수정 (§3-3) ----------

  @Test
  @DisplayName("기술 스택 수정 성공 시 도메인 메서드로 값이 교체되고 id를 반환한다")
  void update_Success() {
    // given
    TechStack existing = techStackWithId(5L, "Java", TechStackCategory.language);
    TechStackCreateRequest req =
        new TechStackCreateRequest("Kotlin", TechStackCategory.language, "https://new");
    when(techStackRepository.findById(5L)).thenReturn(Optional.of(existing));
    when(techStackRepository.existsByNameAndIdNot("Kotlin", 5L)).thenReturn(false);

    // when
    IdResponse result = techStackService.update(5L, req);

    // then
    assertThat(result.id()).isEqualTo(5L);
    assertThat(existing.getName()).isEqualTo("Kotlin"); // 도메인 메서드로 교체됨 (dirty checking)
    assertThat(existing.getLogoUrl()).isEqualTo("https://new");
    verify(techStackRepository, never()).save(any(TechStack.class)); // merge/save 호출 안 함
  }

  @Test
  @DisplayName("존재하지 않는 id로 수정 시 TechStackNotFoundException이 발생한다")
  void update_Fail_NotFound() {
    // given
    TechStackCreateRequest req =
        new TechStackCreateRequest("Kotlin", TechStackCategory.language, "https://new");
    when(techStackRepository.findById(anyLong())).thenReturn(Optional.empty());

    // when & then
    assertThrows(TechStackNotFoundException.class, () -> techStackService.update(999L, req));
  }

  @Test
  @DisplayName("수정하려는 이름이 다른 기술 스택과 중복되면 TechStackNameDuplicateException이 발생한다")
  void update_Fail_DuplicateName() {
    // given
    TechStack existing = techStackWithId(5L, "Java", TechStackCategory.language);
    TechStackCreateRequest req =
        new TechStackCreateRequest("Python", TechStackCategory.language, "https://new");
    when(techStackRepository.findById(5L)).thenReturn(Optional.of(existing));
    when(techStackRepository.existsByNameAndIdNot("Python", 5L)).thenReturn(true);

    // when & then
    assertThrows(TechStackNameDuplicateException.class, () -> techStackService.update(5L, req));
  }

  // ---------- 삭제 (§3-4) ----------

  @Test
  @DisplayName("기술 스택 삭제 성공 시 deleteById를 호출한다")
  void delete_Success() {
    // given
    when(techStackRepository.existsById(5L)).thenReturn(true);

    // when
    techStackService.delete(5L);

    // then
    verify(techStackRepository).deleteById(5L);
  }

  @Test
  @DisplayName("존재하지 않는 id로 삭제 시 TechStackNotFoundException이 발생하고 deleteById를 호출하지 않는다")
  void delete_Fail_NotFound() {
    // given
    when(techStackRepository.existsById(999L)).thenReturn(false);

    // when & then
    assertThrows(TechStackNotFoundException.class, () -> techStackService.delete(999L));
    verify(techStackRepository, never()).deleteById(anyLong());
  }
}
