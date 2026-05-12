package com.study.profile.infrastructure;

import com.study.profile.domain.team.TeamProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamProfile, Long> {

  Optional<TeamProfile> findByInviteCode(String inviteCode);
}
