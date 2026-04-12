package com.study.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Refresh Token 엔티티.
 *
 * <p>토큰 원문은 저장하지 않는다 — SHA-256 해시만 보관. DB가 유출되어도 토큰을 복원할 수 없다.
 *
 * <p>familyId는 최초 로그인 시 생성되고, rotation마다 동일한 familyId로 새 토큰이 발급된다. 사용된(USED) 토큰이 다시
 * 들어오면 해당 familyId의 모든 토큰을 REVOKED 처리한다.
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
      @Index(name = "idx_refresh_token_hash", columnList = "token_hash", unique = true),
      @Index(name = "idx_refresh_token_family", columnList = "family_id"),
      @Index(name = "idx_refresh_token_user", columnList = "user_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  /** SHA-256 해시. 원문 토큰이 이 필드에 할당되면 보안 사고다. */
  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  /** 토큰 가계(family). 같은 로그인 세션에서 rotation된 토큰들은 동일 familyId를 공유한다. */
  @Column(name = "family_id", nullable = false)
  private UUID familyId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RefreshTokenStatus status;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  // ── 팩토리 메서드 ──

  /** 새 refresh token 레코드 생성. tokenHash는 반드시 SHA-256 해시여야 한다. */
  public static RefreshToken create(UUID userId, String tokenHash, UUID familyId, Instant expiresAt) {
    RefreshToken token = new RefreshToken();
    token.userId = userId;
    token.tokenHash = tokenHash;
    token.familyId = familyId;
    token.status = RefreshTokenStatus.ACTIVE;
    token.expiresAt = expiresAt;
    return token;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(this.expiresAt);
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
