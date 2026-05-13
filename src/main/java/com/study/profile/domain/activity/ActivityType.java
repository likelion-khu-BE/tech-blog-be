package com.study.profile.domain.activity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 활동 종류 + 점수 정책 + 분류(Kind).
 *
 * <p>점수는 도메인 정책이라 type 자체에 캡슐화. 정책 변경 시 이 파일만 수정.
 *
 * <p>좋아요는 양방향 — 누른 사람({@code *_like})·받은 사람({@code *_like_received}) 별도 type.
 *
 * <p>Kind = 작성형(CREATION) / 반응형(REACTION) 분류. 활동 목록 조회 응답은 작성형만 노출 (반응형은 DB row + 점수 정책 유지하되 §6-1
 * 응답에서 제외). 반응형 본인 보기는 v0.2 별도 endpoint(`/me/activities/reactions`) 검토.
 *
 * <p>점수 정책 출처: {@code docs/profile/profile-api.md} "점수 기준" 표.
 */
public enum ActivityType {
  blog_post(30, Kind.CREATION),
  blog_comment(3, Kind.REACTION),
  blog_post_like(1, Kind.REACTION),
  blog_post_like_received(1, Kind.REACTION),
  qna_question(10, Kind.CREATION),
  qna_answer(10, Kind.CREATION),
  qna_accepted(25, Kind.CREATION),
  qna_comment(3, Kind.REACTION),
  session_speak(50, Kind.CREATION),
  session_event_post(30, Kind.CREATION),
  session_event_comment(3, Kind.REACTION),
  session_event_post_like(1, Kind.REACTION),
  session_event_post_like_received(1, Kind.REACTION);

  public enum Kind {
    CREATION,
    REACTION
  }

  private final int score;
  private final Kind kind;

  ActivityType(int score, Kind kind) {
    this.score = score;
    this.kind = kind;
  }

  public int score() {
    return score;
  }

  public Kind kind() {
    return kind;
  }

  public boolean isCreation() {
    return kind == Kind.CREATION;
  }

  /** 작성형 type 집합 — 활동 목록 §6-1 응답 필터. 변경 시 자동 반영. */
  public static Set<ActivityType> creationTypes() {
    return Collections.unmodifiableSet(
        Arrays.stream(values()).filter(ActivityType::isCreation).collect(Collectors.toSet()));
  }
}
