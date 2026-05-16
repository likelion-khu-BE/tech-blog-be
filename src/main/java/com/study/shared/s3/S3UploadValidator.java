package com.study.shared.s3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * S3 파일 업로드 공통 검증기.
 *
 * <p>두 시점에 검증을 수행한다:
 *
 * <ol>
 *   <li>presigned URL 발급 전 — 파일명 기반 확장자 검증 및 파일 수 제한 ({@link #validateFilenames})
 *   <li>게시글/자료 저장 전 — HeadObject 기반 실제 Content-Type·크기 검증 ({@link #validateUpload})
 * </ol>
 *
 * <p>도메인 서비스는 이 클래스를 주입받아 각 시점에 호출한다. 검증 실패 시 {@link S3Exception}을 던지며, {@code
 * GlobalExceptionHandler}가 적절한 HTTP 응답으로 변환한다.
 */
@Component
@RequiredArgsConstructor
public class S3UploadValidator {

  private final S3Properties s3Properties;
  private final S3Service s3Service;

  /**
   * presigned URL 발급 전 파일명 목록 검증.
   *
   * <ul>
   *   <li>파일 수가 {@code upload.maxFilesPerRequest}를 초과하면 {@link S3ErrorCode#TOO_MANY_FILES}
   *   <li>허용되지 않는 확장자면 {@link S3ErrorCode#INVALID_FILE_EXTENSION}
   * </ul>
   */
  public void validateFilenames(List<String> filenames) {
    int max = s3Properties.upload().maxFilesPerRequest();
    if (filenames.size() > max) {
      throw new S3Exception(S3ErrorCode.TOO_MANY_FILES);
    }

    Set<String> allowed = new HashSet<>(s3Properties.upload().allowedExtensions());
    for (String filename : filenames) {
      String ext = extractExtension(filename).toLowerCase();
      if (!allowed.contains(ext)) {
        throw new S3Exception(S3ErrorCode.INVALID_FILE_EXTENSION);
      }
    }
  }

  /**
   * 업로드 완료 후 S3 객체 검증 (HeadObject).
   *
   * <ul>
   *   <li>키가 존재하지 않으면 {@link S3ErrorCode#FILE_NOT_UPLOADED}
   *   <li>Content-Type이 {@code image/}로 시작하지 않으면 {@link S3ErrorCode#INVALID_CONTENT_TYPE} → S3 파일
   *       즉시 삭제
   *   <li>파일 크기가 {@code upload.maxFileSizeBytes}를 초과하면 {@link S3ErrorCode#FILE_TOO_LARGE} → S3 파일
   *       즉시 삭제
   * </ul>
   *
   * @param bucket 검증할 파일이 위치한 버킷
   * @param key 검증할 S3 객체 키
   */
  public void validateUpload(String bucket, String key) {
    HeadObjectResponse head;
    try {
      head = s3Service.headObject(bucket, key);
    } catch (NoSuchKeyException e) {
      throw new S3Exception(S3ErrorCode.FILE_NOT_UPLOADED);
    }

    String contentType = head.contentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      s3Service.delete(bucket, key);
      throw new S3Exception(S3ErrorCode.INVALID_CONTENT_TYPE);
    }

    if (head.contentLength() > s3Properties.upload().maxFileSizeBytes()) {
      s3Service.delete(bucket, key);
      throw new S3Exception(S3ErrorCode.FILE_TOO_LARGE);
    }
  }

  private String extractExtension(String filename) {
    int i = filename.lastIndexOf('.');
    return i >= 0 ? filename.substring(i + 1) : "";
  }
}
