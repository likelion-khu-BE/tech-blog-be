package com.study.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * blog 모듈 독립 부트스트랩 — blog 통합 테스트 전용.
 *
 * <p>주의: scanBasePackages를 com.study.blog로 제한해야 한다. 기본값(이 클래스가 속한 패키지 + 하위)으로 두면 Spring이
 * 클래스패스의 다른 모듈 빈까지 스캔해 app 모듈 테스트와 Bean 이름 충돌(ConflictingBeanDefinitionException) 발생.
 */
@SpringBootApplication(scanBasePackages = "com.study.blog")
@EntityScan({"com.study.blog", "com.study.common.entity"})
public class BlogTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(BlogTestApplication.class, args);
  }
}
