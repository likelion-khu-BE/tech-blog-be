package com.study.blog.presentation.comment;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.blog.application.comment.CommentService;
import com.study.blog.application.comment.dto.CommentCreateRequest;
import com.study.blog.application.comment.dto.CommentResponse;
import com.study.blog.application.comment.dto.CommentUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog")
public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping("/posts/{postId}/comments")
  public ResponseEntity<List<CommentResponse>> getComments(
      @PathVariable Long postId, @CurrentUser CustomUserDetails user) {
    Long requesterId = user != null ? user.userId() : null;
    return ResponseEntity.ok(commentService.getComments(postId, requesterId));
  }

  @PostMapping("/posts/{postId}/comments")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<CommentResponse> createComment(
      @PathVariable Long postId,
      @Valid @RequestBody CommentCreateRequest req,
      @CurrentUser CustomUserDetails user) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(commentService.createComment(postId, req, user.userId()));
  }

  @PutMapping("/comments/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<CommentResponse> updateComment(
      @PathVariable Long id,
      @Valid @RequestBody CommentUpdateRequest req,
      @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(commentService.updateComment(id, req, user.userId()));
  }

  @DeleteMapping("/comments/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<Void> deleteComment(
      @PathVariable Long id, @CurrentUser CustomUserDetails user) {
    commentService.deleteComment(id, user.userId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/comments/{id}/like")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<Map<String, Boolean>> toggleLike(
      @PathVariable Long id, @CurrentUser CustomUserDetails user) {
    boolean liked = commentService.toggleLike(id, user.userId());
    return ResponseEntity.ok(Map.of("liked", liked));
  }
}
