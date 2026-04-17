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

@Entity
@DynamicUpdate
@Table(
    name = "member",
    indexes = {
      @Index(name = "idx_member_email", columnList = "email", unique = true)
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

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

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public static Member create(String name, String email, SessionType sessionType) {
    Member member = new Member();
    member.name = name;
    member.email = email;
    member.sessionType = sessionType;
    return member;
  }

  public void updateProfile(String department, String profileImageUrl, String githubUrl, String linksJson) {
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
