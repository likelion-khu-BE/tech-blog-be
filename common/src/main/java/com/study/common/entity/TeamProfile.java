package com.study.common.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
 * 팀 프로필 엔티티.
 *
 * <p>멋쟁이사자처럼에서 멤버들이 모여 만든 프로젝트 팀 정보를 저장한다. 어느 기수에서 만들어진 팀인지, 팀 이름·설명·프로젝트 URL을 관리한다. 팀에 참여한 멤버 목록은
 * 별도 TeamMember 엔티티에서 관리한다.
 *
 * <p>[DB 테이블: team_profile]
 */
@Entity
@DynamicUpdate // 변경된 필드만 UPDATE 쿼리에 포함 (description 하나만 바꿔도 전체를 UPDATE하지 않는다)
@Table(name = "team_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id; // DB가 자동으로 부여하는 고유 번호

  // 이 팀이 속한 기수 — ON DELETE SET NULL이므로 기수가 삭제돼도 팀은 남고 generation만 null이 된다.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "generation_id")
  private Generation generation;

  @Column(name = "name", nullable = false)
  private String name; // 팀 이름 (예: "쇼핑몰팀", "헬스케어팀")

  @Column(name = "description")
  private String description; // 팀 소개·설명 (선택 입력)

  @Column(name = "project_url")
  private String projectUrl; // 배포 URL (선택 입력)

  @Column(name = "github_url")
  private String githubUrl; // 프로젝트 GitHub (선택 입력)

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt; // 팀 생성 시각

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt; // 마지막 수정 시각

  @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TeamImage> images = new ArrayList<>();

  /**
   * 이미지들을 추가하는 메서드
   *
   * @param imageUrls 추가할 이미지 경로 리스트 (null이거나 비어있으면 아무것도 안 함)
   */
  public void updateImages(List<String> imageUrls) {
    // 기존 이미지를 싹 지우고 새로 갈아끼우거나, 유지한 채 추가하는 로직 중 선택
    this.images.clear();
    if (imageUrls != null) {
      for (String url : imageUrls) {
        this.images.add(TeamImage.create(this, url));
      }
    }
  }

  // TeamProfile 클래스 안에 추가
  @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TeamTechStack> techStacks = new ArrayList<>();

  /** 팀의 사용 기술 스택을 업데이트하는 메서드 */
  public void updateTechStacks(List<TechStack> newStacks) {
    this.techStacks.clear();
    if (newStacks != null) {
      for (TechStack stack : newStacks) {
        this.techStacks.add(TeamTechStack.create(this, stack));
      }
    }
  }

  /** 새 팀을 만들 때 사용하는 정적 팩토리 메서드. 팀 이름과 소속 기수만 필수이고, 설명·URL은 update()로 나중에 채울 수 있다. */
  public static TeamProfile create(Generation generation, String name) {
    TeamProfile teamProfile = new TeamProfile();
    teamProfile.generation = generation;
    teamProfile.name = name;
    return teamProfile;
  }

  /** 팀의 설명과 프로젝트 URL을 수정할 때 호출하는 메서드. */
  public void update(String description, String projectUrl, String githubUrl) {
    this.description = description;
    this.projectUrl = projectUrl;
    this.githubUrl = githubUrl;
  }

  /** DB에 INSERT하기 직전 JPA가 자동으로 createdAt, updatedAt을 현재 시각으로 초기화한다. */
  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  /** DB에 UPDATE하기 직전 JPA가 자동으로 updatedAt을 현재 시각으로 갱신한다. */
  @PreUpdate
  void preUpdate() {
    this.updatedAt = Instant.now();
  }
}
