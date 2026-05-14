package com.study.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.study.profile.domain.generation.Generation;
import com.study.profile.infrastructure.GenerationRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerationServiceTest {

  @Mock private GenerationRepository generationRepository;

  @InjectMocks private GenerationService generationService;

  @Test
  @DisplayName("존재하는 기수 번호(Integer)로 조회 시 정상적으로 Generation을 반환한다")
  void getGenerationByNumber_Success() {
    // given
    Integer generationNumber = 12;
    Generation mockGeneration = Generation.create(generationNumber, null, null, false);
    when(generationRepository.findById(generationNumber)).thenReturn(Optional.of(mockGeneration));

    // when
    Generation result = generationService.getGenerationByNumber(generationNumber);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getNumber()).isEqualTo(generationNumber);
  }

  @Test
  @DisplayName("존재하지 않는 기수 번호로 조회 시 IllegalArgumentException이 발생한다")
  void getGenerationByNumber_Fail_NotFound() {
    // given
    when(generationRepository.findById(anyInt())).thenReturn(Optional.empty());

    // when & then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          generationService.getGenerationByNumber(999);
        });
  }
}
