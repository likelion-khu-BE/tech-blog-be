package com.study.sessionboard.presentation.controller;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.sessionboard.application.EventPostService;
import com.study.sessionboard.presentation.dto.EventPostCreateRequest;
import com.study.sessionboard.presentation.dto.EventPostCreateResponse;
import com.study.sessionboard.presentation.dto.EventPostResponse;
import com.study.sessionboard.presentation.dto.EventPostUpdateResponse;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session-board/{generationId}/event-posts")
@RequiredArgsConstructor
public class EventPostController {

  private final EventPostService eventPostService;

  @GetMapping("/{eventPostId}")
  public ResponseEntity<EventPostResponse> getEventPost(
      @PathVariable Long generationId, @PathVariable Long eventPostId) {
    return ResponseEntity.ok(eventPostService.getEventPost(eventPostId));
  }

  @PostMapping
  public ResponseEntity<EventPostCreateResponse> createEventPost(
      @CurrentUser CustomUserDetails user,
      @PathVariable Long generationId,
      @Valid @RequestBody EventPostCreateRequest request) {
    Long id = eventPostService.createEventPost(user.userId(), generationId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(new EventPostCreateResponse(id));
  }

  @PutMapping("/{eventPostId}")
  public ResponseEntity<EventPostUpdateResponse> updateEventPost(
      @CurrentUser CustomUserDetails user,
      @PathVariable Long generationId,
      @PathVariable Long eventPostId,
      @Valid @RequestBody EventPostCreateRequest request) {
    eventPostService.updateEventPost(user.userId(), eventPostId, request);
    return ResponseEntity.ok(new EventPostUpdateResponse(eventPostId, OffsetDateTime.now()));
  }

  @DeleteMapping("/{eventPostId}")
  public ResponseEntity<Void> deleteEventPost(
      @CurrentUser CustomUserDetails user,
      @PathVariable Long generationId,
      @PathVariable Long eventPostId) {
    eventPostService.deleteEventPost(user.userId(), eventPostId);
    return ResponseEntity.noContent().build();
  }
}
