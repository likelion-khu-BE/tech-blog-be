package com.study.qna.application.dto.request.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 태그 생성 요청 DTO. */
public record TagCreateRequest(@NotBlank @Size(max = 50) String name) {}
