package com.study.qna.application.dto.request.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 질문 생성 요청 DTO.
 */
public record QuestionCreateRequest(
    @NotBlank @Size(max = 255) String title,
    @NotBlank String content,
    List<Long> tagIds) {}
