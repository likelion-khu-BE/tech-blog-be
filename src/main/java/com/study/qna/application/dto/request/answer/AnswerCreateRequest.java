package com.study.qna.application.dto.request.answer;

import jakarta.validation.constraints.NotBlank;

/**
 * 답변 생성 요청 DTO.
 */
public record AnswerCreateRequest(@NotBlank String content) {}
