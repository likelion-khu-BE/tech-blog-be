package com.study.profile.infrastructure;

import com.study.profile.domain.techstack.MemberTechStack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTechStackRepository extends JpaRepository<MemberTechStack, Long> {

  void deleteByMemberId(Long memberId);
}
