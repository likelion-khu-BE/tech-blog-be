package com.study.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id")
  private TeamProfile team;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Column(name = "role_in_team")
  private String roleInTeam;

  @Column(name = "is_lead", nullable = false)
  private Boolean isLead = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public static TeamMember create(TeamProfile team, Member member, String roleInTeam, boolean isLead) {
    TeamMember teamMember = new TeamMember();
    teamMember.team = team;
    teamMember.member = member;
    teamMember.roleInTeam = roleInTeam;
    teamMember.isLead = isLead;
    return teamMember;
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
