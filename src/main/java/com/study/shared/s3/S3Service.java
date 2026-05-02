package com.study.shared.s3;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${spring.cloud.aws.region.static}")
  private String region;

  @Value("${spring.cloud.aws.s3.presigned-url-expiration}")
  private long presignedUrlExpiration;

  public List<PresignedUrlResponse> generatePresignedPutUrls(
      String bucket, List<String> filenames) {
    return filenames.stream()
        .map(
            filename -> {
              String key = generateKey(filename);
              String presignedUrl = presignPutUrl(bucket, key);
              return new PresignedUrlResponse(presignedUrl, key);
            })
        .toList();
  }

  public String getFileUrl(String bucket, String key) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
  }

  public void delete(String bucket, String key) {
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
  }

  private String presignPutUrl(String bucket, String key) {
    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
            .putObjectRequest(r -> r.bucket(bucket).key(key))
            .build();

    PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
    return presigned.url().toString();
  }

  private String generateKey(String filename) {
    String ext = "";
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex >= 0) {
      ext = filename.substring(dotIndex);
    }
    return "images/" + UUID.randomUUID() + ext;
  }
}