package com.study.sessionboard.application.session.exception;

public class SessionNotFoundException extends RuntimeException {

  public SessionNotFoundException() {
    super("해당 기수에서 세션을 찾을 수 없거나, 존재하지 않는 세션입니다.");
  }

  public SessionNotFoundException(Long sessionId) {
    super("해당 기수에서 세션을 찾을 수 없거나, 존재하지 않는 세션입니다. (요청 ID: " + sessionId + ")");
  }
}
