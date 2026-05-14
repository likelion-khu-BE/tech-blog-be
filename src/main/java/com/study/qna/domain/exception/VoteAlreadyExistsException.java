package com.study.qna.domain.exception;

public class VoteAlreadyExistsException extends QnaException {

  public VoteAlreadyExistsException() {
    super("이미 투표한 답변입니다.");
  }
}
