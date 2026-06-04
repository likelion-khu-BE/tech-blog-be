package com.study.shared.s3;

import com.study.shared.ratelimit.RateLimitService;
import java.util.List;

/**
 * 도메인별 S3 클라이언트.
 *
 * <p>버킷을 생성 시점에 고정하고 Rate Limit · 파일 검증을 내장한다. 각 도메인 Config에서 {@code @Bean}으로 등록해 사용한다.
 *
 * <pre>
 * // 등록 예시 (도메인 Config 클래스)
 * {@literal @}Bean
 * public DomainS3Client eventS3Client(S3Service s3Service, S3UploadValidator validator,
 *         RateLimitService rateLimitService, S3Properties props) {
 *     return new DomainS3Client(props.bucket().event(), s3Service, validator, rateLimitService);
 * }
 *
 * // 사용 예시 (컨트롤러)
 * {@literal @}Autowired
 * {@literal @}Qualifier("eventS3Client")
 * private DomainS3Client s3;
 *
 * return s3.issuePresignedUrls(userId, filenames);  // 발급
 *
 * // 사용 예시 (서비스)
 * String url = s3.validateAndGetUrl(key);  // 저장 시
 * s3.delete(key);                          // 삭제 시
 * String url = s3.getFileUrl(key);         // 조회 시
 * </pre>
 */
public final class DomainS3Client {

  private final String bucket;
  private final S3Service s3Service;
  private final S3UploadValidator validator;
  private final RateLimitService rateLimitService;

  public DomainS3Client(
      String bucket,
      S3Service s3Service,
      S3UploadValidator validator,
      RateLimitService rateLimitService) {
    this.bucket = bucket;
    this.s3Service = s3Service;
    this.validator = validator;
    this.rateLimitService = rateLimitService;
  }

  /**
   * presigned PUT URL 일괄 발급.
   *
   * <p>Rate Limit → 파일명(확장자·수) 검증 → URL 생성 순서를 보장한다. presigned URL 엔드포인트에서 호출한다.
   *
   * @throws S3Exception {@link S3ErrorCode#UPLOAD_RATE_LIMITED} Rate Limit 초과 시
   * @throws S3Exception {@link S3ErrorCode#TOO_MANY_FILES} 파일 수 초과 시
   * @throws S3Exception {@link S3ErrorCode#INVALID_FILE_EXTENSION} 허용되지 않는 확장자 시
   */
  public List<PresignedUrlResponse> issuePresignedUrls(Long userId, List<String> filenames) {
    if (!rateLimitService.tryConsume(userId)) {
      throw new S3Exception(S3ErrorCode.UPLOAD_RATE_LIMITED);
    }
    validator.validateFilenames(filenames);
    return s3Service.generatePresignedPutUrls(bucket, filenames);
  }

  /** prefix 지정 버전 — S3 키를 {prefix}/{UUID}.{ext} 형태로 생성. */
  public List<PresignedUrlResponse> issuePresignedUrls(
      Long userId, List<String> filenames, String prefix) {
    if (!rateLimitService.tryConsume(userId)) {
      throw new S3Exception(S3ErrorCode.UPLOAD_RATE_LIMITED);
    }
    validator.validateFilenames(filenames);
    return s3Service.generatePresignedPutUrls(bucket, prefix, filenames);
  }

  /**
   * 업로드 완료된 파일 검증 후 공개 URL 반환.
   *
   * <p>Content-Type({@code image/*}) · 파일 크기 검증 → 실패 시 S3 파일 자동 삭제. 게시글·자료 저장 직전에 호출한다.
   *
   * @throws S3Exception {@link S3ErrorCode#FILE_NOT_UPLOADED} S3에 파일 없음
   * @throws S3Exception {@link S3ErrorCode#INVALID_CONTENT_TYPE} 이미지가 아닌 파일
   * @throws S3Exception {@link S3ErrorCode#FILE_TOO_LARGE} 크기 초과 (S3 파일 자동 삭제됨)
   */
  public String validateAndGetUrl(String key) {
    validator.validateUpload(bucket, key);
    return s3Service.getFileUrl(bucket, key);
  }

  /**
   * 이미 DB에 저장된 key로 공개 URL 반환 (검증 없음).
   *
   * <p>조회 응답 DTO 조립 시 사용한다.
   */
  public String getFileUrl(String key) {
    return s3Service.getFileUrl(bucket, key);
  }

  /**
   * S3 파일 삭제.
   *
   * <p>키가 존재하지 않아도 예외를 던지지 않는다. 게시글·자료 삭제 시 DB 삭제 전에 호출한다.
   */
  public void delete(String key) {
    s3Service.delete(bucket, key);
  }
}
