package com.study.profile.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.study.profile.application.GenerationService;
import com.study.profile.domain.generation.Generation;
import com.study.profile.infrastructure.GenerationRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class GenerationServiceIntegrationTest {

  @Autowired private GenerationService generationService;

  @Autowired private GenerationRepository generationRepository;

  @Test
  @DisplayName("존재하는 기수 번호로 조회 시 정상적으로 Generation을 반환한다")
  void getGenerationByNumber_Success() {
    // given
    Integer number = 13;
    Generation generation = Generation.create(number, LocalDate.of(2025, 3, 1), null, true);
    generationRepository.save(generation);

    // when
    Generation result = generationService.getGenerationByNumber(number);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getNumber()).isEqualTo(number);
  }

  @Test
  @DisplayName("존재하지 않는 기수 번호로 조회 시 IllegalArgumentException이 발생한다")
  void getGenerationByNumber_Fail_NotFound() {
    // given
    Integer nonExistentNumber = 999;

    // when & then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          generationService.getGenerationByNumber(nonExistentNumber);
        });
  }
}
