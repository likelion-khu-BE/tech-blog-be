package com.study.blog.common.exception;

import org.springframework.http.HttpStatus;

public enum BlogErrorCode {
  POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다"),
  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다"),
  PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다"),
  FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다");

  private final HttpStatus status;
  private final String message;

  BlogErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
