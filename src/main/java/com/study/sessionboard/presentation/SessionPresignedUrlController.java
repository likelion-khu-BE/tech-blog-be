package com.study.sessionboard.presentation;

import com.study.shared.s3.S3Service;
import com.study.shared.s3.PresignedUrlResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionPresignedUrlController {

  private final S3Service s3Service;

  @Value("${spring.cloud.aws.s3.bucket.session}")
  private String bucket;

  @GetMapping("/presigned-urls")
  public ResponseEntity<List<PresignedUrlResponse>> getPresignedUrls(
      @RequestParam List<String> filenames) {
    return ResponseEntity.ok(s3Service.generatePresignedPutUrls(bucket, filenames));
  }
}