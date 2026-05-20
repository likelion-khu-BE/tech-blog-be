package com.study.sessionboard.presentation.dto.session;

import java.time.OffsetDateTime;

public record SessionUpdateResponse(Long id, OffsetDateTime updatedAt) {}
