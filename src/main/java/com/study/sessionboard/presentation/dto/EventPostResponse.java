package com.study.sessionboard.presentation.dto;

import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostImage;
import com.study.sessionboard.domain.event.EventPostStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

  public static EventPostResponse of(EventPost post, List<EventPostImage> images) {
    return new EventPostResponse(
        post.getId(),
        post.getType().name(),
        post.getStatus(),
        post.getTitle(),
        new AuthorResponse(
            post.getAuthor().getId(),
            post.getAuthor().getName(),
            post.getAuthor()
                .getName()
                .substring(0, Math.min(post.getAuthor().getName().length(), 2))),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        extractExcerpt(post.getBody()),
        post.getBody(),
        List.of(post.getTags()),
        images.stream()
            .map(img -> new ImageResponse(img.getOrder(), img.getUrl()))
            .collect(Collectors.toList()),
        post.getLikeCount(),
        false,
        post.getCommentCount());
  }

  private static String extractExcerpt(String body) {
    if (body == null || body.isEmpty()) {
      return "";
    }
    String stripped = body.replaceAll("<[^>]*>", "").strip();
    return stripped.length() <= 100 ? stripped : stripped.substring(0, 100);
  }
}
