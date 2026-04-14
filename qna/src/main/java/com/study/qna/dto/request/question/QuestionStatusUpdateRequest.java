package com.study.qna.dto.request.question;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionStatusUpdateRequest {

    /** 변경할 상태. "CLOSED"만 허용 (OPEN→RESOLVED 전이는 채택 API로 처리). */
    @NotBlank
    private String status;
}
