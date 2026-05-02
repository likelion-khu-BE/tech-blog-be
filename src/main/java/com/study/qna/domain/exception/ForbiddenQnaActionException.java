package com.study.qna.domain.exception;

public class ForbiddenQnaActionException extends QnaException {

  public ForbiddenQnaActionException() {
    super("해당 작업에 대한 권한이 없습니다.");
  }
}
