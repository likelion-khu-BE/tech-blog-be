package com.study.common.entity.team;

import com.study.common.entity.member.Member;

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
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 팀-멤버 연결 엔티티 (중간 테이블).
 *
 * <p>"어떤 팀에 어떤 멤버가 참여하고 있는가"를 기록한다. 역할 분야는 별도 TeamMemberRole 테이블에서 관리하며, 한 팀원이 여러 역할을 가질 수 있다.
 *
 * <p>[DB 테이블: team_member]
 */
@Entity
@Table(name = "team_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id; // 이 연결 행의 고유 번호

  // 어떤 팀인지 (team_id 외래키로 TeamProfile 테이블 참조)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = false)
  private TeamProfile team;

  // 어떤 멤버인지 (member_id 외래키로 Member 테이블 참조)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  // 이 팀원이 맡은 역할 목록. TeamMemberRole 테이블에 별도 행으로 저장된다.
  // cascade = ALL : 이 팀원이 저장/삭제될 때 역할 행들도 함께 처리된다.
  // orphanRemoval = true : roles 리스트에서 제거하면 DB에서도 해당 역할 행이 삭제된다.
  @OneToMany(mappedBy = "teamMember", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TeamMemberRole> roles = new ArrayList<>();

  @Column(name = "is_lead", nullable = false)
  private Boolean isLead = false; // 팀장 여부 (true면 팀장, false면 일반 팀원)

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt; // 팀에 합류한 시각

  /**
   * 멤버를 팀에 추가할 때 사용하는 정적 팩토리 메서드.
   *
   * @param roles 이 팀원이 맡을 역할 목록 (예: [backend, infra])
   */
  public static TeamMember create(
      TeamProfile team, Member member, List<RoleInTeam> roles, boolean isLead) {
    TeamMember teamMember = new TeamMember();
    teamMember.team = team;
    teamMember.member = member;
    teamMember.isLead = isLead;
    if (roles != null) {
      for (RoleInTeam role : roles) {
        teamMember.addRole(role);
      }
    }
    return teamMember;
  }

  /** 역할을 하나 추가하는 내부 편의 메서드. */
  private void addRole(RoleInTeam role) {
    this.roles.add(TeamMemberRole.create(this, role));
  }

  /** DB 저장 직전에 JPA가 자동으로 현재 시각을 createdAt에 넣어준다. */
  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
