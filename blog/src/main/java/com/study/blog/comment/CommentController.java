package com.study.blog.comment;

import com.study.blog.comment.dto.CommentCreateRequest;
import com.study.blog.comment.dto.CommentResponse;
import com.study.blog.comment.dto.CommentUpdateRequest;
import com.study.blog.common.ApiResponse;
import com.study.blog.common.auth.MockAuth;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog")
public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping("/posts/{postId}/comments")
  public ApiResponse<List<CommentResponse>> getComments(@PathVariable Long postId) {
    UUID userId = MockAuth.MOCK_USER_ID;
    return ApiResponse.success(commentService.getComments(postId, userId));
  }

  @PostMapping("/posts/{postId}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CommentResponse> createComment(
      @PathVariable Long postId, @Valid @RequestBody CommentCreateRequest req) {
    UUID userId = MockAuth.MOCK_USER_ID;
    return ApiResponse.success(commentService.createComment(postId, req, userId));
  }

  @PutMapping("/comments/{id}")
  public ApiResponse<CommentResponse> updateComment(
      @PathVariable Long id, @Valid @RequestBody CommentUpdateRequest req) {
    UUID userId = MockAuth.MOCK_USER_ID;
    return ApiResponse.success(commentService.updateComment(id, req, userId));
  }

  @DeleteMapping("/comments/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteComment(@PathVariable Long id) {
    UUID userId = MockAuth.MOCK_USER_ID;
    commentService.deleteComment(id, userId);
  }

  @PostMapping("/comments/{id}/like")
  public ApiResponse<Map<String, Boolean>> toggleLike(@PathVariable Long id) {
    UUID userId = MockAuth.MOCK_USER_ID;
    boolean liked = commentService.toggleLike(id, userId);
    return ApiResponse.success(Map.of("liked", liked));
  }
}
