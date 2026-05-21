package com.study.sessionboard.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EventPostCreateRequest(
    @NotBlank String type,
    @NotBlank @Size(max = 100) String title,
    @NotBlank @Size(max = 10000) String body,
    @Size(max = 10) List<@Size(max = 20) String> tags) {}
