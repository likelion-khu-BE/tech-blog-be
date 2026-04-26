package com.study.auth.presentation.dto;

import com.study.auth.domain.UserStatus;

public record SignupResponse(Long userId, String email, UserStatus status) {}
