package com.study.sessionboard.presentation.dto;

import java.time.OffsetDateTime;

public record EventPostUpdateResponse(Long id, OffsetDateTime updatedAt) {}
