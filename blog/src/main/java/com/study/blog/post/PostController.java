package com.study.blog.post;

import com.study.blog.common.ApiResponse;
import com.study.blog.common.auth.MockAuth;
import com.study.blog.post.dto.PostCreateRequest;
import com.study.blog.post.dto.PostResponse;
import com.study.blog.post.dto.PostSummaryResponse;
import com.study.blog.post.dto.PostUpdateRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog/posts")
public class PostController {

  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  @GetMapping
  public ApiResponse<Page<PostSummaryResponse>> getPosts(
      @RequestParam(required = false) String board,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String generation,
      @RequestParam(required = false) UUID authorId,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ApiResponse.success(
        postService.getPosts(board, category, generation, authorId, keyword, page, size));
  }

  @GetMapping("/{id}")
  public ApiResponse<PostResponse> getPost(@PathVariable Long id) {
    UUID userId = MockAuth.MOCK_USER_ID;
    return ApiResponse.success(postService.getPost(id, userId));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<PostResponse> createPost(@Valid @RequestBody PostCreateRequest req) {
    UUID userId = MockAuth.MOCK_USER_ID;
    return ApiResponse.success(postService.createPost(req, userId));
  }

  @PutMapping("/{id}")
  public ApiResponse<PostResponse> updatePost(
      @PathVariable Long id, @Valid @RequestBody PostUpdateRequest req) {
    UUID userId = MockAuth.MOCK_USER_ID;
    return ApiResponse.success(postService.updatePost(id, req, userId));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePost(@PathVariable Long id) {
    UUID userId = MockAuth.MOCK_USER_ID;
    postService.deletePost(id, userId);
  }

  @PostMapping("/{id}/like")
  public ApiResponse<Map<String, Boolean>> toggleLike(@PathVariable Long id) {
    UUID userId = MockAuth.MOCK_USER_ID;
    boolean liked = postService.toggleLike(id, userId);
    return ApiResponse.success(Map.of("liked", liked));
  }

  @PostMapping("/{id}/bookmark")
  public ApiResponse<Map<String, Boolean>> toggleBookmark(@PathVariable Long id) {
    UUID userId = MockAuth.MOCK_USER_ID;
    boolean bookmarked = postService.toggleBookmark(id, userId);
    return ApiResponse.success(Map.of("bookmarked", bookmarked));
  }
}
