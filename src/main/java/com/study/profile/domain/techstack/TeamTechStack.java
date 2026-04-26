package com.study.profile.domain.techstack;

import com.study.profile.domain.team.TeamProfile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "team_tech_stack",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_team_tech_stack",
            columnNames = {"team_id", "tech_stack_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamTechStack {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = false)
  private TeamProfile team;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tech_stack_id", nullable = false)
  private TechStack techStack;

  public static TeamTechStack create(TeamProfile team, TechStack techStack) {
    TeamTechStack tts = new TeamTechStack();
    tts.team = team;
    tts.techStack = techStack;
    return tts;
  }
}
