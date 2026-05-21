package com.study.sessionboard.presentation.event;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.sessionboard.application.event.EventPostService;
import com.study.sessionboard.application.event.dto.EventPostSummaryResponse;
import com.study.sessionboard.application.event.dto.PageWrapper;
import com.study.sessionboard.domain.event.EventPostType;
import com.study.sessionboard.presentation.dto.EventPostCreateRequest;
import com.study.sessionboard.presentation.dto.EventPostCreateResponse;
import com.study.sessionboard.presentation.dto.EventPostResponse;
import com.study.sessionboard.presentation.dto.EventPostUpdateResponse;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/session-board/{generationNumber}/event-posts")
@RequiredArgsConstructor
public class EventPostController {

  private final EventPostService eventPostService;

  @GetMapping
  public ResponseEntity<PageWrapper<EventPostSummaryResponse>> getEventPosts(
      @PathVariable Integer generationNumber, // generation number로 시현 수정
      @RequestParam(required = false) EventPostType type,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(eventPostService.getEventPosts(generationNumber, type, pageable));
  }

  @GetMapping("/{eventPostId}")
  public ResponseEntity<EventPostResponse> getEventPost(
      @PathVariable Integer generationNumber, @PathVariable Long eventPostId) {
    return ResponseEntity.ok(eventPostService.getEventPost(eventPostId));
  }

  @PostMapping
  public ResponseEntity<EventPostCreateResponse> createEventPost(
      @CurrentUser CustomUserDetails user,
      @PathVariable Integer generationNumber,
      @Valid @RequestBody EventPostCreateRequest request) {
    Long id = eventPostService.createEventPost(user.userId(), generationNumber, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(new EventPostCreateResponse(id));
  }

  @PutMapping("/{eventPostId}")
  public ResponseEntity<EventPostUpdateResponse> updateEventPost(
      @CurrentUser CustomUserDetails user,
      @PathVariable Integer generationNumber,
      @PathVariable Long eventPostId,
      @Valid @RequestBody EventPostCreateRequest request) {
    eventPostService.updateEventPost(user.userId(), eventPostId, request);
    return ResponseEntity.ok(new EventPostUpdateResponse(eventPostId, OffsetDateTime.now()));
  }

  @DeleteMapping("/{eventPostId}")
  public ResponseEntity<Void> deleteEventPost(
      @CurrentUser CustomUserDetails user,
      @PathVariable Integer generationNumber,
      @PathVariable Long eventPostId) {
    eventPostService.deleteEventPost(user.userId(), eventPostId);
    return ResponseEntity.noContent().build();
  }
}
