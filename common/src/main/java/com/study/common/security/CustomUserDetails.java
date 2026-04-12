package com.study.common.security;

import com.study.common.entity.UserRole;

/**
 * SecurityContext에 저장되는 인증 주체(principal).
 *
 * <p>JWT 필터가 토큰을 검증한 뒤 이 객체를 생성하여 SecurityContextHolder에 넣는다. 이후 컨트롤러에서
 * {@code @CurrentUser} 또는 {@code @AuthenticationPrincipal}로 꺼내 쓴다.
 *
 * <p>Spring Security의 UserDetails를 구현하지 않는 이유 — JWT 기반 인증에서는 username/password/authorities가
 * principal 내부에 있을 필요 없다. authorities는 Authentication 객체에 별도로 세팅한다.
 */
public record CustomUserDetails(Long userId, UserRole role) {}
