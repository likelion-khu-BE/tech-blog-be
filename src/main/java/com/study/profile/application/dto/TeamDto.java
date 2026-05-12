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
}
