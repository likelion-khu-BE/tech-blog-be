package com.study.sessionboard.controller;

import com.study.sessionboard.dto.EventPostRequest;
import com.study.sessionboard.dto.EventPostResponse;
import com.study.sessionboard.service.EventPostService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// EventPostController.java
@RestController
@RequestMapping("/api/event-posts")
@RequiredArgsConstructor
public class EventPostController {

  private final EventPostService postService;

  // POST /api/event-posts
  @PostMapping
  public ResponseEntity<EventPostResponse> create(@RequestBody EventPostRequest request) {
    return ResponseEntity.ok(postService.create(request));
  }

  // GET /api/event-posts?generationId=2
  @GetMapping
  public ResponseEntity<List<EventPostResponse>> getByGeneration(
      @RequestParam Integer generationId) {
    return ResponseEntity.ok(postService.getByGeneration(generationId));
  }

  // GET /api/event-posts/{id}
  @GetMapping("/{id}")
  public ResponseEntity<EventPostResponse> getOne(@PathVariable UUID id) {
    return ResponseEntity.ok(postService.getOne(id));
  }

  // GET /api/event-posts/my?authorId={uuid}
  @GetMapping("/my")
  public ResponseEntity<List<EventPostResponse>> getMyPosts(@RequestParam UUID authorId) {
    return ResponseEntity.ok(postService.getMyPosts(authorId));
  }

  // PUT /api/event-posts/{id}
  @PutMapping("/{id}")
  public ResponseEntity<EventPostResponse> update(
      @PathVariable UUID id, @RequestBody EventPostRequest request) {
    return ResponseEntity.ok(postService.update(id, request));
  }

  // DELETE /api/event-posts/{id}?requesterId={uuid}
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id, @RequestParam UUID requesterId) {
    postService.delete(id, requesterId);
    return ResponseEntity.noContent().build();
  }
}
