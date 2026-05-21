package com.study.qna.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.study.auth.domain.User;
import com.study.profile.domain.exception.MemberNotFoundException;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.MemberGenerationRepository;
import com.study.profile.infrastructure.MemberRepository;
import com.study.qna.application.dto.request.answer.AnswerCreateRequest;
import com.study.qna.application.dto.request.answer.AnswerUpdateRequest;
import com.study.qna.application.dto.response.answer.AnswerDetailResponse;
import com.study.qna.application.dto.response.answer.AnswerListResponse;
import com.study.qna.domain.Answer;
import com.study.qna.domain.Comment;
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
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AnswerService")
class AnswerServiceTest {

  static final Long QUESTION_ID = 1L;
  static final Long ANSWER_ID = 10L;
  static final Long OTHER_ANSWER_ID = 20L;
  static final Long USER_ID = 100L;
  static final Long OTHER_USER_ID = 200L;
  static final Long MEMBER_ID = 1000L;

  @Mock AnswerRepository answerRepository;
  @Mock QuestionRepository questionRepository;
  @Mock CommentRepository commentRepository;
  @Mock ApplicationEventPublisher eventPublisher;
  @Mock MemberRepository memberRepository;
  @Mock MemberGenerationRepository memberGenerationRepository;
  @InjectMocks AnswerService answerService;

  private Question questionWithId(Long id, Long userId) {
    Question q = Question.create(userId, "질문 제목", "질문 내용", 13);
    ReflectionTestUtils.setField(q, "id", id);
    return q;
  }

  private Question resolvedQuestion(Long id, Long userId) {
    Question q = questionWithId(id, userId);
    q.resolve();
    return q;
  }

  private Answer answerWithId(Long id, Long userId, Question question) {
    Answer a = Answer.create(question, userId, "답변 내용");
    ReflectionTestUtils.setField(a, "id", id);
    return a;
  }

  private Answer acceptedAnswerWithId(Long id, Long userId, Question question) {
    Answer a = answerWithId(id, userId, question);
    a.accept();
    return a;
  }

  private void stubAuthorSingle(Long userId) {
    Member member = mock(Member.class);
    when(member.getId()).thenReturn(MEMBER_ID);
    when(member.getName()).thenReturn("테스트유저");
    when(memberRepository.findByUserId(userId)).thenReturn(Optional.of(member));
    when(memberGenerationRepository.findByMemberId(MEMBER_ID)).thenReturn(List.of());
  }

  private void stubAuthorBatch(Long userId) {
    Member member = mock(Member.class);
    User user = mock(User.class);
    when(user.getId()).thenReturn(userId);
    when(member.getId()).thenReturn(MEMBER_ID);
    when(member.getName()).thenReturn("테스트유저");
    when(member.getUser()).thenReturn(user);
    when(memberRepository.findAllByUserIdIn(anyList())).thenReturn(List.of(member));
    when(memberGenerationRepository.findByMemberId(MEMBER_ID)).thenReturn(List.of());
  }

  @Nested
  @DisplayName("getAnswers")
  class GetAnswers {

