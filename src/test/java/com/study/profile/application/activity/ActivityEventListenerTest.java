package com.study.profile.application.activity;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.study.profile.domain.activity.ActivityType;
import com.study.shared.extevent.blog.BlogCommentCreated;
import com.study.shared.extevent.blog.BlogCommentDeleted;
import com.study.shared.extevent.blog.BlogPostCreated;
import com.study.shared.extevent.blog.BlogPostDeleted;
import com.study.shared.extevent.blog.BlogPostLiked;
import com.study.shared.extevent.blog.BlogPostUnliked;
import com.study.shared.extevent.qna.QnaAnswerAccepted;
import com.study.shared.extevent.qna.QnaAnswerCreated;
import com.study.shared.extevent.qna.QnaAnswerDeleted;
import com.study.shared.extevent.qna.QnaAnswerUnaccepted;
import com.study.shared.extevent.qna.QnaCommentCreated;
import com.study.shared.extevent.qna.QnaCommentDeleted;
import com.study.shared.extevent.qna.QnaQuestionCreated;
import com.study.shared.extevent.qna.QnaQuestionDeleted;
import com.study.shared.extevent.sessionboard.SessionEventCommentCreated;
import com.study.shared.extevent.sessionboard.SessionEventCommentDeleted;
import com.study.shared.extevent.sessionboard.SessionEventPostCreated;
import com.study.shared.extevent.sessionboard.SessionEventPostDeleted;
import com.study.shared.extevent.sessionboard.SessionEventPostLiked;
import com.study.shared.extevent.sessionboard.SessionEventPostUnliked;
import com.study.shared.extevent.sessionboard.SessionSpeakerRegistered;
import com.study.shared.extevent.sessionboard.SessionSpeakerUnregistered;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityEventListenerTest {

  @Mock private ActivityService activityService;

  @InjectMocks private ActivityEventListener listener;

  @Nested
  @DisplayName("Blog 이벤트 — 부여")
  class BlogGrant {

    @Test
    @DisplayName("BlogPostCreated → record(userId, blog_post, postId)")
    void onBlogPostCreated() {
      listener.onBlogPostCreated(new BlogPostCreated(1L, 42L));

      then(activityService).should().record(1L, ActivityType.blog_post, 42L);
    }

    @Test
    @DisplayName("BlogCommentCreated → record(userId, blog_comment, commentId)")
    void onBlogCommentCreated() {
      listener.onBlogCommentCreated(new BlogCommentCreated(1L, 7L));

      then(activityService).should().record(1L, ActivityType.blog_comment, 7L);
    }

    @Test
    @DisplayName("BlogPostLiked → 양방향 (record + recordReceived)")
    void onBlogPostLiked() {
      listener.onBlogPostLiked(new BlogPostLiked(99L, 42L, 1L));

      then(activityService).should().record(99L, ActivityType.blog_post_like, 42L);
      then(activityService)
          .should()
          .recordReceived(1L, ActivityType.blog_post_like_received, 42L, 99L);
    }
  }

  @Nested
  @DisplayName("Blog 이벤트 — 차감")
  class BlogRevoke {

    @Test
    @DisplayName("BlogPostDeleted → revoke(blog_post, postId)")
    void onBlogPostDeleted() {
      listener.onBlogPostDeleted(new BlogPostDeleted(1L, 42L));

      then(activityService).should().revoke(ActivityType.blog_post, 42L);
    }

    @Test
    @DisplayName("BlogCommentDeleted → revoke(blog_comment, commentId)")
    void onBlogCommentDeleted() {
      listener.onBlogCommentDeleted(new BlogCommentDeleted(1L, 7L));

      then(activityService).should().revoke(ActivityType.blog_comment, 7L);
    }

    @Test
    @DisplayName("BlogPostUnliked → 양방향 (revokeLike + revokeLikeReceived)")
    void onBlogPostUnliked() {
      listener.onBlogPostUnliked(new BlogPostUnliked(99L, 42L, 1L));

      then(activityService).should().revokeLike(ActivityType.blog_post_like, 42L, 99L);
      then(activityService)
          .should()
          .revokeLikeReceived(ActivityType.blog_post_like_received, 42L, 1L, 99L);
    }
  }

  @Nested
  @DisplayName("QnA 이벤트 — 부여")
  class QnaGrant {

    @Test
    @DisplayName("QnaQuestionCreated → record(userId, qna_question, questionId)")
    void onQnaQuestionCreated() {
      listener.onQnaQuestionCreated(new QnaQuestionCreated(1L, 10L));

      then(activityService).should().record(1L, ActivityType.qna_question, 10L);
    }

    @Test
    @DisplayName("QnaAnswerCreated → record(userId, qna_answer, answerId)")
    void onQnaAnswerCreated() {
      listener.onQnaAnswerCreated(new QnaAnswerCreated(1L, 20L));

      then(activityService).should().record(1L, ActivityType.qna_answer, 20L);
    }

    @Test
    @DisplayName("QnaAnswerAccepted → record(답변자, qna_accepted, answerId)")
    void onQnaAnswerAccepted() {
      listener.onQnaAnswerAccepted(new QnaAnswerAccepted(5L, 20L));

      then(activityService).should().record(5L, ActivityType.qna_accepted, 20L);
    }

    @Test
    @DisplayName("QnaCommentCreated → record(userId, qna_comment, commentId)")
    void onQnaCommentCreated() {
      listener.onQnaCommentCreated(new QnaCommentCreated(1L, 30L));

      then(activityService).should().record(1L, ActivityType.qna_comment, 30L);
    }
  }

  @Nested
  @DisplayName("QnA 이벤트 — 차감")
  class QnaRevoke {

    @Test
    @DisplayName("QnaQuestionDeleted → revoke(qna_question, questionId)")
    void onQnaQuestionDeleted() {
      listener.onQnaQuestionDeleted(new QnaQuestionDeleted(1L, 10L));

      then(activityService).should().revoke(ActivityType.qna_question, 10L);
    }

    @Test
    @DisplayName("QnaAnswerDeleted → revoke(qna_answer, answerId)")
    void onQnaAnswerDeleted() {
      listener.onQnaAnswerDeleted(new QnaAnswerDeleted(1L, 20L));

      then(activityService).should().revoke(ActivityType.qna_answer, 20L);
    }

    @Test
    @DisplayName("QnaAnswerUnaccepted → revoke(qna_accepted, answerId)")
    void onQnaAnswerUnaccepted() {
      listener.onQnaAnswerUnaccepted(new QnaAnswerUnaccepted(5L, 20L));

      then(activityService).should().revoke(ActivityType.qna_accepted, 20L);
    }

    @Test
    @DisplayName("QnaCommentDeleted → revoke(qna_comment, commentId)")
    void onQnaCommentDeleted() {
      listener.onQnaCommentDeleted(new QnaCommentDeleted(1L, 30L));

      then(activityService).should().revoke(ActivityType.qna_comment, 30L);
    }
  }

  @Nested
  @DisplayName("Session board 이벤트 — 부여")
  class SessionGrant {

    @Test
    @DisplayName("SessionEventPostCreated → record(userId, session_event_post, postId)")
    void onSessionEventPostCreated() {
      listener.onSessionEventPostCreated(new SessionEventPostCreated(1L, 50L));

      then(activityService).should().record(1L, ActivityType.session_event_post, 50L);
    }

    @Test
    @DisplayName("SessionEventCommentCreated → record(userId, session_event_comment, commentId)")
    void onSessionEventCommentCreated() {
      listener.onSessionEventCommentCreated(new SessionEventCommentCreated(1L, 60L));

      then(activityService).should().record(1L, ActivityType.session_event_comment, 60L);
    }

    @Test
    @DisplayName("SessionEventPostLiked → 양방향 (record + recordReceived)")
    void onSessionEventPostLiked() {
      listener.onSessionEventPostLiked(new SessionEventPostLiked(99L, 50L, 1L));

      then(activityService).should().record(99L, ActivityType.session_event_post_like, 50L);
      then(activityService)
          .should()
          .recordReceived(1L, ActivityType.session_event_post_like_received, 50L, 99L);
    }

    @Test
    @DisplayName("SessionSpeakerRegistered → record(userId, session_speak, sessionId)")
    void onSessionSpeakerRegistered() {
      listener.onSessionSpeakerRegistered(new SessionSpeakerRegistered(1L, 70L));

      then(activityService).should().record(1L, ActivityType.session_speak, 70L);
    }
  }

  @Nested
  @DisplayName("Session board 이벤트 — 차감")
  class SessionRevoke {

    @Test
    @DisplayName("SessionEventPostDeleted → revoke(session_event_post, postId)")
    void onSessionEventPostDeleted() {
      listener.onSessionEventPostDeleted(new SessionEventPostDeleted(1L, 50L));

      then(activityService).should().revoke(ActivityType.session_event_post, 50L);
    }

    @Test
    @DisplayName("SessionEventCommentDeleted → revoke(session_event_comment, commentId)")
    void onSessionEventCommentDeleted() {
      listener.onSessionEventCommentDeleted(new SessionEventCommentDeleted(1L, 60L));

      then(activityService).should().revoke(ActivityType.session_event_comment, 60L);
    }

    @Test
    @DisplayName("SessionEventPostUnliked → 양방향 (revokeLike + revokeLikeReceived)")
    void onSessionEventPostUnliked() {
      listener.onSessionEventPostUnliked(new SessionEventPostUnliked(99L, 50L, 1L));

      then(activityService).should().revokeLike(ActivityType.session_event_post_like, 50L, 99L);
      then(activityService)
          .should()
          .revokeLikeReceived(ActivityType.session_event_post_like_received, 50L, 1L, 99L);
    }

    @Test
    @DisplayName("SessionSpeakerUnregistered → revoke(session_speak, sessionId)")
    void onSessionSpeakerUnregistered() {
      listener.onSessionSpeakerUnregistered(new SessionSpeakerUnregistered(1L, 70L));

      then(activityService).should().revoke(ActivityType.session_speak, 70L);
    }
  }

  @Nested
  @DisplayName("교차 검증 — 잘못된 service 메서드 호출 안 함")
  class CrossCheck {

    @Test
    @DisplayName("BlogPostCreated 시 recordReceived 호출 X (자기 행위라 actor 없음)")
    void blogPostCreated_doesNotCallRecordReceived() {
      listener.onBlogPostCreated(new BlogPostCreated(1L, 42L));

      then(activityService)
          .should(never())
          .recordReceived(
              org.mockito.ArgumentMatchers.anyLong(),
              org.mockito.ArgumentMatchers.any(),
              org.mockito.ArgumentMatchers.anyLong(),
              org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("BlogPostDeleted 시 revokeLike/revokeLikeReceived 호출 X (좋아요 아님)")
    void blogPostDeleted_doesNotCallRevokeLike() {
      listener.onBlogPostDeleted(new BlogPostDeleted(1L, 42L));

      then(activityService)
          .should(never())
          .revokeLike(
              org.mockito.ArgumentMatchers.any(),
              org.mockito.ArgumentMatchers.anyLong(),
              org.mockito.ArgumentMatchers.anyLong());
    }
  }
}
