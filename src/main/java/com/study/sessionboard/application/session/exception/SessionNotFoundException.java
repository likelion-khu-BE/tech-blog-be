package com.study.sessionboard.application.session.exception;

public class SessionNotFoundException extends RuntimeException {

  public SessionNotFoundException() {
    super("해당 세션을 찾을 수 없습니다.");
  }

  public SessionNotFoundException(Long sessionId) {
    super("해당 세션을 찾을 수 없습니다. ID: " + sessionId);
  }
}
