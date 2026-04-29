package com.study.sessionboard.application.event.dto;

import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostType;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** 목록/앨범 조회용 응답 DTO. thumbnailUrl은 첫 번째 이미지 URL (없으면 null). */
public class EventPostSummaryResponse {

  private final Long id;
  private final EventPostType type;
  private final String title;
  private final LocalDate eventDate;
  private final String location;
  private final String authorName;
  private final String thumbnailUrl;
  private final int likeCount;
  private final OffsetDateTime createdAt;

  private EventPostSummaryResponse(
      Long id,
      EventPostType type,
      String title,
      LocalDate eventDate,
      String location,
      String authorName,
      String thumbnailUrl,
      int likeCount,
      OffsetDateTime createdAt) {
    this.id = id;
    this.type = type;
    this.title = title;
    this.eventDate = eventDate;
    this.location = location;
    this.authorName = authorName;
    this.thumbnailUrl = thumbnailUrl;
    this.likeCount = likeCount;
    this.createdAt = createdAt;
  }

  public static EventPostSummaryResponse of(EventPost post, String thumbnailUrl) {
    return new EventPostSummaryResponse(
        post.getId(),
        post.getType(),
        post.getTitle(),
        post.getEventDate(),
        post.getLocation(),
        post.getAuthor().getName(),
        thumbnailUrl,
        post.getLikeCount(),
        post.getCreatedAt());
  }

  public Long getId() {
    return id;
  }

  public EventPostType getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public LocalDate getEventDate() {
    return eventDate;
  }

  public String getLocation() {
    return location;
  }

  public String getAuthorName() {
    return authorName;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public int getLikeCount() {
    return likeCount;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
