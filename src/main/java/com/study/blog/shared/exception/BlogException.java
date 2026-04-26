package com.study.blog.shared.exception;

public class BlogException extends RuntimeException {

  private final BlogErrorCode errorCode;

  public BlogException(BlogErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public BlogErrorCode getErrorCode() {
    return errorCode;
  }
}
