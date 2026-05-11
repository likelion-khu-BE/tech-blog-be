package com.study.profile.application.activity;

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
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 외부 BC 도메인 이벤트 → 활동 점수 부여/차감 어댑터.
 *
 * <p>AFTER_COMMIT + Async 조합 (ADR 0003 §4): 발행자 트랜잭션 commit 후 비동기 실행 → 발행자 응답 지연 X, 트랜잭션 부담 X.
 *
 * <p>두 영역:
 *
 * <ul>
 *   <li><b>부여</b>: 생성/좋아요/채택/등록 이벤트 → {@link ActivityService#record} / {@link
 *       ActivityService#recordReceived}
 *   <li><b>차감</b>: 삭제/취소 이벤트 → {@link ActivityService#revoke} / {@link ActivityService#revokeLike}
 *       / {@link ActivityService#revokeLikeReceived}
 * </ul>
 *
 * <p>cascade 정책: 발행자 BC가 cascade 자식들도 각자 fine-grained 이벤트(예: {@link BlogPostDeleted} + {@link
 * BlogCommentDeleted} × N + {@link BlogPostUnliked} × M)로 발행. 이 어댑터는 들어온 이벤트를 1:1 처리.
 *
 * <p>채택 답변 삭제: {@link QnaAnswerUnaccepted} + {@link QnaAnswerDeleted} 두 이벤트 모두 발행되어야 채택 +25 + 답변
 * +10 모두 차감.
 */
@Component
@RequiredArgsConstructor
public class ActivityEventListener {

  private final ActivityService activityService;

  // ============================================================
  // ===== Blog =====
  // ============================================================

  // ----- 부여 -----

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBlogPostCreated(BlogPostCreated event) {
    activityService.record(event.userId(), ActivityType.blog_post, event.postId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBlogCommentCreated(BlogCommentCreated event) {
    activityService.record(event.userId(), ActivityType.blog_comment, event.commentId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBlogPostLiked(BlogPostLiked event) {
    activityService.record(event.likerId(), ActivityType.blog_post_like, event.postId());
    activityService.recordReceived(
        event.postOwnerId(), ActivityType.blog_post_like_received, event.postId(), event.likerId());
  }

  // ----- 차감 -----

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBlogPostDeleted(BlogPostDeleted event) {
    activityService.revoke(ActivityType.blog_post, event.postId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBlogCommentDeleted(BlogCommentDeleted event) {
    activityService.revoke(ActivityType.blog_comment, event.commentId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBlogPostUnliked(BlogPostUnliked event) {
    activityService.revokeLike(ActivityType.blog_post_like, event.postId(), event.likerId());
    activityService.revokeLikeReceived(
        ActivityType.blog_post_like_received, event.postId(), event.postOwnerId(), event.likerId());
  }

  // ============================================================
  // ===== QnA =====
  // ============================================================

  // ----- 부여 -----

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaQuestionCreated(QnaQuestionCreated event) {
    activityService.record(event.userId(), ActivityType.qna_question, event.questionId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaAnswerCreated(QnaAnswerCreated event) {
    activityService.record(event.userId(), ActivityType.qna_answer, event.answerId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaAnswerAccepted(QnaAnswerAccepted event) {
    activityService.record(event.userId(), ActivityType.qna_accepted, event.answerId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaCommentCreated(QnaCommentCreated event) {
    activityService.record(event.userId(), ActivityType.qna_comment, event.commentId());
  }

  // ----- 차감 -----

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaQuestionDeleted(QnaQuestionDeleted event) {
    activityService.revoke(ActivityType.qna_question, event.questionId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaAnswerDeleted(QnaAnswerDeleted event) {
    activityService.revoke(ActivityType.qna_answer, event.answerId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaAnswerUnaccepted(QnaAnswerUnaccepted event) {
    activityService.revoke(ActivityType.qna_accepted, event.answerId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaCommentDeleted(QnaCommentDeleted event) {
    activityService.revoke(ActivityType.qna_comment, event.commentId());
  }

  // ============================================================
  // ===== Session board =====
  // ============================================================

  // ----- 부여 -----

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionEventPostCreated(SessionEventPostCreated event) {
    activityService.record(event.userId(), ActivityType.session_event_post, event.postId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionEventCommentCreated(SessionEventCommentCreated event) {
    activityService.record(event.userId(), ActivityType.session_event_comment, event.commentId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionEventPostLiked(SessionEventPostLiked event) {
    activityService.record(event.likerId(), ActivityType.session_event_post_like, event.postId());
    activityService.recordReceived(
        event.postOwnerId(),
        ActivityType.session_event_post_like_received,
        event.postId(),
        event.likerId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionSpeakerRegistered(SessionSpeakerRegistered event) {
    activityService.record(event.userId(), ActivityType.session_speak, event.sessionId());
  }

  // ----- 차감 -----

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionEventPostDeleted(SessionEventPostDeleted event) {
    activityService.revoke(ActivityType.session_event_post, event.postId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionEventCommentDeleted(SessionEventCommentDeleted event) {
    activityService.revoke(ActivityType.session_event_comment, event.commentId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionEventPostUnliked(SessionEventPostUnliked event) {
    activityService.revokeLike(
        ActivityType.session_event_post_like, event.postId(), event.likerId());
    activityService.revokeLikeReceived(
        ActivityType.session_event_post_like_received,
        event.postId(),
        event.postOwnerId(),
        event.likerId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionSpeakerUnregistered(SessionSpeakerUnregistered event) {
    activityService.revoke(ActivityType.session_speak, event.sessionId());
  }
}
