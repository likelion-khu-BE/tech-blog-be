package com.study.profile.application.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.profile.domain.activity.ActivityFailure;
import com.study.profile.infrastructure.ActivityFailureRepository;
import com.study.shared.extevent.blog.BlogPostCreated;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityFailureLoggerTest {

  @Mock private ActivityFailureRepository failureRepository;

  @Spy private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks private ActivityFailureLogger logger;

  @Nested
  @DisplayName("정상 흐름")
  class Normal {

    @Test
    @DisplayName("이벤트 + 예외 → ActivityFailure row 저장 (event_type, payload, error)")
    void logFailure_savesRow() {
      // Given
      BlogPostCreated event = new BlogPostCreated(1L, 42L);
      IllegalStateException ex = new IllegalStateException("Member 없음");

      // When
      logger.logFailure(event, ex);

      // Then
      ArgumentCaptor<ActivityFailure> captor = ArgumentCaptor.forClass(ActivityFailure.class);
      verify(failureRepository).save(captor.capture());
      ActivityFailure failure = captor.getValue();
      assertThat(failure.getEventType()).isEqualTo("BlogPostCreated");
      assertThat(failure.getPayloadJson()).contains("\"userId\":1").contains("\"postId\":42");
      assertThat(failure.getErrorClass()).isEqualTo("IllegalStateException");
      assertThat(failure.getErrorMsg()).isEqualTo("Member 없음");
    }
  }

  @Nested
  @DisplayName("엣지 케이스")
  class Edge {

    @Test
    @DisplayName("event가 null → eventType='unknown' + payload='null'")
    void logFailure_nullEvent() {
      // Given
      IllegalStateException ex = new IllegalStateException("trigger");

      // When
      logger.logFailure(null, ex);

      // Then
      ArgumentCaptor<ActivityFailure> captor = ArgumentCaptor.forClass(ActivityFailure.class);
      verify(failureRepository).save(captor.capture());
      ActivityFailure failure = captor.getValue();
      assertThat(failure.getEventType()).isEqualTo("unknown");
      assertThat(failure.getPayloadJson()).isEqualTo("null");
    }
  }

  @Nested
  @DisplayName("마지막 안전망 — DB 저장 실패")
  class LastResort {

    @Test
    @DisplayName("Repository.save throw → 로그만 남기고 예외 안 던짐 (cascading failure 차단)")
    void logFailure_dbSaveFails_doesNotRethrow() {
      // Given
      BlogPostCreated event = new BlogPostCreated(1L, 42L);
      RuntimeException ex = new RuntimeException("trigger");
      willThrow(new RuntimeException("DB connection lost"))
          .given(failureRepository)
          .save(any(ActivityFailure.class));

      // When + Then — 예외 전파 없이 정상 리턴
      assertThatCode(() -> logger.logFailure(event, ex)).doesNotThrowAnyException();
    }
  }
}
