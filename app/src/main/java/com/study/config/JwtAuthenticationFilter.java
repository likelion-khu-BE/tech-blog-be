package com.study.config;

import com.study.common.security.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 매 요청마다 Authorization 헤더의 Bearer 토큰을 검증하는 필터.
 *
 * <p>UsernamePasswordAuthenticationFilter 앞에 등록한다 — 폼 로그인 인증 전에 JWT 인증을 먼저 시도하기 위함. 토큰이 유효하면
 * SecurityContext에 인증 정보를 설정하고, 유효하지 않으면 (만료, 변조 등) 아무것도 설정하지 않아 AuthenticationEntryPoint가 401을
 * 반환하게 된다.
 *
 * <p>jjwt 라이브러리 타입은 이 클래스에 노출되지 않는다 — JwtProvider.parseAccessToken()이 Optional로 감싸서 반환하므로 app 모듈은
 * jjwt에 의존하지 않는다 (의존성 역전).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = resolveToken(request);

    if (token != null) {
      jwtProvider
          .parseAccessToken(token)
          .ifPresent(
              info -> {
                // ROLE_ prefix — Spring Security의 hasRole()이 자동으로 ROLE_ prefix를 기대
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        info.toUserDetails(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + info.role().name())));
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
              });
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.equals("/api/auth/signup")
        || path.equals("/api/auth/login")
        || path.equals("/api/auth/refresh")
        || path.equals("/api/auth/logout");
  }

  private String resolveToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
