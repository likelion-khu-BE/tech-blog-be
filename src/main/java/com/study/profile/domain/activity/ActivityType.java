package com.study.profile.domain.activity;

/**
 * 활동 종류. 점수 매핑은 ActivityRecorder.scoreOf 참조.
 *
 * <p>좋아요는 양방향 — 누른 사람({@code *_like})·받은 사람({@code *_like_received}) 별도 type.
 */
public enum ActivityType {
  blog_post,
  blog_comment,
  blog_post_like,
  blog_post_like_received,
  qna_question,
  qna_answer,
  qna_accepted,
  qna_comment,
  session_speak,
  session_event_post,
  session_event_comment,
  session_event_post_like,
  session_event_post_like_received
}
