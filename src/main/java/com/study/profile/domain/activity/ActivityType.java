package com.study.profile.domain.activity;

/**
 * 활동마다 점수(score)가 다르게 부여된다.
 *
 * <p>점수 정책 ({@link com.study.profile.application.ActivityRecorder} 참조):
 *
 * <ul>
 *   <li>session_speak : +20 (발표는 노력 가중)
 *   <li>blog_post / qna_accepted / session_event_post / session_note : +10
 *   <li>blog_comment / qna_question / qna_answer / session_event_comment : +5
 * </ul>
 *
 * <p>session 활동 매핑은 sessionboard 도메인 entity에 대응:
 *
 * <ul>
 *   <li>session_speak — {@code SessionSpeaker} 등록 (발표자)
 *   <li>session_note — {@code SessionNote} 작성 (발표 자료)
 *   <li>session_event_post — {@code EventPost} 작성 (행사 게시글)
 *   <li>session_event_comment — {@code EventPostComment} 작성 (행사 게시글 댓글)
 * </ul>
 */
public enum ActivityType {
  blog_post,
  blog_comment,
  qna_question,
  qna_answer,
  qna_accepted,
  session_speak,
  session_note,
  session_event_post,
  session_event_comment
}
