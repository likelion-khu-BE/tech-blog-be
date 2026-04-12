package com.study.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정.
 *
 * <p>핵심 설계:
 *
 * <ul>
 *   <li>STATELESS 세션 — JWT 기반이므로 서버 세션 사용 안 함
 *   <li>CSRF 비활성화 — stateless API에서는 SameSite 쿠키가 CSRF를 대체
 *   <li>폼 로그인/HTTP Basic 비활성화 — 자체 JWT 인증만 사용
 *   <li>{@code @EnableMethodSecurity} — 모든 모듈에서 {@code @PreAuthorize} 사용 가능
 * </ul>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ObjectMapper objectMapper;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        // JWT 기반 stateless API — 서버 세션, CSRF, 폼 로그인 모두 불필요
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)

        // URL 레벨 인가 — 세밀한 제어는 @PreAuthorize로
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/auth/signup", "/api/auth/login", "/api/auth/refresh")
                    .permitAll()
                    .anyRequest()
                    .authenticated())

        // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        // 인증/인가 실패 시 JSON 응답 (Spring 기본 HTML 페이지 대체)
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler()))

        // CORS — CorsConfig에서 설정한 CorsConfigurationSource를 사용
        .cors(cors -> {})
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    // BCrypt cost factor 10 (기본값) — 비밀번호 해시 1회에 ~100ms
    // brute-force를 어렵게 만들면서도 로그인 응답 시간에 영향이 적은 균형점
    return new BCryptPasswordEncoder();
  }

  /**
   * 401 Unauthorized — 인증 자체가 안 된 경우 (토큰 없음, 만료, 변조).
   *
   * <p>Spring Security 기본 동작은 /login 리다이렉트인데, REST API에는 부적절하다.
   */
  private AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, authException) ->
        writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다");
  }

  /**
   * 403 Forbidden — 인증은 됐지만 권한이 부족한 경우.
   *
   * <p>예: MEMBER가 ADMIN 전용 엔드포인트에 접근.
   */
  private AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) ->
        writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다");
  }

  private void writeErrorResponse(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(
        response.getWriter(),
        Map.of("status", status, "message", message));
  }
}
