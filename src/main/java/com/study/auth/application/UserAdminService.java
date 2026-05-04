package com.study.auth.application;

import com.study.auth.domain.User;
import com.study.auth.domain.UserStatus;
import com.study.auth.infrastructure.UserRepository;
import com.study.auth.presentation.dto.UserResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAdminService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<UserResponse> getUsers(String status) {
    List<User> users =
        status != null
            ? userRepository.findAllByStatusOrderBySignupRequestedAtDesc(
                UserStatus.valueOf(status.toUpperCase()))
            : userRepository.findAllByOrderBySignupRequestedAtDesc();
    return users.stream().map(this::toResponse).toList();
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
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: " + userId));
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
