package com.study.qna.dto.response.question;

import com.study.common.entity.qna.Question;
import com.study.qna.dto.response.common.MemberSummaryResponse;
import com.study.qna.dto.response.tag.TagResponse;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSummaryResponse {

    private Long id;
    private String title;
    private String status;
    private int generation;
    private int viewCount;
    private int answerCount;
    private boolean hasAcceptedAnswer;
    private MemberSummaryResponse author;
    private List<TagResponse> tags;
    private Instant createdAt;

    public static QuestionSummaryResponse from(Question question) {
        boolean hasAcceptedAnswer = question.getAnswers().stream()
            .anyMatch(a -> a.isAccepted());

        List<TagResponse> tags = question.getQuestionTags().stream()
            .map(qt -> TagResponse.from(qt.getTag()))
            .toList();

        return QuestionSummaryResponse.builder()
            .id(question.getId())
            .title(question.getTitle())
            .status(question.getStatus().name())
            .generation(question.getGeneration())
            .viewCount(question.getViewCount())
            .answerCount(question.getAnswerCount())
            .hasAcceptedAnswer(hasAcceptedAnswer)
            .author(MemberSummaryResponse.from(question.getMember()))
            .tags(tags)
            .createdAt(question.getCreatedAt())
            .build();
    }
}
