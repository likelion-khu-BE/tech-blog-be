package com.study.common.aop;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Controller 및 Service 메서드 자동 로깅 Aspect.
 *
 * <p>{@code @RestController}, {@code @Service} 어노테이션이 붙은 클래스의 public 메서드 호출 시 클래스명, 메서드명, 파라미터,
 * 리턴값(또는 예외), 실행 시간(ms)을 DEBUG 레벨로 자동 기록한다.
 *
 * <p>로그 형식:
 *
 * <pre>
 * [BlogService.hello] args=[] → returned="hello blog" (2ms)
 * [BlogService.hello] args=[] → threw IllegalArgumentException: "invalid id" (1ms)
 * </pre>
 */
@Aspect
@Component
public class LoggingAspect {

  private static final ConcurrentHashMap<Class<?>, Logger> LOGGER_CACHE = new ConcurrentHashMap<>();

  @Around(
      "@within(org.springframework.web.bind.annotation.RestController)"
          + " || @within(org.springframework.stereotype.Service)")
  public Object logMethodCall(ProceedingJoinPoint pjp) throws Throwable {
    Class<?> declaringType = pjp.getSignature().getDeclaringType();
    String className = declaringType.getSimpleName();
    String methodName = pjp.getSignature().getName();
    String label = "[" + className + "." + methodName + "]";
    String args = Arrays.toString(pjp.getArgs());

    Logger log = LOGGER_CACHE.computeIfAbsent(declaringType, LoggerFactory::getLogger);

    long start = System.currentTimeMillis();
    try {
      Object result = pjp.proceed();
      long elapsed = System.currentTimeMillis() - start;
      log.debug("{} args={} \u2192 returned=\"{}\" ({}ms)", label, args, result, elapsed);
      return result;
    } catch (Throwable t) {
      long elapsed = System.currentTimeMillis() - start;
      log.debug(
          "{} args={} \u2192 threw {}: \"{}\" ({}ms)",
          label,
          args,
          t.getClass().getSimpleName(),
          t.getMessage(),
          elapsed);
      throw t;
    }
  }
}
