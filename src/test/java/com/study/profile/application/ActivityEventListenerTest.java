package com.study.profile.application;

import static org.mockito.Mockito.verify;

import com.study.profile.domain.activity.ActivityType;
import com.study.shared.event.BlogCommentCreated;
import com.study.shared.event.BlogPostCreated;
import com.study.shared.event.QnaAnswerAccepted;
import com.study.shared.event.QnaAnswerCreated;
import com.study.shared.event.QnaQuestionCreated;
import com.study.shared.event.SessionCommentCreated;
import com.study.shared.event.SessionPostCreated;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link ActivityEventListener} 단위 테스트 — Mock 기반.
 *
 * <p>각 이벤트가 올바른 ActivityType으로 {@link ActivityRecorder}에 위임되는지 검증.
 */
@ExtendWith(MockitoExtension.class)
class ActivityEventListenerTest {

  @Mock private ActivityRecorder activityRecorder;

  @InjectMocks private ActivityEventListener listener;

  @Test
  @DisplayName("BlogPostCreated → recorder.record(blog_post)")
  void blogPostCreated() {
    listener.onBlogPostCreated(new BlogPostCreated(1L, 42L));
    verify(activityRecorder).record(1L, ActivityType.blog_post, 42L);
  }

  @Test
  @DisplayName("BlogCommentCreated → recorder.record(blog_comment)")
  void blogCommentCreated() {
    listener.onBlogCommentCreated(new BlogCommentCreated(1L, 42L));
    verify(activityRecorder).record(1L, ActivityType.blog_comment, 42L);
  }

  @Test
  @DisplayName("QnaQuestionCreated → recorder.record(qna_question)")
  void qnaQuestionCreated() {
    listener.onQnaQuestionCreated(new QnaQuestionCreated(1L, 42L));
    verify(activityRecorder).record(1L, ActivityType.qna_question, 42L);
  }

  @Test
  @DisplayName("QnaAnswerCreated → recorder.record(qna_answer)")
  void qnaAnswerCreated() {
    listener.onQnaAnswerCreated(new QnaAnswerCreated(1L, 42L));
    verify(activityRecorder).record(1L, ActivityType.qna_answer, 42L);
  }

  @Test
  @DisplayName("QnaAnswerAccepted → recorder.record(qna_accepted)")
  void qnaAnswerAccepted() {
    listener.onQnaAnswerAccepted(new QnaAnswerAccepted(1L, 42L));
    verify(activityRecorder).record(1L, ActivityType.qna_accepted, 42L);
  }

  @Test
  @DisplayName("SessionPostCreated → recorder.record(session_post)")
  void sessionPostCreated() {
    listener.onSessionPostCreated(new SessionPostCreated(1L, 42L));
    verify(activityRecorder).record(1L, ActivityType.session_post, 42L);
  }

  @Test
  @DisplayName("SessionCommentCreated → recorder.record(session_comment)")
  void sessionCommentCreated() {
    listener.onSessionCommentCreated(new SessionCommentCreated(1L, 42L));
    verify(activityRecorder).record(1L, ActivityType.session_comment, 42L);
  }
}
