package com.study.profile.infrastructure;

import com.study.profile.domain.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {}
