package com.study.profile.application.dto;

import com.study.profile.domain.generation.GenerationRole;
import jakarta.validation.constraints.NotNull;

public record GenerationMemberAddRequest(
    @NotNull Long memberId, @NotNull GenerationRole roleInGen) {}
