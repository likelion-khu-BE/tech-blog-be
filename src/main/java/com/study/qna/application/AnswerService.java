package com.study.qna.application;

import com.study.qna.application.dto.request.answer.AnswerCreateRequest;
import com.study.qna.application.dto.request.answer.AnswerUpdateRequest;
import com.study.qna.application.dto.response.answer.AnswerDetailResponse;
import com.study.qna.application.dto.response.answer.AnswerListResponse;
import com.study.qna.application.dto.response.common.MemberSummaryResponse;
import com.study.qna.domain.Answer;
import com.study.qna.domain.Question;
import com.study.qna.domain.QuestionStatus;
import com.study.qna.domain.exception.AnswerNotFoundException;
import com.study.qna.domain.exception.ForbiddenQnaActionException;
import com.study.qna.domain.exception.QuestionAlreadyClosedException;
import com.study.qna.domain.exception.QuestionNotFoundException;
import com.study.qna.infrastructure.AnswerRepository;
import com.study.qna.infrastructure.QuestionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

  private static final String TEMP_NICKNAME = "임시닉네임";

  private final AnswerRepository answerRepository;
  private final QuestionRepository questionRepository;

  public AnswerListResponse getAnswers(Long questionId) {
    if (!questionRepository.existsById(questionId)) {
      throw new QuestionNotFoundException(questionId);
    }

    List<AnswerDetailResponse> answers =
        answerRepository.findByQuestionId(questionId).stream()
            .map(a -> AnswerDetailResponse.from(a, tempAuthor(a.getUserId())))
            .toList();

    return AnswerListResponse.of(answers);
  }

  @Transactional
  public AnswerDetailResponse createAnswer(
      Long questionId, AnswerCreateRequest request, Long userId) {
    Question question =
        questionRepository
            .findById(questionId)
            .orElseThrow(() -> new QuestionNotFoundException(questionId));

    if (question.getStatus() == QuestionStatus.CLOSED) {
      throw new QuestionAlreadyClosedException(questionId);
    }

    Answer answer = Answer.create(question, userId, request.content());
    Answer saved = answerRepository.save(answer);
    questionRepository.incrementAnswerCount(questionId);

    return AnswerDetailResponse.from(saved, tempAuthor(userId));
  }

  @Transactional
  public AnswerDetailResponse updateAnswer(
      Long answerId, AnswerUpdateRequest request, Long userId) {
    Answer answer =
        answerRepository
            .findById(answerId)
            .orElseThrow(() -> new AnswerNotFoundException(answerId));

    if (!answer.isAuthor(userId)) {
      throw new ForbiddenQnaActionException();
    }

    answer.update(request.content());
    return AnswerDetailResponse.from(answer, tempAuthor(userId));
  }

  @Transactional
  public AnswerDetailResponse acceptAnswer(Long answerId, Long userId) {
    Answer answer =
        answerRepository
            .findById(answerId)
            .orElseThrow(() -> new AnswerNotFoundException(answerId));

    Question question = answer.getQuestion();

    if (!question.isAuthor(userId)) {
      throw new ForbiddenQnaActionException();
    }

    if (question.getStatus() == QuestionStatus.CLOSED) {
      throw new QuestionAlreadyClosedException(question.getId());
    }

    answerRepository.findByQuestionId(question.getId()).stream()
        .filter(a -> a.isAccepted() && !a.getId().equals(answerId))
        .findFirst()
        .ifPresent(Answer::cancelAccept);

    answer.accept();

    if (question.getStatus() == QuestionStatus.OPEN) {
      question.resolve();
    }

    return AnswerDetailResponse.from(answer, tempAuthor(answer.getUserId()));
  }

  @Transactional
  public void deleteAnswer(Long answerId, Long userId) {
    Answer answer =
        answerRepository
            .findById(answerId)
            .orElseThrow(() -> new AnswerNotFoundException(answerId));

    if (!answer.isAuthor(userId)) {
      throw new ForbiddenQnaActionException();
    }

    Long questionId = answer.getQuestion().getId();
    answerRepository.delete(answer);
    questionRepository.decrementAnswerCount(questionId);
  }

  private MemberSummaryResponse tempAuthor(Long userId) {
    return MemberSummaryResponse.of(userId, TEMP_NICKNAME, 0);
  }
}