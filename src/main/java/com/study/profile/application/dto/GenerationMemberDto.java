package com.study.profile.application.dto;

import com.study.profile.domain.generation.GenerationRole;
import com.study.profile.domain.generation.MemberGeneration;
import com.study.profile.domain.member.SessionType;
import java.time.Instant;

public record GenerationMemberDto(
    Long memberId,
    String name,
    SessionType sessionType,
    String profileImageUrl,
    GenerationRole roleInGen,
    Instant joinedAt) {

  public static GenerationMemberDto from(MemberGeneration mg) {
    return new GenerationMemberDto(
        mg.getMember().getId(),
        mg.getMember().getName(),
        mg.getMember().getSessionType(),
        mg.getMember().getProfileImageUrl(),
        mg.getRoleInGen(),
        mg.getJoinedAt());
  }
}
