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
 * 멤버(사용자) 엔티티. @DynamicUpdate : 수정 시 변경된 컬럼만 UPDATE 쿼리에 포함시킨다 (불필요한 쿼리
 * 최소화). @NoArgsConstructor(PROTECTED) : 파라미터 없는 기본 생성자를 protected로 만든다. 외부에서 new Member()를 직접 못 하게
 * 막고, create() 팩토리 메서드를 쓰도록 강제한다.
 */
@Entity
@DynamicUpdate
@Table(
    name = "member",
    indexes = {
      // email 컬럼에 인덱스를 걸어 이메일로 빠르게 검색할 수 있게 한다.
      @Index(name = "idx_member_email", columnList = "email", unique = true)
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false)
  private String name; // 멤버 이름

  @Column(name = "email", nullable = false, unique = true)
  private String email; // 이메일 (중복 불가 — unique = true)

  @Column(name = "department")
  private String department; // 학과 (선택 입력, nullable)

  // @Enumerated(EnumType.STRING) : Enum 값을 DB에 숫자(0,1,2...)가 아닌 문자열("backend", "frontend"...)로
  // 저장한다.
  //                                 숫자로 저장하면 나중에 Enum 순서가 바뀌면 데이터가 오염되므로 STRING이 안전하다.
  @Enumerated(EnumType.STRING)
  @Column(name = "session_type", nullable = false)
  private SessionType sessionType; // 소속 세션(트랙) — backend/frontend/design/ai/pm/etc

  @Column(name = "profile_image_url")
  private String profileImageUrl; // 프로필 이미지 URL (선택)

  @Column(name = "github_url")
  private String githubUrl; // GitHub 프로필 URL (선택)

  // columnDefinition = "jsonb" : 이 컬럼은 DB에서 JSON 형식으로 저장한다.
  // 링크가 여러 개일 수 있어서(포트폴리오, 블로그, SNS 등) JSON 배열로 유연하게 관리한다.
  @Column(name = "links_json", columnDefinition = "jsonb")
  private String linksJson; // 추가 링크 목록 (JSON 형식, 예: [{"type":"blog","url":"..."}])

  /**
   * 멤버가 보유한 기술 스택 목록. mappedBy = "member" : MemberTechStack 엔티티에 있는 'member' 필드와 매핑된다는 뜻. cascade =
   * ALL : 멤버가 저장/삭제될 때 연결된 기술 스택 정보도 함께 처리한다. orphanRemoval = true : 리스트에서 제거하면 DB에서도 해당 행을 삭제한다.
   */
  @jakarta.persistence.OneToMany(
      mappedBy = "member",
      cascade = jakarta.persistence.CascadeType.ALL,
      orphanRemoval = true)
  private java.util.List<MemberTechStack> techStacks = new java.util.ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt; // 최초 생성 시각

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt; // 마지막 수정 시각

  /**
   * 새 멤버를 만들 때 사용하는 정적 팩토리 메서드.
   *
   * <p>new Member()를 직접 쓰는 대신 이 메서드를 통해야 한다. 필수값(name, email, sessionType)만 받고, 나머지는 나중에
   * updateProfile()로 채운다.
   */
  public static Member create(
      String name,
      String email,
      SessionType sessionType,
      String department,
      String profileImageUrl,
      String githubUrl,
      String linksJson) {
    Member member = new Member();
    member.name = name;
    member.email = email;
    member.sessionType = sessionType;
    member.department = department;
    member.profileImageUrl = profileImageUrl;
    member.githubUrl = githubUrl;
    member.linksJson = linksJson;
    return member;
  }

  public void update(
      String name,
      String email,
      SessionType sessionType,
      String department,
      String profileImageUrl,
      String githubUrl,
      String linksJson) {
    this.name = name;
    this.email = email;
    this.sessionType = sessionType;
    this.department = department;
    this.profileImageUrl = profileImageUrl;
    this.githubUrl = githubUrl;
    this.linksJson = linksJson;
  }

  /**
   * JPA가 DB에 INSERT(저장)하기 직전에 자동으로 호출하는 콜백 메서드. createdAt과 updatedAt을 현재 시각으로 초기화한다. 개발자가 직접 호출할 필요
   * 없이 JPA가 알아서 실행한다.
   */
  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  /** JPA가 DB에 UPDATE(수정)하기 직전에 자동으로 호출하는 콜백 메서드. updatedAt을 현재 시각으로 갱신한다. */
  @PreUpdate
  void preUpdate() {
    this.updatedAt = Instant.now();
  }
}
