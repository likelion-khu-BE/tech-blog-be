package com.study.auth.presentation.controller;

import com.study.auth.presentation.dto.LoginRequest;
import com.study.auth.presentation.dto.LoginResponse;
import com.study.auth.presentation.dto.SignupRequest;
import com.study.auth.presentation.dto.SignupResponse;
import com.study.auth.presentation.dto.TokenRefreshResponse;
import com.study.auth.application.AuthService;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 API 엔드포인트.
 *
 * <p>이 컨트롤러의 책임은 HTTP ↔ 서비스 레이어 변환뿐이다:
 *
 * <ul>
 *   <li>요청 바디 → AuthService 호출
 *   <li>refresh token을 HttpOnly 쿠키로 설정/삭제
 *   <li>access token을 응답 바디로 반환
 * </ul>
 *
 * 비즈니스 로직(검증, 토큰 rotation, 재사용 감지)은 전부 AuthService에 있다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

  /**
   * refresh token 쿠키 경로.
   *
   * <p>/api/auth로 설정 — 브라우저가 /api/auth/refresh와 /api/auth/logout에만 쿠키를 전송한다. 일반 API 호출(/api/blogs
   * 등)에는 쿠키가 포함되지 않아 불필요한 노출을 방지.
   */
  private static final String COOKIE_PATH = "/api/auth";

  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
    SignupResponse response = authService.signup(request.email(), request.password());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthService.LoginResult result = authService.login(request.email(), request.password());

    ResponseCookie cookie =
        buildRefreshTokenCookie(result.rawRefreshToken(), result.refreshMaxAgeMs());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(result.response());
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenRefreshResponse> refresh(
      @CookieValue(name = REFRESH_TOKEN_COOKIE) String refreshToken) {

    AuthService.RefreshResult result = authService.refresh(refreshToken);

    ResponseCookie cookie =
        buildRefreshTokenCookie(result.rawRefreshToken(), result.refreshMaxAgeMs());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(new TokenRefreshResponse(result.accessToken()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {

    authService.logout(refreshToken);

    // maxAge(0)으로 쿠키 즉시 삭제
    ResponseCookie deleteCookie =
        ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path(COOKIE_PATH)
            .maxAge(0)
            .build();

    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
        .build();
  }

  // ── private ──

  /**
   * refresh token 쿠키 빌드.
   *
   * <p>보안 속성:
   *
   * <ul>
   *   <li>HttpOnly — JavaScript에서 접근 불가 (XSS 방어)
   *   <li>Secure — HTTPS에서만 전송 (MITM 방어)
   *   <li>SameSite=Strict — 외부 사이트 요청에 쿠키 미전송 (CSRF 방어)
   *   <li>Path=/api/auth — refresh/logout 엔드포인트에만 전송 (최소 노출)
   * </ul>
   */
  private ResponseCookie buildRefreshTokenCookie(String token, long maxAgeMs) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path(COOKIE_PATH)
        .maxAge(Duration.ofMillis(maxAgeMs))
        .build();
  }
}
