package com.study.auth.presentation.controller;

import com.study.auth.application.UserAdminService;
import com.study.auth.infrastructure.security.SecurityUtils;
import com.study.auth.presentation.dto.UserResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserAdminController {

  private final UserAdminService userAdminService;

  @GetMapping
  public ResponseEntity<List<UserResponse>> getUsers(
      @RequestParam(required = false) String status) {
    return ResponseEntity.ok(userAdminService.getUsers(status));
  }

  @PostMapping("/{id}/approve")
  public ResponseEntity<UserResponse> approveUser(@PathVariable Long id) {
    return ResponseEntity.ok(userAdminService.approveUser(id, SecurityUtils.getCurrentUserId()));
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<UserResponse> rejectUser(@PathVariable Long id) {
    return ResponseEntity.ok(userAdminService.rejectUser(id));
  }
}
