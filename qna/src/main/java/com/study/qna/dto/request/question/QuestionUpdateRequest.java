package com.study.qna.dto.request.question;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 질문 수정 요청 DTO (PATCH 방식).
 * 값이 있는 필드만 수정된다. null인 필드는 변경하지 않는다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionUpdateRequest {

    @Size(max = 255)
    private String title;

    private String content;

    private List<Long> tagIds;
}
