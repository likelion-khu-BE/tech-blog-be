package com.study.auth.application;

import com.study.auth.domain.User;
import com.study.auth.domain.UserStatus;
import com.study.auth.infrastructure.UserRepository;
import com.study.auth.presentation.dto.UserResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserAdminService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<UserResponse> getUsers(String status) {
    if (status == null || status.isBlank()) {
      return userRepository.findAllByOrderBySignupRequestedAtDesc().stream()
          .map(this::toResponse)
          .toList();
    }
    try {
      UserStatus userStatus = UserStatus.valueOf(status.trim().toUpperCase());
      return userRepository.findAllByStatusOrderBySignupRequestedAtDesc(userStatus).stream()
          .map(this::toResponse)
          .toList();
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("유효하지 않은 status 값입니다. 허용 값: PENDING, ACTIVE, REJECTED");
    }
  }

  @Transactional
  public UserResponse approveUser(Long userId, Long adminId) {
    User user = findOrThrow(userId);
    user.approve(adminId);
    return toResponse(userRepository.save(user));
  }

  @Transactional
  public UserResponse rejectUser(Long userId) {
    User user = findOrThrow(userId);
    user.reject();
    return toResponse(userRepository.save(user));
  }

  private User findOrThrow(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다: " + userId));
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getLoginEmail(),
        user.getRole().name(),
        user.getStatus().name(),
        user.getSignupRequestedAt(),
        user.getApprovedAt());
  }
}
