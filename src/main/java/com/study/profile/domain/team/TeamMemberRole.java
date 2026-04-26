package com.study.profile.domain.team;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 팀원 역할 엔티티.
 *
 * <p>한 팀원(TeamMember)이 맡은 역할 분야(RoleInTeam)를 기록한다. 한 팀원이 여러 역할을 가질 수 있으므로 TeamMember와 1:N 관계다.
 *
 * <p>예) 홍길동(팀원)이 backend와 infra 두 역할을 맡으면 team_member_role 행이 2개 생긴다: → (team_member_id=1,
 * role=backend) → (team_member_id=1, role=infra)
 *
 * <p>[DB 테이블: team_member_role]
 */
@Entity
@Table(name = "team_member_role")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMemberRole {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id; // 이 행의 고유 번호

  // 어떤 팀원인지 (team_member_id 외래키로 TeamMember 테이블 참조)
  // ON DELETE CASCADE : 팀원이 삭제되면 이 역할 행도 자동으로 삭제된다.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_member_id", nullable = false)
  private TeamMember teamMember;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private RoleInTeam role; // 역할 분야 (backend/frontend/design/ai/pm/infra/etc)

  /** 팀원에게 역할을 부여할 때 사용하는 정적 팩토리 메서드. TeamMember.create() 내부에서 역할 수만큼 반복 호출된다. */
  public static TeamMemberRole create(TeamMember teamMember, RoleInTeam role) {
    TeamMemberRole tmr = new TeamMemberRole();
    tmr.teamMember = teamMember;
    tmr.role = role;
    return tmr;
  }
}
