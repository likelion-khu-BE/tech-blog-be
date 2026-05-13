package com.study.shared.config;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code @Async} 활성화 + 미잡힌 예외 처리.
 *
 * <p>현재 사용처: {@code ActivityEventListener} — 외부 BC 통합 이벤트 후처리.
 *
 * <p>{@code TaskExecutor}는 Spring Boot 자동 구성 사용. 트래픽·튜닝 필요 시점에 명시 빈 추가.
 *
 * <p>uncaught handler: async listener에서 throw된 예외는 호출자에 전파되지 않으므로 여기서 ERROR 로그로 잡는다. ADR 0003 §처리
 * 실패 시 복구 전략의 "단순 throw + ERROR 로그 + alert"의 로그 부분.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {

  private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (ex, method, params) ->
        log.error(
            "Async listener 실패: {}.{} params={} message={}",
            method.getDeclaringClass().getSimpleName(),
            method.getName(),
            Arrays.toString(params),
            ex.getMessage(),
            ex);
  }
}
