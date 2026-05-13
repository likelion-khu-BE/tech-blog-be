package com.study.shared.s3;

import org.springframework.http.HttpStatus;

public enum S3ErrorCode {
  TOO_MANY_FILES(HttpStatus.BAD_REQUEST, "파일은 최대 허용 개수를 초과할 수 없습니다"),
  INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않는 파일 형식입니다"),
  FILE_NOT_UPLOADED(HttpStatus.BAD_REQUEST, "S3에 업로드된 파일을 찾을 수 없습니다"),
  FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기가 허용 범위를 초과합니다"),
  INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다"),
  UPLOAD_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "요청 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요");

  private final HttpStatus status;
  private final String message;

  S3ErrorCode(HttpStatus status, String message) {
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