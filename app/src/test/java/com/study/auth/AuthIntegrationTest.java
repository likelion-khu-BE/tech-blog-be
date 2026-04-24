package com.study.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.auth.dto.LoginRequest;
import com.study.auth.dto.SignupRequest;
import com.study.common.entity.auth.User;
import com.study.common.repository.RefreshTokenRepository;
import com.study.common.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 인증/인가 통합 테스트.
 *
 * <p>실제 DB(PostgreSQL)와 Spring Security 필터 체인을 포함한 전체 스택 테스트. 모든 테스트는 @Transactional로 롤백되어 독립적으로
 * 실행된다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void cleanUp() {
    // RDS 사용 시 @Transactional 롤백이 MockMvc 요청 단위로 작동하지 않으므로 직접 정리
    refreshTokenRepository.deleteAll();
    userRepository.deleteAll();
  }

  private static final String SIGNUP_URL = "/api/auth/signup";
  private static final String LOGIN_URL = "/api/auth/login";
  private static final String REFRESH_URL = "/api/auth/refresh";
  private static final String LOGOUT_URL = "/api/auth/logout";

  /** ACTIVE 상태 유저를 직접 생성하는 헬퍼. signup API는 PENDING 상태를 만들기 때문. */
  private User createActiveUser(String email, String rawPassword) {
    User user = User.create(email, passwordEncoder.encode(rawPassword));
    user.approve(null);
    return userRepository.save(user);
  }

  /** ACTIVE ADMIN 유저 생성 헬퍼. */
  private User createActiveAdmin(String email, String rawPassword) {
    User user = User.create(email, passwordEncoder.encode(rawPassword));
    user.approve(null);
    user.promoteToAdmin();
    return userRepository.save(user);
  }

  @Nested
  @DisplayName("회원가입")
  class SignupTest {

    @Test
    @DisplayName("정상 가입 → PENDING 상태로 생성")
    void signup_success() throws Exception {
      SignupRequest request = new SignupRequest("test@khu.ac.kr", "password123");

      mockMvc
          .perform(
              post(SIGNUP_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email").value("test@khu.ac.kr"))
          .andExpect(jsonPath("$.status").value("PENDING"))
          .andExpect(jsonPath("$.userId").isNumber());

      // DB에 BCrypt 해시로 저장되었는지 검증
      User saved = userRepository.findByLoginEmail("test@khu.ac.kr").orElseThrow();
      assertThat(saved.getPasswordHash()).startsWith("$2a$");
      assertThat(saved.getPasswordHash()).isNotEqualTo("password123");
    }

    @Test
    @DisplayName("중복 이메일 → 409 Conflict")
    void signup_duplicateEmail() throws Exception {
      createActiveUser("dup@khu.ac.kr", "password123");

      SignupRequest request = new SignupRequest("dup@khu.ac.kr", "password123");

      mockMvc
          .perform(
              post(SIGNUP_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("비밀번호 8자 미만 → 400 Bad Request")
    void signup_shortPassword() throws Exception {
      SignupRequest request = new SignupRequest("test@khu.ac.kr", "short");

      mockMvc
          .perform(
              post(SIGNUP_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 이메일 형식 → 400 Bad Request")
    void signup_invalidEmail() throws Exception {
      SignupRequest request = new SignupRequest("not-an-email", "password123");

      mockMvc
          .perform(
              post(SIGNUP_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("로그인")
  class LoginTest {

    @Test
    @DisplayName("정상 로그인 → access token(body) + refresh token(cookie)")
    void login_success() throws Exception {
      createActiveUser("user@khu.ac.kr", "password123");

      LoginRequest request = new LoginRequest("user@khu.ac.kr", "password123");

      MvcResult result =
          mockMvc
              .perform(
                  post(LOGIN_URL)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.accessToken").isNotEmpty())
              .andExpect(jsonPath("$.tokenType").value("Bearer"))
              .andReturn();

      // Set-Cookie 헤더에 refresh token이 보안 속성과 함께 들어있는지 검증
      String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
      assertThat(setCookie).isNotNull();
      assertThat(setCookie).contains("refresh_token=");
      assertThat(setCookie).containsIgnoringCase("HttpOnly");
      assertThat(setCookie).contains("SameSite=Strict");
      assertThat(setCookie).contains("Path=/api/auth");
    }

    @Test
    @DisplayName("틀린 비밀번호 → 401 (이유 미구분)")
    void login_wrongPassword() throws Exception {
      createActiveUser("user@khu.ac.kr", "password123");

      LoginRequest request = new LoginRequest("user@khu.ac.kr", "wrongpassword");

      mockMvc
          .perform(
              post(LOGIN_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다"));
    }

    @Test
    @DisplayName("존재하지 않는 이메일 → 401 (비밀번호 오류와 동일한 메시지)")
    void login_unknownEmail() throws Exception {
      LoginRequest request = new LoginRequest("nobody@khu.ac.kr", "password123");

      mockMvc
          .perform(
              post(LOGIN_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다"));
    }

    @Test
    @DisplayName("PENDING 유저 로그인 시도 → 403")
    void login_pendingUser() throws Exception {
      // signup API로 PENDING 유저 생성
      SignupRequest signup = new SignupRequest("pending@khu.ac.kr", "password123");
      mockMvc.perform(
          post(SIGNUP_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(signup)));

      LoginRequest login = new LoginRequest("pending@khu.ac.kr", "password123");

      mockMvc
          .perform(
              post(LOGIN_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(login)))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("인증된 API 접근")
  class AuthenticatedAccessTest {

    @Test
    @DisplayName("토큰 없이 인증 필요 API → 401")
    void access_withoutToken() throws Exception {
      mockMvc.perform(get("/api/blog/hello")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("변조된 토큰 → 401")
    void access_withTamperedToken() throws Exception {
      mockMvc
          .perform(
              get("/api/blog/hello").header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("토큰 갱신 (Refresh Token Rotation)")
  class RefreshTest {

    @Test
    @DisplayName("정상 갱신 → 새 access token + 새 refresh token")
    void refresh_success() throws Exception {
      createActiveUser("user@khu.ac.kr", "password123");
      Cookie refreshCookie = loginAndGetRefreshCookie("user@khu.ac.kr", "password123");

      MvcResult result =
          mockMvc
              .perform(post(REFRESH_URL).cookie(refreshCookie))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.accessToken").isNotEmpty())
              .andReturn();

      // 새 refresh token 쿠키가 발급되었는지 확인
      String newSetCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
      assertThat(newSetCookie).contains("refresh_token=");
    }

    @Test
    @DisplayName("같은 refresh token 두 번 사용 → 재사용 감지 → 401")
    void refresh_reuseDetection() throws Exception {
      createActiveUser("user@khu.ac.kr", "password123");
      Cookie refreshCookie = loginAndGetRefreshCookie("user@khu.ac.kr", "password123");

      // 첫 번째 갱신 — 성공
      mockMvc.perform(post(REFRESH_URL).cookie(refreshCookie)).andExpect(status().isOk());

      // 두 번째 갱신 — 같은 토큰 재사용 → 탈취 감지
      mockMvc
          .perform(post(REFRESH_URL).cookie(refreshCookie))
          .andExpect(status().isUnauthorized())
          .andExpect(
              jsonPath("$.message").value("토큰이 재사용되었습니다. 보안을 위해 모든 세션이 만료되었습니다. 다시 로그인해주세요."));
    }

    @Test
    @DisplayName("refresh token 쿠키 없이 요청 → 400")
    void refresh_withoutCookie() throws Exception {
      mockMvc.perform(post(REFRESH_URL)).andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("로그아웃")
  class LogoutTest {

    @Test
    @DisplayName("정상 로그아웃 → 204 + 쿠키 삭제")
    void logout_success() throws Exception {
      createActiveUser("user@khu.ac.kr", "password123");
      Cookie refreshCookie = loginAndGetRefreshCookie("user@khu.ac.kr", "password123");

      MvcResult result =
          mockMvc
              .perform(post(LOGOUT_URL).cookie(refreshCookie))
              .andExpect(status().isNoContent())
              .andReturn();

      // 쿠키 삭제 확인 (Max-Age=0)
      String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
      assertThat(setCookie).contains("Max-Age=0");
    }

    @Test
    @DisplayName("로그아웃 후 refresh token 사용 불가")
    void logout_thenRefreshFails() throws Exception {
      createActiveUser("user@khu.ac.kr", "password123");
      Cookie refreshCookie = loginAndGetRefreshCookie("user@khu.ac.kr", "password123");

      // 로그아웃
      mockMvc.perform(post(LOGOUT_URL).cookie(refreshCookie)).andExpect(status().isNoContent());

      // 로그아웃한 refresh token으로 갱신 시도 → 실패
      mockMvc.perform(post(REFRESH_URL).cookie(refreshCookie)).andExpect(status().isUnauthorized());
    }
  }

  // ── 테스트 헬퍼 ──

  private String loginAndGetAccessToken(String email, String password) throws Exception {
    LoginRequest request = new LoginRequest(email, password);

    MvcResult result =
        mockMvc
            .perform(
                post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

    return objectMapper
        .readTree(result.getResponse().getContentAsString())
        .get("accessToken")
        .asText();
  }

  private Cookie loginAndGetRefreshCookie(String email, String password) throws Exception {
    LoginRequest request = new LoginRequest(email, password);

    MvcResult result =
        mockMvc
            .perform(
                post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

    // Set-Cookie 헤더에서 refresh_token 값 추출
    String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
    assertThat(setCookie).isNotNull();

    String tokenValue =
        setCookie.substring(
            setCookie.indexOf("refresh_token=") + "refresh_token=".length(),
            setCookie.indexOf(";"));

    return new Cookie("refresh_token", tokenValue);
  }
}
