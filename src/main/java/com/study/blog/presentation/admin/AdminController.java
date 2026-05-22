package com.study.blog.presentation.admin;

import com.study.blog.application.admin.AdminService;
import com.study.blog.application.admin.dto.AdminPostResponse;
import com.study.blog.application.admin.dto.AdminStatsResponse;
import com.study.blog.application.admin.dto.PostStatusUpdateRequest;
import com.study.blog.domain.post.PostStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping("/stats")
  public ResponseEntity<AdminStatsResponse> getStats() {
    return ResponseEntity.ok(adminService.getStats());
  }

  @GetMapping("/posts")
  public ResponseEntity<Page<AdminPostResponse>> getAllPosts(
      @RequestParam(required = false) PostStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(adminService.getAllPosts(status, page, size));
  }

  @PatchMapping("/posts/{id}/status")
  public ResponseEntity<AdminPostResponse> changePostStatus(
      @PathVariable Long id, @Valid @RequestBody PostStatusUpdateRequest req) {
    return ResponseEntity.ok(adminService.changePostStatus(id, req));
  }

  @DeleteMapping("/posts/{id}")
  public ResponseEntity<Void> forceDeletePost(@PathVariable Long id) {
    adminService.forceDeletePost(id);
    return ResponseEntity.noContent().build();
  }
}
