package com.study.blog.shared.exception;

import org.springframework.http.HttpStatus;

public enum BlogErrorCode {
  POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다"),
  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다"),
  PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다"),
  FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다"),
  POST_NOT_PUBLISHED(HttpStatus.BAD_REQUEST, "발행된 게시글에만 북마크할 수 있습니다"),
  INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "현재 상태에서 허용되지 않는 상태 변경입니다"),
  REJECTION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "거부 사유를 입력해주세요");

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
