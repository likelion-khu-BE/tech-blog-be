package com.study.qna.application;

import com.study.profile.domain.exception.MemberNotFoundException;
import com.study.profile.infrastructure.MemberGenerationRepository;
import com.study.profile.infrastructure.MemberRepository;
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
import com.study.qna.infrastructure.CommentRepository;
import com.study.qna.infrastructure.QuestionRepository;
import com.study.shared.extevent.qna.QnaAnswerAccepted;
import com.study.shared.extevent.qna.QnaAnswerCreated;
import com.study.shared.extevent.qna.QnaAnswerDeleted;
import com.study.shared.extevent.qna.QnaAnswerUnaccepted;
import com.study.shared.extevent.qna.QnaCommentDeleted;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

  private final AnswerRepository answerRepository;
  private final QuestionRepository questionRepository;
  private final CommentRepository commentRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final MemberRepository memberRepository;
  private final MemberGenerationRepository memberGenerationRepository;

  public AnswerListResponse getAnswers(Long questionId) {
    if (!questionRepository.existsById(questionId)) {
      throw new QuestionNotFoundException(questionId);
    }

    List<Answer> answers = answerRepository.findByQuestionId(questionId);

    List<Long> userIds = answers.stream().map(Answer::getUserId).distinct().toList();
    Map<Long, MemberSummaryResponse> authorByUserId = buildAuthorMap(userIds);

    List<AnswerDetailResponse> responses =
        answers.stream()
            .map(
                a -> {
                  MemberSummaryResponse author = authorByUserId.get(a.getUserId());
                  if (author == null) throw new MemberNotFoundException(a.getUserId());
                  return AnswerDetailResponse.from(a, author);
                })
            .toList();

    return AnswerListResponse.of(responses);
  }

  @Transactional
  public AnswerDetailResponse createAnswer(
      Long questionId, AnswerCreateRequest request, Long userId) {
    Question question =
        questionRepository
            .findById(questionId)
            .orElseThrow(() -> new QuestionNotFoundException(questionId));

    if (question.getStatus() == QuestionStatus.RESOLVED) {
      throw new QuestionAlreadyClosedException(questionId);
    }

    Answer answer = Answer.create(question, userId, request.content());
    Answer saved = answerRepository.save(answer);
    questionRepository.incrementAnswerCount(questionId);

    eventPublisher.publishEvent(new QnaAnswerCreated(userId, questionId, saved.getId()));
    return AnswerDetailResponse.from(saved, buildAuthor(userId));
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
    return AnswerDetailResponse.from(answer, buildAuthor(userId));
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

    if (question.getStatus() == QuestionStatus.RESOLVED) {
      throw new QuestionAlreadyClosedException(question.getId());
    }

    answerRepository.findByQuestionId(question.getId()).stream()
        .filter(a -> a.isAccepted() && !a.getId().equals(answerId))
        .findFirst()
        .ifPresent(
            prev -> {
              prev.cancelAccept();
              eventPublisher.publishEvent(
                  new QnaAnswerUnaccepted(prev.getUserId(), question.getId(), prev.getId()));
            });

    answer.accept();
    eventPublisher.publishEvent(
        new QnaAnswerAccepted(answer.getUserId(), question.getId(), answerId));

    if (question.getStatus() == QuestionStatus.OPEN) {
      question.resolve();
    }

    return AnswerDetailResponse.from(answer, buildAuthor(answer.getUserId()));
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
    eventPublisher.publishEvent(new QnaAnswerDeleted(userId, questionId, answerId));
  }

  @Transactional
  public void deleteAnswerCascade(Answer answer) {
    Long questionId = answer.getQuestion().getId();
    Long answerId = answer.getId();

    commentRepository
        .findByAnswer_IdOrderByCreatedAtAsc(answerId)
        .forEach(
            comment -> {
              commentRepository.delete(comment);
              eventPublisher.publishEvent(
                  new QnaCommentDeleted(
                      comment.getUserId(), questionId, answerId, comment.getId()));
            });

    answerRepository.delete(answer);
    eventPublisher.publishEvent(new QnaAnswerDeleted(answer.getUserId(), questionId, answerId));
  }

  private MemberSummaryResponse buildAuthor(Long userId) {
    var member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new MemberNotFoundException(userId));
    int gen =
        memberGenerationRepository.findByMemberId(member.getId()).stream()
            .findFirst()
            .map(mg -> mg.getGeneration().getNumber())
            .orElse(0);
    return MemberSummaryResponse.of(userId, member.getName(), gen);
  }

  private Map<Long, MemberSummaryResponse> buildAuthorMap(List<Long> userIds) {
    return memberRepository.findAllByUserIdIn(userIds).stream()
        .collect(
            Collectors.toMap(
                m -> m.getUser().getId(),
                m -> {
                  int gen =
                      memberGenerationRepository.findByMemberId(m.getId()).stream()
                          .findFirst()
                          .map(mg -> mg.getGeneration().getNumber())
                          .orElse(0);
                  return MemberSummaryResponse.of(m.getUser().getId(), m.getName(), gen);
                }));
  }
}
