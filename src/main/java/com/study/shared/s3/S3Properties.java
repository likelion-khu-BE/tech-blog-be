// com.study.shared.s3.S3Properties
package com.study.shared.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * S3 설정값 바인딩.
 *
 * <p>application.yml의 spring.cloud.aws.s3 하위 값을 타입 안전하게 주입받는다.
 * S3Config에서 @EnableConfigurationProperties(S3Properties.class)로 활성화.
 */
@ConfigurationProperties(prefix = "spring.cloud.aws.s3")
public record S3Properties(
    BucketProperties bucket, long presignedUrlExpiration, UploadConstraints upload) {

  public record BucketProperties(String event, String session) {}

  /**
   * 업로드 제약 설정.
   *
   * <ul>
   *   <li>{@code maxFileSizeBytes}: 업로드 허용 최대 파일 크기 (바이트). 기본 10MB.
   *   <li>{@code allowedExtensions}: 허용 확장자 목록 (소문자). 기본 jpg·jpeg·png·gif·webp.
   *   <li>{@code maxFilesPerRequest}: 단일 presigned URL 요청당 최대 파일 수. 기본 10개.
   * </ul>
   */
  public record UploadConstraints(
      long maxFileSizeBytes, java.util.List<String> allowedExtensions, int maxFilesPerRequest) {}
}
