package com.study.qna.domain.exception;

public class AnswerNotFoundException extends QnaException {

  public AnswerNotFoundException(Long id) {
    super("답변을 찾을 수 없습니다: " + id);
  }
}
