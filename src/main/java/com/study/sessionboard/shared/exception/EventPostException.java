package com.study.sessionboard.shared.exception;

public class EventPostException extends RuntimeException {

  private final EventPostErrorCode errorCode;

  public EventPostException(EventPostErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public EventPostErrorCode getErrorCode() {
    return errorCode;
  }
}
