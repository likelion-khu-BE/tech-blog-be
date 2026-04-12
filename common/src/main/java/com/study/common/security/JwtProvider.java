package com.study.common.security;

import com.study.common.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증.
 *
 * <p>HS256 대칭키 — 토큰 발급과 검증이 같은 서버에서 이루어지므로 비대칭키(RS256)는 불필요.
 *
 * <p>access token: sub=userId(Long), role 클레임 포함 (매 요청마다 DB 조회 없이 인가 판단) refresh token:
 * sub=userId, familyId 클레임 포함 (rotation 추적용)
 */
@Component
public class JwtProvider {

  private final SecretKey secretKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  public JwtProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-expiration}") long accessExpiration,
      @Value("${jwt.refresh-expiration}") long refreshExpiration) {
    // Base64 디코딩 → HS256에 필요한 최소 256bit SecretKey 생성
    this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    this.accessTokenExpiration = accessExpiration;
    this.refreshTokenExpiration = refreshExpiration;
  }

  /** access token 생성. role을 클레임에 넣어 매 요청마다 DB 조회 없이 인가를 처리한다. */
  public String generateAccessToken(Long userId, UserRole role) {
    Date now = new Date();
    return Jwts.builder()
        .subject(userId.toString())
        .claim("role", role.name())
        .claim("type", "access")
        .issuedAt(now)
        .expiration(new Date(now.getTime() + accessTokenExpiration))
        .signWith(secretKey)
        .compact();
  }

  /**
   * refresh token 생성. familyId로 같은 세션의 토큰 체인을 추적한다.
   *
   * <p>jti(JWT ID)를 랜덤 UUID로 설정하여 동일 시각에 생성되어도 토큰이 유니크하도록 보장한다. 이것이 없으면 같은
   * 밀리초에 rotation이 일어날 때 동일한 토큰이 생성되어 DB unique 제약 위반이 발생한다.
   */
  public String generateRefreshToken(Long userId, UUID familyId) {
    Date now = new Date();
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(userId.toString())
        .claim("familyId", familyId.toString())
        .claim("type", "refresh")
        .issuedAt(now)
        .expiration(new Date(now.getTime() + refreshTokenExpiration))
        .signWith(secretKey)
        .compact();
  }

  /**
   * 토큰 파싱 및 서명 검증.
   *
   * @throws JwtException 서명 불일치, 만료, 형식 오류 등
   */
  public Claims parseToken(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  public Long getUserId(String token) {
    return Long.valueOf(parseToken(token).getSubject());
  }

  public UserRole getRole(String token) {
    return UserRole.valueOf(parseToken(token).get("role", String.class));
  }

  public UUID getFamilyId(String token) {
    return UUID.fromString(parseToken(token).get("familyId", String.class));
  }

  public long getRefreshTokenExpiration() {
    return refreshTokenExpiration;
  }

  /**
   * access token을 파싱하여 인증 정보를 반환한다.
   *
   * <p>jjwt 타입(Claims, JwtException)을 외부 모듈에 노출하지 않기 위해 Optional로 감싼다. 토큰이 유효하지 않거나
   * access 타입이 아니면 empty를 반환.
   */
  public Optional<AccessTokenInfo> parseAccessToken(String token) {
    try {
      Claims claims = parseToken(token);
      if (!"access".equals(claims.get("type", String.class))) {
        return Optional.empty();
      }
      Long userId = Long.valueOf(claims.getSubject());
      UserRole role = UserRole.valueOf(claims.get("role", String.class));
      return Optional.of(new AccessTokenInfo(userId, role));
    } catch (JwtException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
