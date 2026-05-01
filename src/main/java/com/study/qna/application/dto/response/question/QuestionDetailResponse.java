package com.study.qna.application.dto.response.question;

import com.study.qna.application.dto.response.common.MemberSummaryResponse;
import com.study.qna.application.dto.response.tag.TagResponse;
import com.study.qna.domain.Question;
import com.study.qna.domain.QuestionStatus;
import java.time.Instant;
import java.util.List;

/**
 * 질문 상세 응답 DTO.
 */
public record QuestionDetailResponse(
    Long id,
    String title,
    String content,
    String status,
    int generation,
    int viewCount,
    int answerCount,
    boolean hasAcceptedAnswer,
    MemberSummaryResponse author,
    List<TagResponse> tags,
    Instant createdAt,
    Instant updatedAt) {

  public static QuestionDetailResponse of(
      Question question, MemberSummaryResponse author, List<TagResponse> tags) {
    return new QuestionDetailResponse(
        question.getId(),
        question.getTitle(),
        question.getContent(),
        question.getStatus().name(),
        question.getGeneration(),
        question.getViewCount(),
        question.getAnswerCount(),
        question.getStatus() == QuestionStatus.RESOLVED,
        author,
        tags,
        question.getCreatedAt(),
        question.getUpdatedAt());
  }
}
