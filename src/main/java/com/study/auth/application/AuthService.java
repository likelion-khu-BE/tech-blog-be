package com.study.auth.application;

import com.study.auth.domain.RefreshToken;
import com.study.auth.domain.User;
import com.study.auth.domain.exception.EmailAlreadyExistsException;
import com.study.auth.domain.exception.InvalidCredentialsException;
import com.study.auth.domain.exception.InvalidTokenException;
import com.study.auth.domain.exception.TokenReusedException;
import com.study.auth.domain.exception.UserNotActiveException;
import com.study.auth.infrastructure.RefreshTokenRepository;
import com.study.auth.infrastructure.UserRepository;
import com.study.auth.infrastructure.security.JwtProvider;
import com.study.auth.infrastructure.security.TokenHasher;
import com.study.auth.presentation.dto.LoginResponse;
import com.study.auth.presentation.dto.SignupResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 비즈니스 로직.
 *
 * <p>이 클래스가 담당하는 보안 불변식(invariant):
 *
 * <ol>
 *   <li>비밀번호는 BCrypt 해시로만 저장 — 평문이 DB에 닿으면 안 됨
 *   <li>refresh token은 SHA-256 해시로 저장 — DB 유출 시에도 토큰 복원 불가
 *   <li>refresh token rotation — 한 번 사용된 토큰은 USED 처리, 재사용 시 family 전체 폐기
 *   <li>로그인 실패 메시지는 이메일/비밀번호 구분 없이 동일 — 이메일 존재 여부 노출 방지
 * </ol>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final TokenHasher tokenHasher;

  /** 회원가입 — PENDING 상태로 생성. 관리자 승인 전까지 로그인 불가. */
  public SignupResponse signup(String email, String rawPassword) {
    if (userRepository.existsByLoginEmail(email)) {
      throw new EmailAlreadyExistsException();
    }

    String passwordHash = passwordEncoder.encode(rawPassword);
    User user = User.create(email, passwordHash);
    userRepository.save(user);

    return new SignupResponse(user.getId(), user.getLoginEmail(), user.getStatus());
  }

  /**
   * 로그인 — access token + refresh token 발급.
   *
   * <p>반환값의 rawRefreshToken은 쿠키에 넣을 원문 토큰. 이 메서드 밖에서는 원문을 보관하지 않는다.
   */
  public LoginResult login(String email, String rawPassword) {
    // timing attack 방어: 유저가 없어도 BCrypt 연산을 수행하여 응답 시간을 균일하게 만든다
    User user =
        userRepository
            .findByLoginEmail(email)
            .orElseThrow(
                () -> {
                  passwordEncoder.encode("dummy-timing-safe");
                  return new InvalidCredentialsException();
                });

    if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }

    if (!user.isActive()) {
      throw new UserNotActiveException();
    }

    user.updateLastLogin();

    // 새 토큰 family 시작
    UUID familyId = UUID.randomUUID();
    String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());
    String rawRefreshToken = jwtProvider.generateRefreshToken(user.getId(), familyId);

    saveRefreshToken(user.getId(), rawRefreshToken, familyId);

    return new LoginResult(
        new LoginResponse(accessToken), rawRefreshToken, jwtProvider.getRefreshTokenExpiration());
  }

  /**
   * 토큰 갱신 (rotation).
   *
   * <p>핵심 보안 로직:
   *
   * <ol>
   *   <li>들어온 토큰을 atomic UPDATE로 USED 처리 (WHERE status=ACTIVE)
   *   <li>affected rows=0 → 이미 사용된 토큰 → 탈취 공격으로 간주 → family 전체 revoke
   *   <li>affected rows=1 → 정상 rotation → 같은 familyId로 새 토큰 발급
   * </ol>
   */
  public RefreshResult refresh(String rawRefreshToken) {
    String tokenHash = tokenHasher.hash(rawRefreshToken);

    // atomic update — 동시 요청 중 하나만 성공
    int updated = refreshTokenRepository.markAsUsed(tokenHash);

    if (updated == 0) {
      // 재사용 감지: 이미 USED이거나 REVOKED인 토큰이 다시 들어왔다
      handleTokenReuse(tokenHash);
    }

    // 기존 토큰에서 userId, familyId 추출
    Long userId = jwtProvider.getUserId(rawRefreshToken);
    UUID familyId = jwtProvider.getFamilyId(rawRefreshToken);

    // DB 레코드의 만료 시간도 검증 (JWT exp와 이중 체크)
    RefreshToken existingToken =
        refreshTokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() -> new InvalidTokenException("토큰을 찾을 수 없습니다"));

    if (existingToken.isExpired()) {
      refreshTokenRepository.revokeFamily(familyId);
      throw new InvalidTokenException("만료된 refresh token입니다");
    }

    // 유저 상태 재검증 — refresh 시점에 비활성화된 유저 차단
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new InvalidTokenException("사용자를 찾을 수 없습니다"));
    if (!user.isActive()) {
      refreshTokenRepository.revokeFamily(familyId);
      throw new UserNotActiveException();
    }

    // 같은 familyId로 새 토큰 발급 (체인 유지)
    String newAccessToken = jwtProvider.generateAccessToken(userId, user.getRole());
    String newRawRefreshToken = jwtProvider.generateRefreshToken(userId, familyId);

    saveRefreshToken(userId, newRawRefreshToken, familyId);

    return new RefreshResult(
        newAccessToken, newRawRefreshToken, jwtProvider.getRefreshTokenExpiration());
  }

  /** 로그아웃 — 해당 토큰의 family 전체 revoke. */
  public void logout(String rawRefreshToken) {
    if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
      return;
    }

    try {
      UUID familyId = jwtProvider.getFamilyId(rawRefreshToken);
      refreshTokenRepository.revokeFamily(familyId);
    } catch (Exception e) {
      // 이미 만료되었거나 잘못된 토큰이어도 로그아웃은 성공 처리
      // 클라이언트 쿠키 삭제가 주 목적이므로 서버 에러를 전파할 이유 없음
    }
  }

  // ── private ──

  private void saveRefreshToken(Long userId, String rawToken, UUID familyId) {
    String hash = tokenHasher.hash(rawToken);
    Instant expiresAt = Instant.now().plusMillis(jwtProvider.getRefreshTokenExpiration());
    RefreshToken token = RefreshToken.create(userId, hash, familyId, expiresAt);
    refreshTokenRepository.save(token);
  }

  /**
   * 토큰 재사용 감지 시 호출.
   *
   * <p>공격자와 정상 사용자의 토큰을 구분할 수 없으므로, 해당 family의 모든 토큰을 폐기하고 재로그인을 강제한다.
   */
  private void handleTokenReuse(String tokenHash) {
    refreshTokenRepository
        .findByTokenHash(tokenHash)
        .ifPresent(token -> refreshTokenRepository.revokeFamily(token.getFamilyId()));
    throw new TokenReusedException();
  }

  // ── 결과 타입 ──

  /** 로그인 결과. rawRefreshToken은 쿠키에 설정할 원문 토큰. */
  public record LoginResult(LoginResponse response, String rawRefreshToken, long refreshMaxAgeMs) {}

  /** 토큰 갱신 결과. */
  public record RefreshResult(String accessToken, String rawRefreshToken, long refreshMaxAgeMs) {}
}
