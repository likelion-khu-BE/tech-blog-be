package com.study.profile.application.dto;

import com.study.profile.domain.generation.Generation;
import java.time.Instant;
import java.time.LocalDate;

public record GenerationDto(
    Integer id,
    String label,
    Integer number,
    LocalDate startDate,
    LocalDate endDate,
    Boolean isCurrent,
    Instant createdAt) {

  public static GenerationDto from(Generation g) {
    return new GenerationDto(
        g.getNumber(),
        g.getNumber() + "기",
        g.getNumber(),
        g.getStartDate(),
        g.getEndDate(),
        g.getIsCurrent(),
        g.getCreatedAt());
  }
}
