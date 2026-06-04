package com.study.profile.config;

import com.study.shared.ratelimit.RateLimitService;
import com.study.shared.s3.DomainS3Client;
import com.study.shared.s3.S3Properties;
import com.study.shared.s3.S3Service;
import com.study.shared.s3.S3UploadValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProfileConfig {

  @Bean
  public DomainS3Client profileS3Client(
      S3Service s3Service,
      S3UploadValidator validator,
      RateLimitService rateLimitService,
      S3Properties props) {
    return new DomainS3Client(props.bucket().profile(), s3Service, validator, rateLimitService);
  }
}
