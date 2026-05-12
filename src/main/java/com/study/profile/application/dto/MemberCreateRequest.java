package com.study.profile.application.dto;

import com.study.profile.domain.member.SessionType;

public record MemberCreateRequest(
    String name,
    SessionType sessionType,
    String department,
    String profileImageUrl,
    String githubUrl,
    String displayedEmail,
    String intro,
    String linksJson) {}
