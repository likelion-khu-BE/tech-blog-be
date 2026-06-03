package com.study.profile.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record GenerationCloseRequest(@NotNull LocalDate endDate) {}
