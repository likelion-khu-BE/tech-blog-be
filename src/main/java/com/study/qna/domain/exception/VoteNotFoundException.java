package com.study.qna.domain.exception;

public class VoteNotFoundException extends QnaException {

  public VoteNotFoundException() {
    super("투표를 찾을 수 없습니다.");
  }
}
