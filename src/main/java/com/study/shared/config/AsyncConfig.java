package com.study.shared.config;

import com.study.profile.application.activity.ActivityFailureLogger;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * {@code @Async} 활성화 + 미잡힌 예외 처리.
 *
 * <p>현재 사용처: {@code ActivityEventListener} — 외부 BC 통합 이벤트 후처리 (부여 + 차감).
 *
 * <p>{@code TaskExecutor}는 Spring Boot 자동 구성 사용. 트래픽·튜닝 필요 시점에 명시 빈 추가.
 *
 * <p>uncaught handler: async listener에서 throw된 예외를 잡아 (1) ERROR 로그 + (2) {@code activity_failure}
 * 테이블에 영구 보관. ADR 0003 §처리 실패 시 복구 전략 구현.
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

  private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

  private final ActivityFailureLogger failureLogger;

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (ex, method, params) -> {
      log.error(
          "Async listener 실패: {}.{} params={} message={}",
          method.getDeclaringClass().getSimpleName(),
          method.getName(),
          Arrays.toString(params),
          ex.getMessage(),
          ex);

      Object event = params.length > 0 ? params[0] : null;
      failureLogger.logFailure(event, ex);
    };
  }
}
