package com.study.qna.domain.exception;

public class TagAlreadyExistsException extends QnaException {

  public TagAlreadyExistsException(String name) {
    super("이미 존재하는 태그입니다: " + name);
  }
}
