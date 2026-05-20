package com.study.auth.application;

import com.study.auth.domain.User;
import com.study.auth.domain.UserStatus;
import com.study.auth.infrastructure.UserRepository;
import com.study.auth.presentation.dto.UserResponse;
import com.study.profile.infrastructure.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserAdminService {

  private final UserRepository userRepository;
  private final MemberRepository memberRepository;

  @Transactional(readOnly = true)
  public List<UserResponse> getUsers(String status) {
    List<User> users;
    if (status == null || status.isBlank()) {
      users = userRepository.findAllByOrderBySignupRequestedAtDesc();
    } else {
      try {
        UserStatus userStatus = UserStatus.valueOf(status.trim().toUpperCase());
        users = userRepository.findAllByStatusOrderBySignupRequestedAtDesc(userStatus);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("유효하지 않은 status 값입니다. 허용 값: PENDING, ACTIVE, REJECTED");
      }
    }

    // 시현 N+1 수정: 유저별 member 개별 조회 → findAllByUserIdIn 한 번으로 통합
    List<Long> userIds = users.stream().map(User::getId).toList();
    Map<Long, Long> memberIdByUserId =
        memberRepository.findAllByUserIdIn(userIds).stream()
            .collect(Collectors.toMap(m -> m.getUser().getId(), m -> m.getId()));

    return users.stream().map(u -> toResponse(u, memberIdByUserId.get(u.getId()))).toList();
  }

  @Transactional
  public UserResponse approveUser(Long userId, Long adminId) {
    User user = findOrThrow(userId);
    user.approve(adminId);
    Long memberId = memberRepository.findByUserId(userId).map(m -> m.getId()).orElse(null);
    return toResponse(userRepository.save(user), memberId);
  }

  @Transactional
  public UserResponse rejectUser(Long userId) {
    User user = findOrThrow(userId);
    user.reject();
    Long memberId = memberRepository.findByUserId(userId).map(m -> m.getId()).orElse(null);
    return toResponse(userRepository.save(user), memberId);
  }

  private User findOrThrow(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다: " + userId));
  }

  private UserResponse toResponse(User user, Long memberId) {
    return new UserResponse(
        user.getId(),
        memberId,
        user.getLoginEmail(),
        user.getRole().name(),
        user.getStatus().name(),
        user.getSignupRequestedAt(),
        user.getApprovedAt());
  }
}
