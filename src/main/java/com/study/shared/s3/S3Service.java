// com.study.shared.s3.S3Service
package com.study.shared.s3;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * 프로젝트 전 도메인 공통 S3 유틸리티.
 *
 * <p>버킷 이름은 호출 측에서 주입한다 — 이 클래스는 어떤 버킷을 쓸지 알지 못한다. 버킷 이름은 {@link S3Properties}를 통해 도메인 서비스에서 주입받아
 * 사용한다.
 *
 * <p>제공 기능:
 *
 * <ul>
 *   <li>presigned PUT URL 생성 (배치 / 단건 + Content-Type 고정)
 *   <li>객체 공개 URL 조회
 *   <li>객체 메타데이터 조회 (HeadObject)
 *   <li>객체 삭제
 *   <li>S3 키 생성 (images/{UUID}.{ext})
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${spring.cloud.aws.region.static}")
  private String region;

  @Value("${spring.cloud.aws.s3.presigned-url-expiration}")
  private long presignedUrlExpiration;

  // ----------------------------------------------------------------
  // Presigned URL
  // ----------------------------------------------------------------

  /** 파일명 목록으로 presigned PUT URL 일괄 생성. Content-Type 미고정 — 범용 업로드용. */
  public List<PresignedUrlResponse> generatePresignedPutUrls(
      String bucket, List<String> filenames) {
    return filenames.stream()
        .map(
            filename -> {
              String key = generateKey(filename);
              String presignedUrl = presignPutUrl(bucket, key, null, presignedUrlExpiration);
              return new PresignedUrlResponse(presignedUrl, key);
            })
        .toList();
  }

  /** prefix 지정 버전 — 경로를 {prefix}/{UUID}.{ext} 형태로 생성. */
  public List<PresignedUrlResponse> generatePresignedPutUrls(
      String bucket, String prefix, List<String> filenames) {
    return filenames.stream()
        .map(
            filename -> {
              String key = generateKey(prefix, filename);
              String presignedUrl = presignPutUrl(bucket, key, null, presignedUrlExpiration);
              return new PresignedUrlResponse(presignedUrl, key);
            })
        .toList();
  }

  /**
   * 단건 presigned PUT URL 생성 (Content-Type 고정).
   *
   * <p>Content-Type을 고정하면 AWS가 업로드 요청의 Content-Type 헤더를 검증한다. 클라이언트는 반드시 동일한 Content-Type 헤더를 포함해야
   * 한다.
   *
   * @param contentType 고정할 MIME 타입 (예: "image/jpeg")
   * @param expirySeconds URL 만료 시간(초)
   */
  public String generatePresignedPutUrl(
      String bucket, String key, String contentType, long expirySeconds) {
    return presignPutUrl(bucket, key, contentType, expirySeconds);
  }

  // ----------------------------------------------------------------
  // Object operations
  // ----------------------------------------------------------------

  /** 버킷과 키로 공개 접근 URL 반환. */
  public String getFileUrl(String bucket, String key) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
  }

  /**
   * S3 객체 메타데이터 조회 (HeadObject).
   *
   * <p>반환값에서 contentType, contentLength, lastModified를 활용해 도메인 서비스에서 업로드 검증(타입·크기·시각)을 수행할 수 있다.
   *
   * @throws software.amazon.awssdk.services.s3.model.NoSuchKeyException 키가 존재하지 않으면 발생
   */
  public HeadObjectResponse headObject(String bucket, String key) {
    return s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
  }

  /** S3 객체 삭제. 키가 존재하지 않아도 예외를 던지지 않는다. */
  public void delete(String bucket, String key) {
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
  }

  // ----------------------------------------------------------------
  // Key generation
  // ----------------------------------------------------------------

  /**
   * 파일명에서 확장자를 추출해 UUID 기반 S3 키를 생성한다.
   *
   * <p>형식: {@code images/{UUID}.{ext}}
   */
  public String generateKey(String filename) {
    return "images/" + UUID.randomUUID() + extractExt(filename);
  }

  /** prefix 지정 버전 — 형식: {@code {prefix}/{UUID}.{ext}} */
  public String generateKey(String prefix, String filename) {
    return prefix + "/" + UUID.randomUUID() + extractExt(filename);
  }

  private String extractExt(String filename) {
    int dotIndex = filename.lastIndexOf('.');
    return dotIndex >= 0 ? filename.substring(dotIndex) : "";
  }

  // ----------------------------------------------------------------
  // Internal
  // ----------------------------------------------------------------

  private String presignPutUrl(String bucket, String key, String contentType, long expirySeconds) {
    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(expirySeconds))
            .putObjectRequest(
                r -> {
                  r.bucket(bucket).key(key);
                  if (contentType != null) r.contentType(contentType);
                })
            .build();
    PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
    return presigned.url().toString();
  }
}
