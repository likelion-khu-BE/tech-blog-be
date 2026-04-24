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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 사용자 엔티티.
 *
 * <p>가입 시 PENDING 상태로 생성되며, 관리자 승인 후 ACTIVE가 되어야 로그인 가능. passwordHash는 BCrypt로 인코딩된 값만 저장한다 — 평문은
 * 절대 이 객체에 들어오지 않는다.
 */
@Entity
@DynamicUpdate
@Table(
    name = "users",
    indexes = {
      @Index(name = "idx_user_login_email", columnList = "login_email", unique = true),
      @Index(name = "idx_user_status", columnList = "status")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "login_email", nullable = false, unique = true)
  private String loginEmail;

  /** BCrypt 해시. 평문 비밀번호가 이 필드에 할당되면 보안 사고다. */
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status;

  @Column(name = "signup_requested_at", nullable = false, updatable = false)
  private Instant signupRequestedAt;

  @Column(name = "approved_at")
  private Instant approvedAt;

  @Column(name = "approved_by")
  private Long approvedBy;

  @Column(name = "expired_at")
  private Instant expiredAt;

  @Column(name = "last_login_at")
  private Instant lastLoginAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  // ── 팩토리 메서드 ──

  /** 회원가입 시 호출. 반드시 BCrypt 해시를 넘겨야 한다. */
  public static User create(String loginEmail, String passwordHash) {
    User user = new User();
    user.loginEmail = loginEmail;
    user.passwordHash = passwordHash;
    user.role = UserRole.MEMBER;
    user.status = UserStatus.PENDING;
    user.signupRequestedAt = Instant.now();
    return user;
  }

  // ── 도메인 행위 ──

  public void approve(Long adminId) {
    this.status = UserStatus.ACTIVE;
    this.approvedAt = Instant.now();
    this.approvedBy = adminId;
  }

  public void promoteToAdmin() {
    this.role = UserRole.ADMIN;
  }

  public void reject() {
    this.status = UserStatus.REJECTED;
  }

  public void expire() {
    this.status = UserStatus.EXPIRED;
    this.expiredAt = Instant.now();
  }

  public void updateLastLogin() {
    this.lastLoginAt = Instant.now();
  }

  public boolean isActive() {
    return this.status == UserStatus.ACTIVE;
  }

  // ── JPA 콜백 ──

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    this.updatedAt = Instant.now();
  }
}
