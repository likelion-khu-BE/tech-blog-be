package com.study.qna.domain.exception;

public abstract class QnaException extends RuntimeException {

  protected QnaException(String message) {
    super(message);
  }
}
