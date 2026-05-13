package com.study.blog.integration;

import com.study.auth.domain.UserRole;
import com.study.auth.infrastructure.security.CustomUserDetails;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

final class TestAuth {

  private TestAuth() {}

  static RequestPostProcessor asMember(Long userId) {
    return SecurityMockMvcRequestPostProcessors.authentication(
        new UsernamePasswordAuthenticationToken(
            new CustomUserDetails(userId, UserRole.MEMBER, ""),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))));
  }

  static RequestPostProcessor asAdmin(Long userId) {
    return SecurityMockMvcRequestPostProcessors.authentication(
        new UsernamePasswordAuthenticationToken(
            new CustomUserDetails(userId, UserRole.ADMIN, ""),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
  }
}
