package com.study.qna.domain.exception;

public class QuestionAlreadyClosedException extends QnaException {

  public QuestionAlreadyClosedException(Long questionId) {
    super("이미 종료된 질문입니다: " + questionId);
  }
}
