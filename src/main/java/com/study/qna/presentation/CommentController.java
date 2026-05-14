package com.study.qna.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.qna.application.CommentService;
import com.study.qna.application.dto.request.comment.CommentCreateRequest;
import com.study.qna.application.dto.request.comment.CommentUpdateRequest;
import com.study.qna.application.dto.response.comment.CommentResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("qnaCommentController")
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommentController {

  private final CommentService commentService;

  @GetMapping("/answers/{answerId}/comments")
  public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long answerId) {
    return ResponseEntity.ok(commentService.getComments(answerId));
  }

  @PostMapping("/answers/{answerId}/comments")
  public ResponseEntity<CommentResponse> createComment(
      @PathVariable Long answerId,
      @RequestBody @Valid CommentCreateRequest request,
      @CurrentUser CustomUserDetails user) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(commentService.createComment(answerId, request, user.userId()));
  }

  @PatchMapping("/comments/{commentId}")
  public ResponseEntity<CommentResponse> updateComment(
      @PathVariable Long commentId,
      @Valid @RequestBody CommentUpdateRequest request,
      @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(commentService.updateComment(commentId, request, user.userId()));
  }

  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable Long commentId, @CurrentUser CustomUserDetails user) {
    commentService.deleteComment(commentId, user.userId());
    return ResponseEntity.noContent().build();
  }
}
