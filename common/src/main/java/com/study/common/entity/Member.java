package com.study.common.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 멤버(프로필) 엔티티.
 *
 * <p>인증/계정 정보는 {@link User}에 있고, Member는 <b>프로필 도메인</b>만 담당한다. 로그인 이메일 · 비밀번호 · 승인 상태 같은 계정 속성은
 * 여기에 두지 않는다 — 중복 저장으로 인한 정합성 붕괴를 피하기 위함.
 *
 * <p>User ↔ Member 는 1:1. User 없이는 Member가 존재할 수 없다(회원가입 승인 후 프로필 생성 흐름을 전제).
 */
@Entity
@DynamicUpdate
@Table(
    name = "member",
    indexes = {@Index(name = "idx_member_user_id", columnList = "user_id", unique = true)})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  /**
   * 계정(User) 참조. LAZY로 두어 프로필 조회 시 불필요한 조인을 막는다. 로그인 이메일이 필요하면 {@code
   * member.getUser().getLoginEmail()}으로 접근.
   */
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "department")
  private String department;

  @Enumerated(EnumType.STRING)
  @Column(name = "session_type", nullable = false)
  private SessionType sessionType;

  @Column(name = "profile_image_url")
  private String profileImageUrl;

  @Column(name = "github_url")
  private String githubUrl;

  @Column(name = "links_json", columnDefinition = "jsonb")
  private String linksJson;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MemberTechStack> techStacks = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  /**
   * 새 멤버 생성. User(계정)는 이미 존재해야 한다 — 회원가입 승인 플로우에서 User 생성 후 이 메서드로 프로필을 만든다.
   *
   * <p>email을 받지 않는 이유: 계정 이메일은 {@link User#getLoginEmail()}이 원본. 중복 저장 금지.
   */
  public static Member create(
      User user,
      String name,
      SessionType sessionType,
      String department,
      String profileImageUrl,
      String githubUrl,
      String linksJson) {
    Member member = new Member();
    member.user = user;
    member.name = name;
    member.sessionType = sessionType;
    member.department = department;
    member.profileImageUrl = profileImageUrl;
    member.githubUrl = githubUrl;
    member.linksJson = linksJson;
    return member;
  }

  public void update(
      String name,
      SessionType sessionType,
      String department,
      String profileImageUrl,
      String githubUrl,
      String linksJson) {
    this.name = name;
    this.sessionType = sessionType;
    this.department = department;
    this.profileImageUrl = profileImageUrl;
    this.githubUrl = githubUrl;
    this.linksJson = linksJson;
  }

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
