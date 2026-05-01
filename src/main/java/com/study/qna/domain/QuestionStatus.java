package com.study.qna.domain;

/**
 * 질문 상태 Enum과 상태 전이 규칙을 정의한다.
 */
public enum QuestionStatus {
  OPEN,
  RESOLVED,
  CLOSED;

  public boolean canTransitionTo(QuestionStatus next) {
    if (this == OPEN) {
      return next == RESOLVED || next == CLOSED;
    }
    if (this == RESOLVED) {
      return next == CLOSED;
    }
    return false;
  }
}



