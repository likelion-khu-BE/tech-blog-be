package com.study.auth.presentation.dto;

import java.time.Instant;

// 시현 N+1 수정: memberId 추가 — 어드민 페이지에서 멤버 프로필 직접 링크용
public record UserResponse(
    Long id,
    Long memberId,
    String email,
    String role,
    String status,
    Instant signupRequestedAt,
    Instant approvedAt) {}
