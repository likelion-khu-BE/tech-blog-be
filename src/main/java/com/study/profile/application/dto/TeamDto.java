package com.study.profile.application.dto;

import java.time.Instant;
import java.util.List;

public class TeamDto {

  public record TeamCreateRequest(
      String name,
      String description,
      String projectUrl,
      String githubUrl,
      Integer generationNumber,
      List<String> imageUrls,
      List<Long> techStackIds) {}

  public record TeamCreateResponse(Long id, String inviteCode, Instant inviteCodeExpiresAt) {}

  public record TeamUpdateRequest(
      String name,
      String description,
      String projectUrl,
      String githubUrl,
      Integer generationNumber,
      List<String> imageUrls,
      List<Long> techStackIds) {}

  public record TeamUpdateResponse(Long id, Instant updatedAt) {}

  public record InviteCodeResponse(String inviteCode, Instant inviteCodeExpiresAt) {}

  public record GenerationSummary(Integer number) {}

  public record TechStackSummary(Long id, String name, String category, String logoUrl) {}

  public record TeamListResponse(
      Long id,
      String name,
      String description,
      GenerationSummary generation,
      List<TechStackSummary> techStacks,
      int memberCount,
      String thumbUrl) {}

  public record TeamMemberSummary(
      Long memberId,
      String name,
      String sessionType,
      String profileImageUrl,
      boolean isLead,
      List<String> roles) {}

  public record TeamDetailResponse(
      Long id,
      String name,
      String description,
      String projectUrl,
      String githubUrl,
      GenerationSummary generation,
      List<TechStackSummary> techStacks,
      List<String> imageUrls,
      List<TeamMemberSummary> members,
      String inviteCode,
      Instant inviteCodeExpiresAt,
      Instant updatedAt) {}
}
