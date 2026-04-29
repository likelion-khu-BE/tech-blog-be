package com.study.sessionboard.presentation.event;

import com.study.sessionboard.application.event.EventPostService;
import com.study.sessionboard.application.event.dto.EventPostCreateRequest;
import com.study.sessionboard.application.event.dto.EventPostResponse;
import com.study.sessionboard.application.event.dto.EventPostSummaryResponse;
import com.study.sessionboard.application.event.dto.EventPostUpdateRequest;
import com.study.sessionboard.domain.event.EventPostType;
import com.study.sessionboard.shared.auth.MockAuth;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/events")
public class EventPostController {

  private final EventPostService eventPostService;

  public EventPostController(EventPostService eventPostService) {
    this.eventPostService = eventPostService;
  }

  @GetMapping
  public ResponseEntity<Page<EventPostSummaryResponse>> getEventPosts(
      @RequestParam(required = false) EventPostType type,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(eventPostService.getEventPosts(type, date, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EventPostResponse> getEventPost(@PathVariable Long id) {
    return ResponseEntity.ok(eventPostService.getEventPost(id));
  }

  @PostMapping
  public ResponseEntity<EventPostResponse> createEventPost(
      @Valid @RequestBody EventPostCreateRequest request) {
    EventPostResponse response =
        eventPostService.createEventPost(request, MockAuth.MOCK_MEMBER_ID);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<EventPostResponse> updateEventPost(
      @PathVariable Long id, @Valid @RequestBody EventPostUpdateRequest request) {
    return ResponseEntity.ok(eventPostService.updateEventPost(id, request, MockAuth.MOCK_MEMBER_ID));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEventPost(@PathVariable Long id) {
    eventPostService.deleteEventPost(id, MockAuth.MOCK_MEMBER_ID);
    return ResponseEntity.noContent().build();
  }
}
