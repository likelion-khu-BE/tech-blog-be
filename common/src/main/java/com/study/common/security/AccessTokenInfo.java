package com.study.common.security;

import com.study.common.entity.UserRole;
import java.util.UUID;

/**
 * access token 파싱 결과.
 *
 * <p>jjwt의 Claims를 외부 모듈에 노출하지 않기 위한 래퍼. app 모듈의 JwtAuthenticationFilter가 이 타입만 의존한다.
 */
public record AccessTokenInfo(UUID userId, UserRole role) {

  /** JwtAuthenticationFilter에서 SecurityContext에 넣을 principal로 변환. */
  public CustomUserDetails toUserDetails() {
    return new CustomUserDetails(userId, role);
  }
}
