package com.study.profile.application.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.profile.domain.activity.ActivityFailure;
import com.study.profile.infrastructure.ActivityFailureRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listener에서 throw된 예외를 {@link ActivityFailure} 테이블에 영구 보관.
 *
 * <p>마지막 안전망 — DB 저장 실패도 try-catch로 감싸 로그만 남김. AsyncConfig에서 AsyncUncaughtExceptionHandler가 호출.
 */
@Component
@RequiredArgsConstructor
public class ActivityFailureLogger {

  private static final Logger log = LoggerFactory.getLogger(ActivityFailureLogger.class);

  private final ActivityFailureRepository failureRepository;
  private final ObjectMapper objectMapper;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void logFailure(Object event, Throwable ex) {
    String eventType = event != null ? event.getClass().getSimpleName() : "unknown";
    String payloadJson = serialize(event);
    String errorClass = ex.getClass().getSimpleName();
    String errorMsg = ex.getMessage();

    try {
      failureRepository.save(ActivityFailure.of(eventType, payloadJson, errorClass, errorMsg));
    } catch (Exception dbEx) {
      log.error("activity_failure 저장 실패. eventType={} errorClass={}", eventType, errorClass, dbEx);
    }
  }

  private String serialize(Object event) {
    if (event == null) return "null";
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      return "{\"_serializationError\":\"" + e.getMessage() + "\"}";
    }
  }
}
