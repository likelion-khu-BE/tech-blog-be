package com.study.qna.application;

import com.study.qna.application.dto.request.question.QuestionCreateRequest;
import com.study.qna.application.dto.response.common.MemberSummaryResponse;
import com.study.qna.application.dto.response.question.QuestionDetailResponse;
import com.study.qna.application.dto.response.question.QuestionSummaryResponse;
import com.study.qna.application.dto.response.tag.TagResponse;
import com.study.qna.domain.Question;
import com.study.qna.domain.exception.ForbiddenQnaActionException;
import com.study.qna.domain.exception.QuestionNotFoundException;
import com.study.qna.infrastructure.QuestionRepository;
import com.study.qna.infrastructure.TagRepository;
import java.util.List;
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

  public List<QuestionSummaryResponse> getQuestions() {
    return questionRepository.findAllWithTags().stream().map(this::toSummaryResponse).toList();
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
      tagRepository.findAllByIdIn(request.tagIds()).forEach(question::addTag);
    }

    Question saved = questionRepository.save(question);
    return toDetailResponse(saved);
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
