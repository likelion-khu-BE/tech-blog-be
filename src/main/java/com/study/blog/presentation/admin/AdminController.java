package com.study.blog.presentation.admin;

import com.study.blog.application.admin.AdminService;

import com.study.blog.application.admin.dto.AdminPostResponse;
import com.study.blog.application.admin.dto.AdminStatsResponse;
import com.study.blog.application.admin.dto.PostStatusUpdateRequest;
import com.study.blog.shared.auth.MockAuth;
import com.study.blog.shared.exception.BlogErrorCode;
import com.study.blog.shared.exception.BlogException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog/admin")
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping("/stats")
  public ResponseEntity<AdminStatsResponse> getStats(
      @RequestHeader(value = "X-Admin-Token", required = false) String token) {
    validateAdminToken(token);
    return ResponseEntity.ok(adminService.getStats());
  }

  @GetMapping("/posts")
  public ResponseEntity<Page<AdminPostResponse>> getAllPosts(
      @RequestHeader(value = "X-Admin-Token", required = false) String token,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    validateAdminToken(token);
    return ResponseEntity.ok(adminService.getAllPosts(page, size));
  }

  @PatchMapping("/posts/{id}/status")
  public ResponseEntity<Void> changePostStatus(
      @RequestHeader(value = "X-Admin-Token", required = false) String token,
      @PathVariable Long id,
      @Valid @RequestBody PostStatusUpdateRequest req) {
    validateAdminToken(token);
    adminService.changePostStatus(id, req.getStatus());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/posts/{id}")
  public ResponseEntity<Void> forceDeletePost(
      @RequestHeader(value = "X-Admin-Token", required = false) String token,
      @PathVariable Long id) {
    validateAdminToken(token);
    adminService.forceDeletePost(id);
    return ResponseEntity.noContent().build();
  }

  private void validateAdminToken(String token) {
    if (!MockAuth.ADMIN_TOKEN.equals(token)) {
      throw new BlogException(BlogErrorCode.UNAUTHORIZED);
    }
  }
}
