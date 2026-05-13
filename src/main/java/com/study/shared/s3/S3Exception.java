package com.study.shared.s3;

public class S3Exception extends RuntimeException {

  private final S3ErrorCode errorCode;

  public S3Exception(S3ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public S3ErrorCode getErrorCode() {
    return errorCode;
  }
}