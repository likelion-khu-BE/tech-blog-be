package com.study.profile.application;

import com.study.profile.domain.activity.ActivityType;
import com.study.shared.event.BlogCommentCreated;
import com.study.shared.event.BlogPostCreated;
import com.study.shared.event.QnaAnswerAccepted;
import com.study.shared.event.QnaAnswerCreated;
import com.study.shared.event.QnaQuestionCreated;
import com.study.shared.event.SessionCommentCreated;
import com.study.shared.event.SessionPostCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 외부 BC 도메인 이벤트 → 활동 기록 어댑터.
 *
 * <p>각 이벤트를 받아 {@link ActivityRecorder}에 위임. 이벤트와 ActivityType 매핑만 담당, 실제 저장 로직은 recorder.
 *
 * <p>Spring 기본 {@code @EventListener}: sync + 같은 트랜잭션 + listener 실패 시 publisher 롤백 → 글 저장과 활동 기록의
 * 정합성 보장.
 */
@Component
@RequiredArgsConstructor
public class ActivityEventListener {

  private final ActivityRecorder activityRecorder;

  @EventListener
  public void onBlogPostCreated(BlogPostCreated event) {
    activityRecorder.record(event.userId(), ActivityType.blog_post, event.postId());
  }

  @EventListener
  public void onBlogCommentCreated(BlogCommentCreated event) {
    activityRecorder.record(event.userId(), ActivityType.blog_comment, event.commentId());
  }

  @EventListener
  public void onQnaQuestionCreated(QnaQuestionCreated event) {
    activityRecorder.record(event.userId(), ActivityType.qna_question, event.questionId());
  }

  @EventListener
  public void onQnaAnswerCreated(QnaAnswerCreated event) {
    activityRecorder.record(event.userId(), ActivityType.qna_answer, event.answerId());
  }

  @EventListener
  public void onQnaAnswerAccepted(QnaAnswerAccepted event) {
    activityRecorder.record(event.userId(), ActivityType.qna_accepted, event.answerId());
  }

  @EventListener
  public void onSessionPostCreated(SessionPostCreated event) {
    activityRecorder.record(event.userId(), ActivityType.session_post, event.postId());
  }

  @EventListener
  public void onSessionCommentCreated(SessionCommentCreated event) {
    activityRecorder.record(event.userId(), ActivityType.session_comment, event.commentId());
  }
}
