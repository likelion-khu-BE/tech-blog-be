package com.study.sessionboard.presentation.session;

import com.study.sessionboard.application.session.SessionService;
import com.study.sessionboard.domain.session.SessionStatus;
import com.study.sessionboard.presentation.dto.session.SessionCreateRequest;
import com.study.sessionboard.presentation.dto.session.SessionCreateResponse;
import com.study.sessionboard.presentation.dto.session.SessionListResponse;
import com.study.sessionboard.presentation.dto.session.SessionResponse;
import com.study.sessionboard.presentation.dto.session.SessionUpdateRequest;
import com.study.sessionboard.presentation.dto.session.SessionUpdateResponse;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session-board/{generationNumber}/sessions")
@RequiredArgsConstructor
public class SessionController {

  private final SessionService sessionService;

  @GetMapping
  public ResponseEntity<SessionListResponse> getSessions(
      @PathVariable Integer generationNumber,
      @RequestParam(required = false) SessionStatus status) {
    return ResponseEntity.ok(
        new SessionListResponse(sessionService.getSessions(generationNumber, status)));
  }

  @GetMapping("/{sessionId}")
  public ResponseEntity<SessionResponse> getSession(
      @PathVariable Integer generationNumber, @PathVariable Long sessionId) {
    return ResponseEntity.ok(sessionService.getSession(generationNumber, sessionId));
  }

  @PostMapping
  public ResponseEntity<SessionCreateResponse> createSession(
      @PathVariable Integer generationNumber, @RequestBody SessionCreateRequest request) {
    Long id = sessionService.createSession(generationNumber, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(new SessionCreateResponse(id));
  }

  @PutMapping("/{sessionId}")
  public ResponseEntity<SessionUpdateResponse> updateSession(
      @PathVariable Integer generationNumber,
      @PathVariable Long sessionId,
      @RequestBody SessionUpdateRequest request) {
    OffsetDateTime updatedAt = sessionService.updateSession(generationNumber, sessionId, request);
    return ResponseEntity.ok(new SessionUpdateResponse(sessionId, updatedAt));
  }

  @DeleteMapping("/{sessionId}")
  public ResponseEntity<Void> deleteSession(
      @PathVariable Integer generationNumber, @PathVariable Long sessionId) {
    sessionService.deleteSession(generationNumber, sessionId);
    return ResponseEntity.noContent().build();
  }
}
