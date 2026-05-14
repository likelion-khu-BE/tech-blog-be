package com.study.qna.domain.exception;

public class VoteSelfNotAllowedException extends QnaException {

  public VoteSelfNotAllowedException() {
    super("본인 답변에는 투표할 수 없습니다.");
  }
}
