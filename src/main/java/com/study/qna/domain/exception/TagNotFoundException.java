package com.study.qna.domain.exception;

public class TagNotFoundException extends QnaException {

  public TagNotFoundException(Long id) {
    super("태그를 찾을 수 없습니다: " + id);
  }
}
