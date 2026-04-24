package com.study.common.security;

import com.study.common.entity.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 현재 인증된 사용자 정보를 꺼내는 static 유틸리티.
 *
 * <p>컨트롤러에서는 {@code @CurrentUser CustomUserDetails user}로 주입받을 수 있지만, 서비스/리포지토리 등 컨트롤러 바깥에서 현재 유저가
 * 필요할 때 이 클래스를 쓴다.
 *
 * <p>내부적으로 SecurityContextHolder의 ThreadLocal에서 Authentication을 꺼낸다 — 같은 요청 스레드 내에서만 유효하고, 요청이 끝나면
 * 자동 정리된다.
 *
 * <p>사용법:
 *
 * <pre>{@code
 * Long userId = SecurityUtils.getCurrentUserId();
 * UserRole role = SecurityUtils.getCurrentUserRole();
 * CustomUserDetails user = SecurityUtils.getCurrentUser();
 * }</pre>
 */
public final class SecurityUtils {

  private SecurityUtils() {}

  /** 현재 인증된 사용자의 전체 정보. 인증되지 않은 요청이면 null. */
  public static CustomUserDetails getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof CustomUserDetails userDetails) {
      return userDetails;
    }
    return null;
  }

  /**
   * 현재 인증된 사용자의 ID.
   *
   * @throws IllegalStateException 인증되지 않은 요청에서 호출 시
   */
  public static Long getCurrentUserId() {
    CustomUserDetails user = getCurrentUser();
    if (user == null) {
      throw new IllegalStateException("인증된 사용자가 없습니다");
    }
    return user.userId();
  }

  /**
   * 현재 인증된 사용자의 권한.
   *
   * @throws IllegalStateException 인증되지 않은 요청에서 호출 시
   */
  public static UserRole getCurrentUserRole() {
    CustomUserDetails user = getCurrentUser();
    if (user == null) {
      throw new IllegalStateException("인증된 사용자가 없습니다");
    }
    return user.role();
  }

  /** 현재 요청이 인증된 상태인지 확인. */
  public static boolean isAuthenticated() {
    return getCurrentUser() != null;
  }

  /** 현재 사용자가 ADMIN인지 확인. */
  public static boolean isAdmin() {
    CustomUserDetails user = getCurrentUser();
    return user != null && user.role() == UserRole.ADMIN;
  }
}
