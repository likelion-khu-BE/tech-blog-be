package com.study.sessionboard.presentation.dto;

import com.study.sessionboard.domain.event.EventPostStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record EventPostResponse(
    Long id,
    String type,
    EventPostStatus status,
    String title,
    AuthorResponse author,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String excerpt,
    String body,
    List<String> tags,
    List<ImageResponse> images,
    int likeCount,
    boolean likedByMe,
    int commentCount) {

  public record AuthorResponse(Long id, String name, String initial) {}

  public record ImageResponse(int order, String url) {}
}
