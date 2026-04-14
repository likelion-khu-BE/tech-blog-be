package com.study.qna.dto.response.answer;

import com.study.common.entity.qna.Answer;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerListResponse {

    /** 채택된 답변. 채택된 답변이 없으면 null. */
    private AnswerDetailResponse acceptedAnswer;

    /** 채택되지 않은 나머지 답변 목록 (투표순 정렬은 서비스/쿼리 레이어에서 처리). */
    private List<AnswerDetailResponse> answers;

    public static AnswerListResponse of(List<Answer> answers) {
        AnswerDetailResponse accepted = answers.stream()
            .filter(Answer::isAccepted)
            .findFirst()
            .map(AnswerDetailResponse::from)
            .orElse(null);

        List<AnswerDetailResponse> others = answers.stream()
            .filter(a -> !a.isAccepted())
            .map(AnswerDetailResponse::from)
            .toList();

        return AnswerListResponse.builder()
            .acceptedAnswer(accepted)
            .answers(others)
            .build();
    }
}
