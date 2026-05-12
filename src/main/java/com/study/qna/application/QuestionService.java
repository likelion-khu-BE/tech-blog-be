package com.study.qna.application;

import com.study.qna.application.dto.request.question.QuestionCreateRequest;
import com.study.qna.application.dto.request.question.QuestionSearchCondition;
import com.study.qna.application.dto.request.question.QuestionStatusUpdateRequest;
import com.study.qna.application.dto.request.question.QuestionUpdateRequest;
import com.study.qna.application.dto.response.common.MemberSummaryResponse;
import com.study.qna.application.dto.response.question.QuestionDetailResponse;
import com.study.qna.application.dto.response.question.QuestionSummaryResponse;
import com.study.qna.application.dto.response.tag.TagResponse;
import com.study.qna.domain.Question;
import com.study.qna.domain.QuestionStatus;
import com.study.qna.domain.Tag;
import com.study.qna.domain.exception.ForbiddenQnaActionException;
import com.study.qna.domain.exception.QuestionNotFoundException;
import com.study.qna.domain.exception.TagNotFoundException;
import com.study.qna.infrastructure.QuestionRepository;
import com.study.qna.infrastructure.TagRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

  private static final String TEMP_NICKNAME = "임시닉네임";

  private final QuestionRepository questionRepository;
  private final TagRepository tagRepository;

  public List<QuestionSummaryResponse> getQuestions(QuestionSearchCondition condition) {
    return questionRepository.searchQuestions(condition).stream()
        .map(this::toSummaryResponse)
        .toList();
  }

  public QuestionDetailResponse getQuestion(Long questionId) {
    Question question =
        questionRepository
            .findByIdWithTags(questionId)
            .orElseThrow(() -> new QuestionNotFoundException(questionId));

    questionRepository.incrementViewCount(questionId);
    question.incrementViewCount();

    return toDetailResponse(question);
  }

  @Transactional
  public QuestionDetailResponse createQuestion(QuestionCreateRequest request, Long userId) {
    Question question = Question.create(userId, request.title(), request.content(), 0);

    if (request.tagIds() != null && !request.tagIds().isEmpty()) {
      List<Tag> tags = fetchAndValidateTags(request.tagIds());
      tags.forEach(question::addTag);
    }

    Question saved = questionRepository.save(question);
    return toDetailResponse(saved);
  }

  @Transactional
  public QuestionDetailResponse updateQuestion(
      Long questionId, QuestionUpdateRequest request, Long userId) {
    Question question =
        questionRepository
            .findByIdWithTags(questionId)
            .orElseThrow(() -> new QuestionNotFoundException(questionId));

    if (!question.isAuthor(userId)) {
      throw new ForbiddenQnaActionException();
    }

    question.update(request.title(), request.content());

    if (request.tagIds() != null) {
      question.getQuestionTags().clear();
      if (!request.tagIds().isEmpty()) {
        List<Tag> tags = fetchAndValidateTags(request.tagIds());
        tags.forEach(question::addTag);
      }
    }

    return toDetailResponse(question);
  }

  @Transactional
  public QuestionDetailResponse closeQuestion(
      Long questionId, QuestionStatusUpdateRequest request, Long userId) {
    Question question =
        questionRepository
            .findByIdWithTags(questionId)
            .orElseThrow(() -> new QuestionNotFoundException(questionId));

    if (!question.isAuthor(userId)) {
      throw new ForbiddenQnaActionException();
    }

    QuestionStatus targetStatus = QuestionStatus.valueOf(request.status());
    if (targetStatus == QuestionStatus.CLOSED) {
      question.close();
    }

    return toDetailResponse(question);
  }

  @Transactional
  public void deleteQuestion(Long questionId, Long userId) {
    Question question =
        questionRepository
            .findByIdWithTags(questionId)
            .orElseThrow(() -> new QuestionNotFoundException(questionId));

    if (!question.isAuthor(userId)) {
      throw new ForbiddenQnaActionException();
    }

    questionRepository.delete(question);
  }

  private List<Tag> fetchAndValidateTags(List<Long> tagIds) {
    List<Tag> tags = tagRepository.findAllByIdIn(tagIds);
    if (tags.size() != tagIds.size()) {
      Set<Long> foundIds = tags.stream().map(Tag::getId).collect(Collectors.toSet());
      Long missingId =
          tagIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElseThrow();
      throw new TagNotFoundException(missingId);
    }
    return tags;
  }

  private QuestionSummaryResponse toSummaryResponse(Question question) {
    return QuestionSummaryResponse.of(
        question,
        MemberSummaryResponse.of(question.getUserId(), TEMP_NICKNAME, 0),
        question.getQuestionTags().stream().map(qt -> TagResponse.from(qt.getTag())).toList());
  }

  private QuestionDetailResponse toDetailResponse(Question question) {
    return QuestionDetailResponse.of(
        question,
        MemberSummaryResponse.of(question.getUserId(), TEMP_NICKNAME, 0),
        question.getQuestionTags().stream().map(qt -> TagResponse.from(qt.getTag())).toList());
  }
}
