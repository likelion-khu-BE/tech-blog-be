package com.study.profile.application;

import static org.mockito.Mockito.verify;

import com.study.profile.domain.activity.ActivityType;
import com.study.shared.event.BlogCommentCreated;
import com.study.shared.event.BlogPostCreated;
import com.study.shared.event.EventPostCommentCreated;
import com.study.shared.event.EventPostCreated;
import com.study.shared.event.QnaAnswerAccepted;
import com.study.shared.event.QnaAnswerCreated;
import com.study.shared.event.QnaQuestionCreated;
import com.study.shared.event.SessionNoteCreated;
import com.study.shared.event.SessionSpeakerRegistered;
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
  @DisplayName("SessionSpeakerRegistered → recorder.record(session_speak)")
  void sessionSpeakerRegistered() {
    listener.onSessionSpeakerRegistered(new SessionSpeakerRegistered(1L, 42L));
    verify(activityRecorder).record(1L, ActivityType.session_speak, 42L);
  }

  @Test
  @DisplayName("SessionNoteCreated → recorder.record(session_note)")
  void sessionNoteCreated() {
    listener.onSessionNoteCreated(new SessionNoteCreated(1L, 42L));
    verify(activityRecorder).record(1L, ActivityType.session_note, 42L);
  }

  @Test
  @DisplayName("EventPostCreated → recorder.record(session_event_post)")
  void eventPostCreated() {
    listener.onEventPostCreated(new EventPostCreated(1L, 42L));
    verify(activityRecorder).record(1L, ActivityType.session_event_post, 42L);
  }

  @Test
  @DisplayName("EventPostCommentCreated → recorder.record(session_event_comment)")
  void eventPostCommentCreated() {
    listener.onEventPostCommentCreated(new EventPostCommentCreated(1L, 42L));
    verify(activityRecorder).record(1L, ActivityType.session_event_comment, 42L);
  }
}
