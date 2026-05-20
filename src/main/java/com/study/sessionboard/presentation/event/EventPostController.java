package com.study.sessionboard.presentation.event;

import com.study.sessionboard.application.event.EventPostService;
import com.study.sessionboard.application.event.dto.*;
import com.study.sessionboard.domain.event.EventPostType;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session-board/{generationNumber}/event-posts") // generation number로 시현 수정
public class EventPostController {

  private final EventPostService eventPostService;

  public EventPostController(EventPostService eventPostService) {
    this.eventPostService = eventPostService;
  }

  @GetMapping
  public ResponseEntity<PageWrapper<EventPostSummaryResponse>> getEventPosts(
      @PathVariable Integer generationNumber, // generation number로 시현 수정
      @RequestParam(required = false) EventPostType type,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(eventPostService.getEventPosts(generationNumber, type, pageable));
  }

  @PostMapping("/{eventPostId}/like")
  public ResponseEntity<LikeToggleResponse> toggleLike(
          @PathVariable Long generationNumber,
          @PathVariable Long eventPostId,
          @RequestParam Long memberId) {
    return ResponseEntity.ok(eventPostService.toggleLike(eventPostId, memberId));
  }

  @GetMapping("/{eventPostId}/comments")
  public ResponseEntity<List<CommentResponse>> getComments(
          @PathVariable Long generationNumber,
          @PathVariable Long eventPostId) {
    return ResponseEntity.ok(eventPostService.getComments(eventPostId));
  }

  @PostMapping("/{eventPostId}/comments")
  public ResponseEntity<CommentResponse> createComment(
          @PathVariable Long generationNumber,
          @PathVariable Long eventPostId,
          @RequestParam Long memberId,
          @RequestBody @Valid CommentRequest request) {
    return ResponseEntity.status(201).body(eventPostService.createComment(eventPostId, memberId, request));
  }

  @PatchMapping("/{eventPostId}/comments/{commentId}")
  public ResponseEntity<Void> updateComment(
          @PathVariable Long generationNumber,
          @PathVariable Long eventPostId,
          @PathVariable Long commentId,
          @RequestParam Long memberId,
          @RequestBody @Valid CommentRequest request) {
    eventPostService.updateComment(commentId, memberId, request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{eventPostId}/comments/{commentId}")
  public ResponseEntity<Void> deleteComment(
          @PathVariable Long generationNumber,
          @PathVariable Long eventPostId,
          @PathVariable Long commentId,
          @RequestParam Long memberId) {
    eventPostService.deleteComment(commentId, memberId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{eventPostId}/comments/{commentId}/replies")
  public ResponseEntity<CommentResponse> createReply(
          @PathVariable Long generationNumber,
          @PathVariable Long eventPostId,
          @PathVariable Long commentId,
          @RequestParam Long memberId,
          @RequestBody @Valid CommentRequest request) {
    return ResponseEntity.status(201).body(eventPostService.createReply(eventPostId, commentId, memberId, request));
  }

  @PatchMapping("/{eventPostId}/comments/{commentId}/replies/{replyId}")
  public ResponseEntity<Void> updateReply(
          @PathVariable Long generationNumber,
          @PathVariable Long eventPostId,
          @PathVariable Long commentId,
          @PathVariable Long replyId,
          @RequestParam Long memberId,
          @RequestBody @Valid CommentRequest request) {
    eventPostService.updateReply(replyId, memberId, request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{eventPostId}/comments/{commentId}/replies/{replyId}")
  public ResponseEntity<Void> deleteReply(
          @PathVariable Long generationNumber,
          @PathVariable Long eventPostId,
          @PathVariable Long commentId,
          @PathVariable Long replyId,
          @RequestParam Long memberId) {
    eventPostService.deleteReply(replyId, memberId);
    return ResponseEntity.noContent().build();
  }
}
