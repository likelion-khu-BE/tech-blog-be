package com.study.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/**
 * Refresh token SHA-256 해싱.
 *
 * <p>DB에는 해시만 저장한다. DB가 유출되어도 토큰 원문을 복원할 수 없다. BCrypt와 달리 salt 없는 단방향 해시를 쓰는 이유 —
 * refresh token은 충분히 긴 랜덤 문자열(JWT)이라 rainbow table 공격이 비현실적이고, 매 요청마다 빠르게 조회해야 하므로
 * BCrypt의 의도적 느림(cost factor)이 부적절하다.
 */
@Component
public class TokenHasher {

  public String hash(String rawToken) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      // SHA-256은 모든 JVM에서 지원 — 도달 불가능한 코드
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
