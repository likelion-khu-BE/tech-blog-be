package com.study.sessionboard.presentation.dto.session;

import com.study.sessionboard.domain.session.SessionNote;
import java.time.OffsetDateTime;
import java.util.List;

public record SessionNoteResponse(
    Long id,
    AuthorResponse author,
    String body,
    List<NoteLinkResponse> links,
    OffsetDateTime createdAt) {

  public record AuthorResponse(Long id, String name, String initial) {}

  public record NoteLinkResponse(String label, String url, int order) {}

  public static SessionNoteResponse from(SessionNote note) {
    String name = note.getAuthor().getName();
    String initial = name.length() >= 2 ? name.substring(0, 2) : name;

    return new SessionNoteResponse(
        note.getId(),
        new AuthorResponse(note.getAuthor().getId(), name, initial),
        note.getBody(),
        note.getLinks().stream()
            .map(link -> new NoteLinkResponse(link.getLabel(), link.getUrl(), link.getOrder()))
            .toList(),
        note.getCreatedAt());
  }
}
