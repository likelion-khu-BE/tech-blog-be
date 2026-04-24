package com.study.blog.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.study.common.aop.LoggingAspect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * {@link LoggingAspect} 단위 테스트.
 *
 * <p>DB 없이 Spring AOP 컨텍스트만 띄워 Aspect가 올바른 로그 메시지를 DEBUG 레벨로 출력하는지 검증한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LoggingAspectTest.TestConfig.class)
class LoggingAspectTest {

  @Configuration
  @EnableAspectJAutoProxy
  static class TestConfig {

    @Bean
    LoggingAspect loggingAspect() {
      return new LoggingAspect();
    }

    @Bean
    DummyService dummyService() {
      return new DummyService();
    }
  }

  @Service
  static class DummyService {

    public String greet(String name) {
      return "Hello, " + name;
    }

    public void throwError() {
      throw new IllegalArgumentException("invalid id");
    }
  }

  @Autowired DummyService dummyService;

  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  @BeforeEach
  void setUp() {
    logger = (Logger) LoggerFactory.getLogger(DummyService.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    logger.setLevel(Level.DEBUG);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(listAppender);
  }

  @Test
  @DisplayName("정상 반환 시 메서드명·파라미터·리턴값·실행시간이 DEBUG 로그에 포함된다")
  void logsMethodReturnValue() {
    dummyService.greet("World");

    assertThat(listAppender.list)
        .isNotEmpty()
        .anySatisfy(
            event -> {
              assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
              String msg = event.getFormattedMessage();
              assertThat(msg).contains("[DummyService.greet]");
              assertThat(msg).contains("World");
              assertThat(msg).contains("returned");
              assertThat(msg).contains("ms)");
            });
  }

  @Test
  @DisplayName("예외 발생 시 예외 클래스명·메시지·실행시간이 DEBUG 로그에 포함되고 예외가 다시 던져진다")
  void logsExceptionAndRethrows() {
    assertThatThrownBy(() -> dummyService.throwError())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid id");

    assertThat(listAppender.list)
        .isNotEmpty()
        .anySatisfy(
            event -> {
              assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
              String msg = event.getFormattedMessage();
              assertThat(msg).contains("[DummyService.throwError]");
              assertThat(msg).contains("threw");
              assertThat(msg).contains("IllegalArgumentException");
              assertThat(msg).contains("invalid id");
              assertThat(msg).contains("ms)");
            });
  }
}
