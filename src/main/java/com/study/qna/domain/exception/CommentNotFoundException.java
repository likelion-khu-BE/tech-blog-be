package com.study.qna.domain.exception;

public class CommentNotFoundException extends QnaException {

  public CommentNotFoundException(Long id) {
    super("댓글을 찾을 수 없습니다: " + id);
  }
}
