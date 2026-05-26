package com.study.sessionboard.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.config.TestcontainersConfig;
import com.study.profile.domain.generation.Generation;
import com.study.profile.infrastructure.GenerationRepository;
import com.study.sessionboard.domain.session.Session;
import com.study.sessionboard.domain.session.SessionStatus;
import com.study.sessionboard.infrastructure.session.SessionRepository;
import com.study.sessionboard.presentation.dto.session.SessionCreateRequest;
import com.study.sessionboard.presentation.dto.session.SessionUpdateRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfig.class)
class SessionIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private SessionRepository sessionRepository;
  @Autowired private GenerationRepository generationRepository;

  private Generation testGeneration;

  @BeforeEach
  void setUp() {
    testGeneration = Generation.create(13, LocalDate.now(), null, true);
    generationRepository.save(testGeneration);
  }

  @Test
  @DisplayName("세션 생성 성공")
  @WithMockUser
  void createSession_Success() throws Exception {
    SessionCreateRequest request =
        new SessionCreateRequest(
            "W1", "세션 제목", SessionStatus.SCHEDULED, OffsetDateTime.now(), List.of());

    mockMvc
        .perform(
            post("/api/session-board/13/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  @DisplayName("세션 목록 조회 성공")
  @WithMockUser
  void getSessions_Success() throws Exception {
    Session session =
        Session.create(testGeneration, "W1", "세션 1", SessionStatus.DONE, OffsetDateTime.now());
    sessionRepository.save(session);

    mockMvc
        .perform(get("/api/session-board/13/sessions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessions").isArray())
        .andExpect(jsonPath("$.sessions[0].title").value("세션 1"));
  }

  @Test
  @DisplayName("세션 목록 조회 실패 - 존재하지 않는 기수")
  @WithMockUser
  void getSessions_InvalidGeneration() throws Exception {
    mockMvc
        .perform(get("/api/session-board/999/sessions"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
    // Not checking message here because GlobalExceptionHandler might overwrite it
  }

  @Test
  @DisplayName("세션 단건 조회 성공")
  @WithMockUser
  void getSession_Success() throws Exception {
    Session session =
        Session.create(testGeneration, "W1", "세션 1", SessionStatus.DONE, OffsetDateTime.now());
    Session saved = sessionRepository.save(session);

    mockMvc
        .perform(get("/api/session-board/13/sessions/" + saved.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("세션 1"))
        .andExpect(jsonPath("$.weekLabel").value("W1"));
  }

  @Test
  @DisplayName("세션 단건 조회 실패 - 존재하지 않는 세션")
  @WithMockUser
  void getSession_NotFound() throws Exception {
    mockMvc
        .perform(get("/api/session-board/13/sessions/9999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(
            jsonPath("$.message").value("해당 기수에서 세션을 찾을 수 없거나, 존재하지 않는 세션입니다. (요청 ID: 9999)"));
  }

  @Test
  @DisplayName("세션 수정 성공")
  @WithMockUser
  void updateSession_Success() throws Exception {
    Session session =
        Session.create(testGeneration, "W1", "세션 1", SessionStatus.SCHEDULED, OffsetDateTime.now());
    Session saved = sessionRepository.save(session);

    SessionUpdateRequest request =
        new SessionUpdateRequest("W2", "수정된 제목", SessionStatus.ONGOING, null, List.of());

    mockMvc
        .perform(
            put("/api/session-board/13/sessions/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(saved.getId()))
        .andExpect(jsonPath("$.updatedAt").exists());

    Session updated = sessionRepository.findById(saved.getId()).orElseThrow();
    assertThat(updated.getTitle()).isEqualTo("수정된 제목");
    assertThat(updated.getWeekLabel()).isEqualTo("W2");
    assertThat(updated.getStatus()).isEqualTo(SessionStatus.ONGOING);
  }

  @Test
  @DisplayName("세션 삭제 성공")
  @WithMockUser
  void deleteSession_Success() throws Exception {
    Session session =
        Session.create(testGeneration, "W1", "세션 1", SessionStatus.SCHEDULED, OffsetDateTime.now());
    Session saved = sessionRepository.save(session);

    mockMvc
        .perform(delete("/api/session-board/13/sessions/" + saved.getId()))
        .andExpect(status().isNoContent());

    assertThat(sessionRepository.findById(saved.getId())).isEmpty();
  }
}
