package com.study.qna.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.study.qna.application.dto.request.comment.CommentCreateRequest;
import com.study.qna.application.dto.request.comment.CommentUpdateRequest;
import com.study.qna.application.dto.response.comment.CommentResponse;
import com.study.qna.domain.Answer;
import com.study.qna.domain.Comment;
import com.study.qna.domain.Question;
import com.study.qna.domain.exception.AnswerNotFoundException;
import com.study.qna.domain.exception.CommentNotFoundException;
import com.study.qna.domain.exception.ForbiddenQnaActionException;
import com.study.qna.infrastructure.AnswerRepository;
import com.study.qna.infrastructure.CommentRepository;
import com.study.shared.extevent.qna.QnaCommentCreated;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("QnA CommentService")
class CommentServiceTest {

  static final Long QUESTION_ID = 10L;
  static final Long ANSWER_ID = 1L;
  static final Long COMMENT_ID = 100L;
  static final Long USER_ID = 99L;
  static final Long OTHER_USER_ID = 88L;

  @Mock CommentRepository commentRepository;
  @Mock AnswerRepository answerRepository;
  @Mock ApplicationEventPublisher eventPublisher;
  @InjectMocks CommentService commentService;

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Question makeQuestion(Long id) {
    Question q = Question.create(USER_ID, "테스트 질문", "질문 내용", 13);
    ReflectionTestUtils.setField(q, "id", id);
    return q;
  }

  private Answer makeAnswer(Long id, Question question) {
    Answer a = Answer.create(question, USER_ID, "테스트 답변");
    ReflectionTestUtils.setField(a, "id", id);
    return a;
  }

  private Comment makeComment(Long id, Answer answer, Long userId) {
    Comment c = Comment.createForAnswer(userId, answer, "테스트 댓글");
    ReflectionTestUtils.setField(c, "id", id);
    return c;
  }

  // ── getComments ────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getComments")
  class GetComments {

    @Test
    @DisplayName("존재하지 않는 답변 조회 → AnswerNotFoundException")
    void answerNotFound_throwsAnswerNotFoundException() {
      when(answerRepository.existsById(ANSWER_ID)).thenReturn(false);

      assertThatThrownBy(() -> commentService.getComments(ANSWER_ID))
          .isInstanceOf(AnswerNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 없으면 빈 리스트 반환")
    void noComments_returnsEmptyList() {
      when(answerRepository.existsById(ANSWER_ID)).thenReturn(true);
      when(commentRepository.findByAnswer_IdOrderByCreatedAtAsc(ANSWER_ID)).thenReturn(List.of());

      List<CommentResponse> result = commentService.getComments(ANSWER_ID);

      assertThat(result).isEmpty();
    }
  }

  // ── createComment ──────────────────────────────────────────────────────────

  @Nested
  @DisplayName("createComment")
  class CreateComment {

    @Test
    @DisplayName("댓글 생성 성공 → 저장 + QnaCommentCreated 이벤트 발행")
    void success_savesAndPublishesEvent() {
      Question question = makeQuestion(QUESTION_ID);
      Answer answer = makeAnswer(ANSWER_ID, question);
      Comment saved = makeComment(COMMENT_ID, answer, USER_ID);

      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(answer));
      when(commentRepository.save(any())).thenReturn(saved);

      CommentResponse result =
          commentService.createComment(ANSWER_ID, new CommentCreateRequest("댓글 내용"), USER_ID);

      assertThat(result.id()).isEqualTo(COMMENT_ID);
      verify(commentRepository).save(any());
      verify(answerRepository).incrementCommentCount(ANSWER_ID);
      verify(eventPublisher).publishEvent(any(QnaCommentCreated.class));
    }

    @Test
    @DisplayName("존재하지 않는 답변에 댓글 생성 → AnswerNotFoundException, 이벤트 미발행")
    void answerNotFound_throwsAndNoEvent() {
      when(answerRepository.findById(ANSWER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  commentService.createComment(ANSWER_ID, new CommentCreateRequest("내용"), USER_ID))
          .isInstanceOf(AnswerNotFoundException.class);

      verify(eventPublisher, never()).publishEvent(any());
    }
  }

  // ── updateComment ──────────────────────────────────────────────────────────

  @Nested
  @DisplayName("updateComment")
  class UpdateComment {

    @Test
    @DisplayName("본인 댓글 수정 → content 변경 후 반환")
    void owner_updatesContent() {
      Question question = makeQuestion(QUESTION_ID);
      Answer answer = makeAnswer(ANSWER_ID, question);
      Comment comment = makeComment(COMMENT_ID, answer, USER_ID);

      when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

      CommentResponse result =
          commentService.updateComment(COMMENT_ID, new CommentUpdateRequest("수정된 내용"), USER_ID);

      assertThat(result.content()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("타인 댓글 수정 → ForbiddenQnaActionException")
    void notOwner_throwsForbidden() {
      Question question = makeQuestion(QUESTION_ID);
      Answer answer = makeAnswer(ANSWER_ID, question);
      Comment comment = makeComment(COMMENT_ID, answer, OTHER_USER_ID);

      when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

      assertThatThrownBy(
              () ->
                  commentService.updateComment(
                      COMMENT_ID, new CommentUpdateRequest("수정 시도"), USER_ID))
          .isInstanceOf(ForbiddenQnaActionException.class);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정 → CommentNotFoundException")
    void notFound_throwsCommentNotFoundException() {
      when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  commentService.updateComment(
                      COMMENT_ID, new CommentUpdateRequest("수정 시도"), USER_ID))
          .isInstanceOf(CommentNotFoundException.class);
    }
  }

  // ── deleteComment ──────────────────────────────────────────────────────────

  @Nested
  @DisplayName("deleteComment")
  class DeleteComment {

    @Test
    @DisplayName("본인 댓글 삭제 → 삭제 + commentCount 감소 + QnaCommentDeleted 이벤트 발행")
    void owner_deletesAndPublishesEvent() {
      Question question = makeQuestion(QUESTION_ID);
      Answer answer = makeAnswer(ANSWER_ID, question);
      Comment comment = makeComment(COMMENT_ID, answer, USER_ID);

      when(commentRepository.findByIdWithAnswerAndQuestion(COMMENT_ID)).thenReturn(Optional.of(comment));

      commentService.deleteComment(COMMENT_ID, USER_ID);

      verify(commentRepository).delete(comment);
      verify(answerRepository).decrementCommentCount(ANSWER_ID);
      verify(eventPublisher).publishEvent(any(QnaCommentDeleted.class));
    }

    @Test
    @DisplayName("타인 댓글 삭제 → ForbiddenQnaActionException, 이벤트 미발행")
    void notOwner_throwsForbiddenAndNoEvent() {
      Question question = makeQuestion(QUESTION_ID);
      Answer answer = makeAnswer(ANSWER_ID, question);
      Comment comment = makeComment(COMMENT_ID, answer, OTHER_USER_ID);

      when(commentRepository.findByIdWithAnswerAndQuestion(COMMENT_ID)).thenReturn(Optional.of(comment));

      assertThatThrownBy(() -> commentService.deleteComment(COMMENT_ID, USER_ID))
          .isInstanceOf(ForbiddenQnaActionException.class);

      verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 → CommentNotFoundException")
    void notFound_throwsCommentNotFoundException() {
      when(commentRepository.findByIdWithAnswerAndQuestion(COMMENT_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> commentService.deleteComment(COMMENT_ID, USER_ID))
          .isInstanceOf(CommentNotFoundException.class);
    }
  }
}
