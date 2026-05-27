package com.study.sessionboard.presentation.session;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.sessionboard.application.session.SessionNoteService;
import com.study.sessionboard.presentation.dto.session.SessionNoteListResponse;
import com.study.sessionboard.presentation.dto.session.SessionNoteRequest;
import com.study.sessionboard.presentation.dto.session.SessionNoteResponse;
import java.util.List;
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
@RequestMapping("/api/session-board/{generationNumber}/sessions/{sessionId}/notes")
@RequiredArgsConstructor
public class SessionNoteController {

  private final SessionNoteService sessionNoteService;

  @GetMapping
  public ResponseEntity<SessionNoteListResponse> getNotes(
      @PathVariable Integer generationNumber,
      @PathVariable Long sessionId,
      @RequestParam(required = false) String q) {
    List<SessionNoteResponse> notes = sessionNoteService.getNotes(generationNumber, sessionId, q);
    return ResponseEntity.ok(new SessionNoteListResponse(notes));
  }

  @PostMapping
  public ResponseEntity<SessionNoteResponse> createNote(
      @PathVariable Integer generationNumber,
      @PathVariable Long sessionId,
      @RequestBody SessionNoteRequest request,
      @CurrentUser CustomUserDetails user) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(sessionNoteService.createNote(generationNumber, sessionId, request, user.userId()));
  }

  @PutMapping("/{noteId}")
  public ResponseEntity<SessionNoteResponse> updateNote(
      @PathVariable Integer generationNumber,
      @PathVariable Long sessionId,
      @PathVariable Long noteId,
      @RequestBody SessionNoteRequest request) {
    return ResponseEntity.ok(
        sessionNoteService.updateNote(generationNumber, sessionId, noteId, request));
  }

  @DeleteMapping("/{noteId}")
  public ResponseEntity<Void> deleteNote(
      @PathVariable Integer generationNumber,
      @PathVariable Long sessionId,
      @PathVariable Long noteId) {
    sessionNoteService.deleteNote(generationNumber, sessionId, noteId);
    return ResponseEntity.noContent().build();
  }
}
