package com.study.qna.application.dto.request.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 질문 상태 변경 요청 DTO. */
public record QuestionStatusUpdateRequest(
    @NotBlank @Pattern(regexp = "RESOLVED", message = "status는 RESOLVED만 허용됩니다") String status) {}
