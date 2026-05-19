package com.study.profile.domain.activity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 활동 종류 — 점수(score) / 분류(Kind: 작성형·반응형) / 도메인(Domain: BLOG·QNA·SESSION)을 type 자체에 캡슐화.
 *
 * <p>좋아요는 양방향 — 누른 사람({@code *_like})·받은 사람({@code *_like_received}) 별도 type.
 *
 * <p>활동 read API 분리:
 *
 * <ul>
 *   <li>§6-1 stats: 작성형만 도메인별 카운트
 *   <li>§6-2 activities: 작성형 활동 목록 (누구나)
 *   <li>§6-3 reactions: 반응형 활동 목록 (본인만)
 *   <li>§6-4 ranking: 모든 type 점수 합산 (가중치 반영)
 * </ul>
 */
public enum ActivityType {
  blog_post(30, Kind.CREATION, Domain.BLOG),
  blog_comment(3, Kind.REACTION, Domain.BLOG),
  blog_post_like(1, Kind.REACTION, Domain.BLOG),
  blog_post_like_received(1, Kind.REACTION, Domain.BLOG),
  qna_question(10, Kind.CREATION, Domain.QNA),
  qna_answer(10, Kind.CREATION, Domain.QNA),
  qna_accepted(25, Kind.CREATION, Domain.QNA),
  qna_answer_upvote(1, Kind.REACTION, Domain.QNA),
  qna_answer_downvote(1, Kind.REACTION, Domain.QNA),
  qna_comment(3, Kind.REACTION, Domain.QNA),
  session_speak(50, Kind.CREATION, Domain.SESSION),
  session_event_post(30, Kind.CREATION, Domain.SESSION),
  session_event_comment(3, Kind.REACTION, Domain.SESSION),
  session_event_post_like(1, Kind.REACTION, Domain.SESSION),
  session_event_post_like_received(1, Kind.REACTION, Domain.SESSION);

  public enum Kind {
    CREATION,
    REACTION
  }

  public enum Domain {
    BLOG,
    QNA,
    SESSION
  }

  private final int score;
  private final Kind kind;
  private final Domain domain;

  ActivityType(int score, Kind kind, Domain domain) {
    this.score = score;
    this.kind = kind;
    this.domain = domain;
  }

  public int score() {
    return score;
  }

  public Kind kind() {
    return kind;
  }

  public Domain domain() {
    return domain;
  }

  /** 작성형 type 집합 — §6-2 activities 응답 필터. */
  public static Set<ActivityType> creationTypes() {
    return filterByKind(Kind.CREATION);
  }

  /** 반응형 type 집합 — §6-3 reactions 응답 필터. */
  public static Set<ActivityType> reactionTypes() {
    return filterByKind(Kind.REACTION);
  }

  private static Set<ActivityType> filterByKind(Kind kind) {
    return Collections.unmodifiableSet(
        Arrays.stream(values()).filter(t -> t.kind == kind).collect(Collectors.toSet()));
  }

  /**
   * 활동 항목 → frontend 라우트 path 매핑. 매핑이 한 곳에 캡슐화 — path 변경 시 이 메서드만 수정.
   *
   * @param referenceId 활동 row의 reference_id (Listener가 저장한 child id)
   * @param parentResourceId 부모 리소스 id (없으면 null)
   * @return frontend 라우트 path. {@code session_speak}는 page 미정으로 null
   */
  public String linkPath(Long referenceId, Long parentResourceId) {
    return switch (this) {
      case blog_post, blog_post_like, blog_post_like_received ->
          "/blog/posts/" + referenceId;
      case blog_comment ->
          parentResourceId == null
              ? null
              : "/blog/posts/" + parentResourceId + "#comment-" + referenceId;

      case qna_question -> "/qna/questions/" + referenceId;
      case qna_answer, qna_accepted, qna_answer_upvote, qna_answer_downvote ->
          parentResourceId == null
              ? null
              : "/qna/questions/" + parentResourceId + "#answer-" + referenceId;
      case qna_comment ->
          parentResourceId == null
              ? null
              : "/qna/questions/" + parentResourceId + "#comment-" + referenceId;

      case session_speak -> null; // 발표 page 미정 — v0.2
      case session_event_post, session_event_post_like, session_event_post_like_received ->
          "/session/events/" + referenceId;
      case session_event_comment ->
          parentResourceId == null
              ? null
              : "/session/events/" + parentResourceId + "#comment-" + referenceId;
    };
  }
}
