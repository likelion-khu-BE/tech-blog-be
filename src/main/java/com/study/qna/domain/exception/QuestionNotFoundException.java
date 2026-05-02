package com.study.qna.domain.exception;

public class QuestionNotFoundException extends QnaException {

  public QuestionNotFoundException(Long id) {
    super("질문을 찾을 수 없습니다: " + id);
  }
}
