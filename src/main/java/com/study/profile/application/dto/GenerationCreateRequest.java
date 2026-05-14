package com.study.profile.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record GenerationCreateRequest(
    @NotNull Integer number,
    @NotNull LocalDate startDate,
    LocalDate endDate,
    Boolean isCurrent) {}
