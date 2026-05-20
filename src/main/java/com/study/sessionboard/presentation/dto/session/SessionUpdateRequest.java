package com.study.sessionboard.presentation.dto.session;

import com.study.sessionboard.domain.session.SessionStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record SessionUpdateRequest(
    String weekLabel,
    String title,
    SessionStatus status,
    OffsetDateTime startedAt,
    List<Long> speakerIds) {}
