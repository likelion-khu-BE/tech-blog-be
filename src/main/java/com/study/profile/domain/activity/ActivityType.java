package com.study.profile.domain.activity;

/**
 * 활동마다 점수(score)가 다르게 부여된다.
 *
 * <p>blog_post : 블로그 글 작성 +10 blog_comment : 블로그 글 댓글 작성 +5 qna_question : Q&A 질문 등록 +5 qna_answer :
 * Q&A 질문에 답변 작성 +5 qna_accepted : 작성한 답변이 채택됨 (추가 점수 부여) +10 other : 기타 활동
 */
public enum ActivityType {
  blog_post,
  blog_comment,
  qna_question,
  qna_answer,
  qna_accepted,
  other
}
