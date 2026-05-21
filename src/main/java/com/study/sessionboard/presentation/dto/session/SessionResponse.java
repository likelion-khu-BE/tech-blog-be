package com.study.sessionboard.presentation.dto.session;

import com.study.sessionboard.domain.session.Session;
import com.study.sessionboard.domain.session.SessionSpeaker;
import com.study.sessionboard.domain.session.SessionStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record SessionResponse(
    Long id,
    String weekLabel,
    String title,
    SessionStatus status,
    OffsetDateTime startedAt,
    List<SpeakerResponse> speakers,
    double rating,
    long noteCount,
    long resourceCount) {

  public record SpeakerResponse(Long id, String name, String role) {}

  public static SessionResponse of(
      Session session,
      List<SessionSpeaker> speakers,
      Double averageRating,
      long noteCount,
      long resourceCount) {
    return new SessionResponse(
        session.getId(),
        session.getWeekLabel(),
        session.getTitle(),
        session.getStatus(),
        session.getStartedAt(),
        speakers.stream()
            .map(
                s ->
                    new SpeakerResponse(
                        s.getMember().getId(), s.getMember().getName(), s.getRole()))
            .toList(),
        averageRating != null ? Math.round(averageRating * 10) / 10.0 : 0.0,
        noteCount,
        resourceCount);
  }
}
