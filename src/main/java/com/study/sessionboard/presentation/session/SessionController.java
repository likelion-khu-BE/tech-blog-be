package com.study.sessionboard.presentation.session;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.sessionboard.application.session.SessionService;
import com.study.sessionboard.domain.session.SessionStatus;
import com.study.sessionboard.presentation.dto.session.*;
import com.study.sessionboard.presentation.dto.session.RetroCreateRequest;
import com.study.sessionboard.presentation.dto.session.RetroResponse;
import java.util.List;
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
    return ResponseEntity.ok(sessionService.getSession(sessionId));
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
    OffsetDateTime updatedAt = sessionService.updateSession(sessionId, request);
    return ResponseEntity.ok(new SessionUpdateResponse(sessionId, updatedAt));
  }

  @DeleteMapping("/{sessionId}")
  public ResponseEntity<Void> deleteSession(
      @PathVariable Integer generationNumber, @PathVariable Long sessionId) {
    sessionService.deleteSession(sessionId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{sessionId}/resources")
  public ResponseEntity<List<ResourceResponse>> getResources(
          @PathVariable Integer generationNumber,
          @PathVariable Long sessionId,
          @RequestParam(required = false) String type) {
    return ResponseEntity.ok(sessionService.getResources(sessionId, type));
  }

  @PostMapping("/{sessionId}/resources")
  public ResponseEntity<ResourceResponse> createResource(
          @PathVariable Integer generationNumber,
          @PathVariable Long sessionId,
          @CurrentUser CustomUserDetails authUser,
          @RequestBody ResourceCreateRequest request) {
    return ResponseEntity.status(201)
            .body(sessionService.createResource(sessionId, authUser.userId(), request));
  }

  @DeleteMapping("/{sessionId}/resources/{resourceId}")
  public ResponseEntity<Void> deleteResource(
          @PathVariable Integer generationNumber,
          @PathVariable Long sessionId,
          @PathVariable Long resourceId,
          @CurrentUser CustomUserDetails authUser) {
    sessionService.deleteResource(resourceId, authUser.userId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{sessionId}/retros")
  public ResponseEntity<List<RetroResponse>> getRetros(
          @PathVariable Integer generationNumber,
          @PathVariable Long sessionId) {
    return ResponseEntity.ok(sessionService.getRetros(sessionId));
  }

  @PostMapping("/{sessionId}/retros")
  public ResponseEntity<RetroResponse> createRetro(
          @PathVariable Integer generationNumber,
          @PathVariable Long sessionId,
          @CurrentUser CustomUserDetails authUser,
          @RequestBody RetroCreateRequest request) {
    return ResponseEntity.status(201)
            .body(sessionService.createRetro(sessionId, authUser.userId(), request));
  }

  @PutMapping("/{sessionId}/retros/{retroId}")
  public ResponseEntity<RetroResponse> updateRetro(
          @PathVariable Integer generationNumber,
          @PathVariable Long sessionId,
          @PathVariable Long retroId,
          @CurrentUser CustomUserDetails authUser,
          @RequestBody RetroCreateRequest request) {
    return ResponseEntity.ok(sessionService.updateRetro(retroId, authUser.userId(), request));
  }
}
