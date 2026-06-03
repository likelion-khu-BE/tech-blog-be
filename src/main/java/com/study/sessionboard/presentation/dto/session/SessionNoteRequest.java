package com.study.sessionboard.presentation.dto.session;

import java.util.List;

public record SessionNoteRequest(String body, List<NoteLinkRequest> links) {

  public record NoteLinkRequest(String label, String url, int order) {}
}
