package com.study.auth.dto;

import com.study.common.entity.UserStatus;

public record SignupResponse(Long userId, String email, UserStatus status) {}
