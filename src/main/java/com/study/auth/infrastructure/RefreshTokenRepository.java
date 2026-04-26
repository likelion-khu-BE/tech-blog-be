package com.study.auth.infrastructure;

import com.study.auth.domain.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  /**
   * 토큰을 USED로 전환 (atomic).
   *
   * <p>WHERE status='ACTIVE' 조건 덕분에 동시에 같은 토큰으로 refresh 요청이 오면 하나만 성공한다. 반환값이 0이면 이미 사용된 토큰 → 재사용
   * 공격으로 판단.
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      "UPDATE RefreshToken r SET r.status = com.study.auth.domain.RefreshTokenStatus.USED"
          + " WHERE r.tokenHash = :tokenHash"
          + " AND r.status = com.study.auth.domain.RefreshTokenStatus.ACTIVE")
  int markAsUsed(@Param("tokenHash") String tokenHash);

  /**
   * 해당 family의 ACTIVE/USED 토큰을 전부 REVOKED 처리.
   *
   * <p>재사용 감지 시 호출 — 공격자와 정상 사용자 토큰을 구분할 수 없으므로 family 전체를 폐기한다.
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      "UPDATE RefreshToken r SET r.status = com.study.auth.domain.RefreshTokenStatus.REVOKED"
          + " WHERE r.familyId = :familyId"
          + " AND r.status <> com.study.auth.domain.RefreshTokenStatus.REVOKED")
  int revokeFamily(@Param("familyId") UUID familyId);

  /** 해당 유저의 모든 ACTIVE 토큰을 REVOKED 처리. 전체 로그아웃 시 사용. */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      "UPDATE RefreshToken r SET r.status = com.study.auth.domain.RefreshTokenStatus.REVOKED"
          + " WHERE r.userId = :userId"
          + " AND r.status = com.study.auth.domain.RefreshTokenStatus.ACTIVE")
  int revokeAllByUserId(@Param("userId") Long userId);
}
