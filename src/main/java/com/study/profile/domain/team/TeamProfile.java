package com.study.profile.domain.team;

import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.techstack.TeamTechStack;
import com.study.profile.domain.techstack.TechStack;

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
    // 1. 만약 프론트에서 null을 보냈다면? "수정 안 함"으로 간주하고 보호!
    if (imageUrls == null) {
      return;
    }

    // 2. null이 아닐 때만(빈 리스트 [] 포함) 기존 걸 지웁니다.
    // 빈 리스트 []가 들어오면 clear()만 되고 루프를 안 돌아서 "전부 삭제"가 됩니다.
    this.images.clear();

    for (String url : imageUrls) {
      this.images.add(TeamImage.create(this, url));
    }
  }

  // TeamProfile 클래스 안에 추가
  @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TeamTechStack> techStacks = new ArrayList<>();

  /** 팀의 사용 기술 스택을 업데이트하는 메서드 */
  public void updateTechStacks(List<TechStack> newStacks) {
    // 1. null이면 아무것도 하지 않고 기존 스택 유지 (방어 로직)
    if (newStacks == null) {
      return;
    }

    // 2. 기존 스택 비우기
    this.techStacks.clear();

    // 3. 중복을 제거(.distinct())하고 리스트에 추가
    newStacks.stream()
        .distinct()
        .forEach(stack -> this.techStacks.add(TeamTechStack.create(this, stack)));
  }

  public static TeamProfile create(
      Generation generation, String name, String description, String projectUrl, String githubUrl) {
    TeamProfile teamProfile = new TeamProfile();
    teamProfile.generation = generation;
    teamProfile.name = name;
    teamProfile.description = description;
    teamProfile.projectUrl = projectUrl;
    teamProfile.githubUrl = githubUrl;
    return teamProfile;
  }

  public void update(
      Generation generation, String name, String description, String projectUrl, String githubUrl) {
    this.generation = generation;
    this.name = name;
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
