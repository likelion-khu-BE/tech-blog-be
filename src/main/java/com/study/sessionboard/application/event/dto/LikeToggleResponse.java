package com.study.sessionboard.application.event.dto;

public record LikeToggleResponse(boolean likedByMe, int likeCount) {}