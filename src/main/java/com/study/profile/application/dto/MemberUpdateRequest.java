package com.study.profile.application.dto;

import com.study.profile.domain.member.SessionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberUpdateRequest(
    @NotBlank String name,
    @NotNull SessionType sessionType,
    String department,
    String profileImageKey,
    String githubUrl,
    String displayedEmail,
    String intro,
    String linksJson) {}
