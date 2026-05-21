package com.study.qna.domain.exception;

public class AnswerNotAcceptedException extends QnaException {

  public AnswerNotAcceptedException(Long id) {
    super("채택된 답변이 아닙니다: " + id);
  }
}
