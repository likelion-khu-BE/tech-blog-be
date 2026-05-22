package com.study.sessionboard.shared.exception;

import org.springframework.http.HttpStatus;

public enum EventPostErrorCode {
  POST_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트 게시글을 찾을 수 없습니다"),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다"),
  FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다");

  private final HttpStatus status;
  private final String message;

  EventPostErrorCode(HttpStatus status, String message) {
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