    @Test
    @DisplayName("존재하지 않는 질문 → QuestionNotFoundException")
    void questionNotFound_throws() {
      when(questionRepository.existsById(QUESTION_ID)).thenReturn(false);

      assertThatThrownBy(() -> answerService.getAnswers(QUESTION_ID))
          .isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    @DisplayName("답변 없으면 빈 목록 반환")
    void noAnswers_returnsEmpty() {
      when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
      when(answerRepository.findByQuestionId(QUESTION_ID)).thenReturn(List.of());
      when(memberRepository.findAllByUserIdIn(anyList())).thenReturn(List.of());

      AnswerListResponse res = answerService.getAnswers(QUESTION_ID);

      assertThat(res.answers()).isEmpty();
      assertThat(res.acceptedAnswer()).isNull();
    }

    @Test
    @DisplayName("채택 답변은 acceptedAnswer로, 나머지는 answers로 분리")
    void withAcceptedAnswer_separatesCorrectly() {
      Question question = questionWithId(QUESTION_ID, USER_ID);
      Answer accepted = acceptedAnswerWithId(ANSWER_ID, USER_ID, question);
      Answer normal = answerWithId(OTHER_ANSWER_ID, USER_ID, question);

      when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
      when(answerRepository.findByQuestionId(QUESTION_ID)).thenReturn(List.of(accepted, normal));
      stubAuthorBatch(USER_ID);

      AnswerListResponse res = answerService.getAnswers(QUESTION_ID);

      assertThat(res.acceptedAnswer()).isNotNull();
      assertThat(res.acceptedAnswer().accepted()).isTrue();
      assertThat(res.answers()).hasSize(1);
      assertThat(res.answers().get(0).accepted()).isFalse();
    }

    @Test
    @DisplayName("Member가 없는 userId → MemberNotFoundException")
    void memberNotFound_throws() {
      Question question = questionWithId(QUESTION_ID, USER_ID);
      Answer answer = answerWithId(ANSWER_ID, USER_ID, question);

      when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
      when(answerRepository.findByQuestionId(QUESTION_ID)).thenReturn(List.of(answer));
      when(memberRepository.findAllByUserIdIn(anyList())).thenReturn(List.of());

      assertThatThrownBy(() -> answerService.getAnswers(QUESTION_ID))
          .isInstanceOf(MemberNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("createAnswer")
  class CreateAnswer {

    @Test
    @DisplayName("정상 등록 → 저장, answerCount 증가, QnaAnswerCreated 발행")
    void normal_savesAndPublishesEvent() {
      Question question = questionWithId(QUESTION_ID, OTHER_USER_ID);
      Answer saved = answerWithId(ANSWER_ID, USER_ID, question);

      when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
      when(answerRepository.save(any())).thenReturn(saved);
      stubAuthorSingle(USER_ID);

      AnswerDetailResponse res =
          answerService.createAnswer(QUESTION_ID, new AnswerCreateRequest("답변 내용"), USER_ID);

      assertThat(res.id()).isEqualTo(ANSWER_ID);
      verify(questionRepository).incrementAnswerCount(QUESTION_ID);
      verify(eventPublisher).publishEvent(any(QnaAnswerCreated.class));
    }

    @Test
    @DisplayName("존재하지 않는 질문 → QuestionNotFoundException")
    void questionNotFound_throws() {
      when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> answerService.createAnswer(QUESTION_ID, new AnswerCreateRequest("내용"), USER_ID))
          .isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    @DisplayName("RESOLVED 질문 → QuestionAlreadyClosedException, 저장 미호출")
    void resolvedQuestion_throws() {
      Question question = resolvedQuestion(QUESTION_ID, OTHER_USER_ID);
      when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));

      assertThatThrownBy(
              () -> answerService.createAnswer(QUESTION_ID, new AnswerCreateRequest("내용"), USER_ID))
          .isInstanceOf(QuestionAlreadyClosedException.class);

      verify(answerRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("updateAnswer")
  class UpdateAnswer {

    @Test
    @DisplayName("본인 답변 수정 → content 변경")
    void owner_updatesContent() {
      Question question = questionWithId(QUESTION_ID, OTHER_USER_ID);
      Answer answer = answerWithId(ANSWER_ID, USER_ID, question);

      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(answer));
      stubAuthorSingle(USER_ID);

      AnswerDetailResponse res =
          answerService.updateAnswer(ANSWER_ID, new AnswerUpdateRequest("수정된 내용"), USER_ID);

      assertThat(res.content()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("존재하지 않는 답변 → AnswerNotFoundException")
    void answerNotFound_throws() {
      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> answerService.updateAnswer(ANSWER_ID, new AnswerUpdateRequest("내용"), USER_ID))
          .isInstanceOf(AnswerNotFoundException.class);
    }

    @Test
    @DisplayName("타인 답변 수정 → ForbiddenQnaActionException")
    void notOwner_throws() {
      Question question = questionWithId(QUESTION_ID, OTHER_USER_ID);
      Answer answer = answerWithId(ANSWER_ID, USER_ID, question);

      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(answer));

      assertThatThrownBy(
              () ->
                  answerService.updateAnswer(
                      ANSWER_ID, new AnswerUpdateRequest("내용"), OTHER_USER_ID))
          .isInstanceOf(ForbiddenQnaActionException.class);
    }
  }

  @Nested
  @DisplayName("acceptAnswer")
  class AcceptAnswer {

    @Test
    @DisplayName("정상 채택 → accepted true, QnaAnswerAccepted 발행, 질문 RESOLVED 전이")
    void normal_acceptsAndPublishesEvent() {
      Question question = questionWithId(QUESTION_ID, USER_ID);
      Answer answer = answerWithId(ANSWER_ID, OTHER_USER_ID, question);

      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(answer));
      when(answerRepository.findByQuestionId(QUESTION_ID)).thenReturn(List.of(answer));
      stubAuthorSingle(OTHER_USER_ID);

      AnswerDetailResponse res = answerService.acceptAnswer(ANSWER_ID, USER_ID);

      assertThat(res.accepted()).isTrue();
      assertThat(question.getStatus()).isEqualTo(QuestionStatus.RESOLVED);
      verify(eventPublisher).publishEvent(any(QnaAnswerAccepted.class));
    }

    @Test
    @DisplayName("기존 채택 답변 있으면 cancelAccept + QnaAnswerUnaccepted 발행")
    void previousAccepted_cancelsAndPublishesUnaccepted() {
      Question question = questionWithId(QUESTION_ID, USER_ID);
      Answer prevAccepted = acceptedAnswerWithId(OTHER_ANSWER_ID, OTHER_USER_ID, question);
      Answer newAnswer = answerWithId(ANSWER_ID, OTHER_USER_ID, question);

      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(newAnswer));
      when(answerRepository.findByQuestionId(QUESTION_ID))
          .thenReturn(List.of(prevAccepted, newAnswer));
      stubAuthorSingle(OTHER_USER_ID);

      answerService.acceptAnswer(ANSWER_ID, USER_ID);

      assertThat(prevAccepted.isAccepted()).isFalse();
      verify(eventPublisher).publishEvent(any(QnaAnswerUnaccepted.class));
      verify(eventPublisher).publishEvent(any(QnaAnswerAccepted.class));
    }

    @Test
    @DisplayName("질문 작성자가 아닌 경우 → ForbiddenQnaActionException")
    void notQuestionAuthor_throws() {
      Question question = questionWithId(QUESTION_ID, USER_ID);
      Answer answer = answerWithId(ANSWER_ID, OTHER_USER_ID, question);

      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(answer));

      assertThatThrownBy(() -> answerService.acceptAnswer(ANSWER_ID, OTHER_USER_ID))
          .isInstanceOf(ForbiddenQnaActionException.class);
    }

    @Test
    @DisplayName("이미 RESOLVED 질문 → QuestionAlreadyClosedException")
    void alreadyResolved_throws() {
      Question question = resolvedQuestion(QUESTION_ID, USER_ID);
      Answer answer = answerWithId(ANSWER_ID, OTHER_USER_ID, question);

      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(answer));

      assertThatThrownBy(() -> answerService.acceptAnswer(ANSWER_ID, USER_ID))
          .isInstanceOf(QuestionAlreadyClosedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 답변 → AnswerNotFoundException")
    void answerNotFound_throws() {
      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> answerService.acceptAnswer(ANSWER_ID, USER_ID))
          .isInstanceOf(AnswerNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("deleteAnswer")
  class DeleteAnswer {

    @Test
    @DisplayName("본인 답변 삭제 → 삭제, answerCount 감소, QnaAnswerDeleted 발행")
    void owner_deletesAndPublishesEvent() {
      Question question = questionWithId(QUESTION_ID, OTHER_USER_ID);
      Answer answer = answerWithId(ANSWER_ID, USER_ID, question);

      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(answer));

      answerService.deleteAnswer(ANSWER_ID, USER_ID);

      verify(answerRepository).delete(answer);
      verify(questionRepository).decrementAnswerCount(QUESTION_ID);
      verify(eventPublisher).publishEvent(any(QnaAnswerDeleted.class));
    }

    @Test
    @DisplayName("존재하지 않는 답변 → AnswerNotFoundException")
    void answerNotFound_throws() {
      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> answerService.deleteAnswer(ANSWER_ID, USER_ID))
          .isInstanceOf(AnswerNotFoundException.class);
    }

    @Test
    @DisplayName("타인 답변 삭제 → ForbiddenQnaActionException, 삭제 미호출")
    void notOwner_throws() {
      Question question = questionWithId(QUESTION_ID, OTHER_USER_ID);
      Answer answer = answerWithId(ANSWER_ID, USER_ID, question);

      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(answer));

      assertThatThrownBy(() -> answerService.deleteAnswer(ANSWER_ID, OTHER_USER_ID))
          .isInstanceOf(ForbiddenQnaActionException.class);

      verify(answerRepository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("deleteAnswerCascade")
  class DeleteAnswerCascade {

    @Test
    @DisplayName("댓글 있으면 댓글 삭제 + QnaCommentDeleted 발행 후 답변 삭제 + QnaAnswerDeleted 발행")
    void withComments_deletesCommentsAndAnswer() {
      Question question = questionWithId(QUESTION_ID, OTHER_USER_ID);
      Answer answer = answerWithId(ANSWER_ID, USER_ID, question);
      Comment comment = Comment.createForAnswer(OTHER_USER_ID, answer, "댓글");
      ReflectionTestUtils.setField(comment, "id", 999L);

      when(commentRepository.findByAnswer_IdOrderByCreatedAtAsc(ANSWER_ID))
          .thenReturn(List.of(comment));

      answerService.deleteAnswerCascade(answer);

      verify(commentRepository).delete(comment);
      verify(eventPublisher).publishEvent(any(QnaCommentDeleted.class));
      verify(answerRepository).delete(answer);
      verify(eventPublisher).publishEvent(any(QnaAnswerDeleted.class));
    }

    @Test
    @DisplayName("댓글 없으면 답변만 삭제 + QnaAnswerDeleted 발행")
    void noComments_deletesOnlyAnswer() {
      Question question = questionWithId(QUESTION_ID, OTHER_USER_ID);
      Answer answer = answerWithId(ANSWER_ID, USER_ID, question);

      when(commentRepository.findByAnswer_IdOrderByCreatedAtAsc(ANSWER_ID)).thenReturn(List.of());

      answerService.deleteAnswerCascade(answer);

      verify(commentRepository, never()).delete(any());
      verify(answerRepository).delete(answer);
      verify(eventPublisher).publishEvent(any(QnaAnswerDeleted.class));
    }
  }
}
