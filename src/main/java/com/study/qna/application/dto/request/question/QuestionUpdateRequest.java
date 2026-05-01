package com.study.qna.application.dto.request.question;

import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 질문 수정 요청 DTO(PATCH).
 */
public record QuestionUpdateRequest(
    @Size(max = 255) String title,
    String content,
    List<Long> tagIds) {}
