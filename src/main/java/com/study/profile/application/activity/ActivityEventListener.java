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
import com.study.shared.extevent.qna.QnaAnswerDownvoteWithdrawn;
import com.study.shared.extevent.qna.QnaAnswerDownvoted;
import com.study.shared.extevent.qna.QnaAnswerUnaccepted;
import com.study.shared.extevent.qna.QnaAnswerUpvoteWithdrawn;
import com.study.shared.extevent.qna.QnaAnswerUpvoted;
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
 *       ActivityService#recordReceived}. parent_resource_id는 댓글/답변/vote에 부모 글·질문 id 저장.
 *   <li><b>차감</b>: 삭제/취소 이벤트 → {@link ActivityService#revoke} / {@link ActivityService#revokeLike}
 *       / {@link ActivityService#revokeLikeReceived}. reference_id(child id)로 cascade.
 * </ul>
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
    activityService.record(event.userId(), ActivityType.blog_post, event.postId(), null);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBlogCommentCreated(BlogCommentCreated event) {
    activityService.record(
        event.userId(), ActivityType.blog_comment, event.commentId(), event.postId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBlogPostLiked(BlogPostLiked event) {
    activityService.record(event.likerId(), ActivityType.blog_post_like, event.postId(), null);
    activityService.recordReceived(
        event.postOwnerId(),
        ActivityType.blog_post_like_received,
        event.postId(),
        null,
        event.likerId());
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
    activityService.record(event.userId(), ActivityType.qna_question, event.questionId(), null);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaAnswerCreated(QnaAnswerCreated event) {
    activityService.record(
        event.userId(), ActivityType.qna_answer, event.answerId(), event.questionId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaAnswerAccepted(QnaAnswerAccepted event) {
    activityService.record(
        event.userId(), ActivityType.qna_accepted, event.answerId(), event.questionId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaAnswerUpvoted(QnaAnswerUpvoted event) {
    activityService.record(
        event.voterId(), ActivityType.qna_answer_upvote, event.answerId(), event.questionId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaAnswerDownvoted(QnaAnswerDownvoted event) {
    activityService.record(
        event.voterId(), ActivityType.qna_answer_downvote, event.answerId(), event.questionId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaCommentCreated(QnaCommentCreated event) {
    activityService.record(
        event.userId(), ActivityType.qna_comment, event.commentId(), event.questionId());
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
    activityService.revoke(ActivityType.qna_answer_upvote, event.answerId());
    activityService.revoke(ActivityType.qna_answer_downvote, event.answerId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaAnswerUnaccepted(QnaAnswerUnaccepted event) {
    activityService.revoke(ActivityType.qna_accepted, event.answerId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaAnswerUpvoteWithdrawn(QnaAnswerUpvoteWithdrawn event) {
    activityService.revokeLike(ActivityType.qna_answer_upvote, event.answerId(), event.voterId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onQnaAnswerDownvoteWithdrawn(QnaAnswerDownvoteWithdrawn event) {
    activityService.revokeLike(ActivityType.qna_answer_downvote, event.answerId(), event.voterId());
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
    activityService.record(event.userId(), ActivityType.session_event_post, event.postId(), null);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionEventCommentCreated(SessionEventCommentCreated event) {
    activityService.record(
        event.userId(), ActivityType.session_event_comment, event.commentId(), event.postId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionEventPostLiked(SessionEventPostLiked event) {
    activityService.record(
        event.likerId(), ActivityType.session_event_post_like, event.postId(), null);
    activityService.recordReceived(
        event.postOwnerId(),
        ActivityType.session_event_post_like_received,
        event.postId(),
        null,
        event.likerId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionSpeakerRegistered(SessionSpeakerRegistered event) {
    activityService.record(event.userId(), ActivityType.session_speak, event.sessionId(), null);
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
