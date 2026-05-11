package com.study.profile.domain.activity;

/**
 * 활동 종류 + 점수 정책.
 *
 * <p>점수는 도메인 정책이라 type 자체에 캡슐화. 정책 변경 시 이 파일만 수정.
 *
 * <p>좋아요는 양방향 — 누른 사람({@code *_like})·받은 사람({@code *_like_received}) 별도 type.
 *
 * <p>점수 정책 출처: {@code docs/profile/profile-api.md} "점수 기준" 표.
 */
public enum ActivityType {
  blog_post(30),
  blog_comment(3),
  blog_post_like(1),
  blog_post_like_received(1),
  qna_question(10),
  qna_answer(10),
  qna_accepted(25),
  qna_comment(3),
  session_speak(50),
  session_event_post(30),
  session_event_comment(3),
  session_event_post_like(1),
  session_event_post_like_received(1);

  private final int score;

  ActivityType(int score) {
    this.score = score;
  }

  public int score() {
    return score;
  }
}
