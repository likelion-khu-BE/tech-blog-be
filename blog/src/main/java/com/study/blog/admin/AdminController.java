package com.study.blog.admin;

import com.study.blog.admin.dto.AdminPostResponse;
import com.study.blog.admin.dto.AdminStatsResponse;
import com.study.blog.admin.dto.PostStatusUpdateRequest;
import com.study.blog.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog/admin")
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping("/stats")
  public ApiResponse<AdminStatsResponse> getStats() {
    return ApiResponse.success(adminService.getStats());
  }

  @GetMapping("/posts")
  public ApiResponse<Page<AdminPostResponse>> getAllPosts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success(adminService.getAllPosts(page, size));
  }

  @PatchMapping("/posts/{id}/status")
  public ApiResponse<Void> changePostStatus(
      @PathVariable Long id, @Valid @RequestBody PostStatusUpdateRequest req) {
    adminService.changePostStatus(id, req.getStatus());
    return ApiResponse.success(null);
  }

  @DeleteMapping("/posts/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void forceDeletePost(@PathVariable Long id) {
    adminService.forceDeletePost(id);
  }
}
