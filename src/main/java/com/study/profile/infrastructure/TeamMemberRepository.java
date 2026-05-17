package com.study.profile.infrastructure;

import com.study.profile.domain.team.TeamMember;
import com.study.profile.domain.team.TeamMemberStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

  List<TeamMember> findByTeamIdAndStatus(Long teamId, TeamMemberStatus status);

  boolean existsByTeamIdAndMemberIdAndIsLeadTrue(Long teamId, Long memberId);

  void deleteByTeamId(Long teamId);

  boolean existsByTeamIdAndMemberId(Long teamId, Long memberId);
}
