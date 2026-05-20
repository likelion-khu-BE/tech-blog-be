package com.study.auth.presentation.dto;

import java.time.Instant;

public record UserResponse(
    Long id,
    Long memberId,
    String email,
    String role,
    String status,
    Instant signupRequestedAt,
    Instant approvedAt) {}
