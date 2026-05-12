package com.study.profile.application.dto;

import com.study.profile.domain.member.Member;
import com.study.profile.domain.member.SessionType;
import java.time.Instant;
import java.util.List;

public record MemberDto(
    Long id,
    String name,
    String department,
    SessionType sessionType,
    String profileImageUrl,
    String githubUrl,
    String displayedEmail,
    String intro,
    String linksJson,
    List<TechStackItemDto> techStacks,
    List<MemberGenerationDto> generations,
    Instant createdAt,
    Instant updatedAt) {

  public static MemberDto from(Member member, List<MemberGenerationDto> generations) {
    List<TechStackItemDto> techStacks =
        member.getTechStacks().stream().map(TechStackItemDto::from).toList();
    return new MemberDto(
        member.getId(),
        member.getName(),
        member.getDepartment(),
        member.getSessionType(),
        member.getProfileImageUrl(),
        member.getGithubUrl(),
        member.getDisplayedEmail(),
        member.getIntro(),
        member.getLinksJson(),
        techStacks,
        generations,
        member.getCreatedAt(),
        member.getUpdatedAt());
  }
}
