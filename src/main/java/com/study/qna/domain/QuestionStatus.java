package com.study.qna.domain;

/** 질문 상태 Enum과 상태 전이 규칙을 정의한다. */
public enum QuestionStatus {
  OPEN,
  RESOLVED;

  public boolean canTransitionTo(QuestionStatus next) {
    return this == OPEN && next == RESOLVED;
  }
}
