package com.study.sessionboard.presentation.event;

import com.study.sessionboard.application.event.EventPostService;
import com.study.sessionboard.application.event.dto.EventPostSummaryResponse;
import com.study.sessionboard.application.event.dto.PageWrapper;
import com.study.sessionboard.domain.event.EventPostType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session-board/{generationId}/event-posts")
public class EventPostController {

  private final EventPostService eventPostService;

  public EventPostController(EventPostService eventPostService) {
    this.eventPostService = eventPostService;
  }

  @GetMapping
  public ResponseEntity<PageWrapper<EventPostSummaryResponse>> getEventPosts(
      @PathVariable Long generationId,
      @RequestParam(required = false) EventPostType type,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(eventPostService.getEventPosts(generationId, type, pageable));
  }
}
