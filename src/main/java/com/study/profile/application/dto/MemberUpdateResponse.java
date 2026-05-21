package com.study.profile.application.dto;

import com.study.profile.domain.member.Member;
import java.time.Instant;

public record MemberUpdateResponse(Long id, String name, Instant updatedAt) {

  public static MemberUpdateResponse from(Member member) {
    return new MemberUpdateResponse(member.getId(), member.getName(), member.getUpdatedAt());
  }
}
