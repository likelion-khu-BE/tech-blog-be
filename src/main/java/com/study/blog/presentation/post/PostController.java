package com.study.blog.presentation.post;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.blog.application.post.PostService;
import com.study.blog.application.post.dto.PostCreateRequest;
import com.study.blog.application.post.dto.PostResponse;
import com.study.blog.application.post.dto.PostSummaryResponse;
import com.study.blog.application.post.dto.PostUpdateRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog/posts")
public class PostController {

  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  @GetMapping
  public ResponseEntity<Page<PostSummaryResponse>> getPosts(
      @RequestParam(required = false) String board,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String generation,
      @RequestParam(required = false) Long authorId,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        postService.getPosts(board, category, generation, authorId, keyword, page, size));
  }

  @GetMapping("/{id}")
  public ResponseEntity<PostResponse> getPost(
      @PathVariable Long id, @CurrentUser CustomUserDetails user) {
    Long requesterId = user != null ? user.userId() : null;
    return ResponseEntity.ok(postService.getPost(id, requesterId));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<PostResponse> createPost(
      @Valid @RequestBody PostCreateRequest req, @CurrentUser CustomUserDetails user) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(postService.createPost(req, user.userId()));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<PostResponse> updatePost(
      @PathVariable Long id,
      @Valid @RequestBody PostUpdateRequest req,
      @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(postService.updatePost(id, req, user.userId()));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<Void> deletePost(
      @PathVariable Long id, @CurrentUser CustomUserDetails user) {
    postService.deletePost(id, user.userId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/like")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<Map<String, Boolean>> toggleLike(
      @PathVariable Long id, @CurrentUser CustomUserDetails user) {
    boolean liked = postService.toggleLike(id, user.userId());
    return ResponseEntity.ok(Map.of("liked", liked));
  }

  @PostMapping("/{id}/bookmark")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<Map<String, Boolean>> toggleBookmark(
      @PathVariable Long id, @CurrentUser CustomUserDetails user) {
    boolean bookmarked = postService.toggleBookmark(id, user.userId());
    return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
  }
}
