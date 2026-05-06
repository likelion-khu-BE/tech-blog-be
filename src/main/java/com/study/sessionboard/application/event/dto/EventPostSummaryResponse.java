package com.study.sessionboard.application.event.dto;

import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostType;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

public record EventPostSummaryResponse(
    Long id,
    EventPostType type,
    String title,
    AuthorDto author,
    OffsetDateTime createdAt,
    String excerpt,
    List<String> tags,
    int likeCount,
    int commentCount,
    boolean hasThumb,
    String thumbUrl) {

  public static EventPostSummaryResponse of(EventPost post, String thumbUrl, int commentCount) {
    return new EventPostSummaryResponse(
        post.getId(),
        post.getType(),
        post.getTitle(),
        AuthorDto.from(post.getAuthor()),
        post.getCreatedAt(),
        extractExcerpt(post.getBody()),
        Arrays.asList(post.getTags()),
        post.getLikeCount(),
        commentCount,
        thumbUrl != null,
        thumbUrl);
  }

  private static String extractExcerpt(String body) {
    if (body == null) return null;
    String stripped = body.replaceAll("<[^>]*>", "").strip();
    return stripped.length() <= 100 ? stripped : stripped.substring(0, 100) + "...";
  }
}
