package com.study.sessionboard.application.event.dto;

import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** 단건 상세 조회용 응답 DTO. */
public class EventPostResponse {

  private final Long id;
  private final EventPostType type;
  private final String title;
  private final String body;
  private final LocalDate eventDate;
  private final String location;
  private final Long authorId;
  private final String authorName;
  private final String generationLabel;
  private final List<String> imageUrls;
  private final int likeCount;
  private final OffsetDateTime createdAt;

  private EventPostResponse(
      Long id,
      EventPostType type,
      String title,
      String body,
      LocalDate eventDate,
      String location,
      Long authorId,
      String authorName,
      String generationLabel,
      List<String> imageUrls,
      int likeCount,
      OffsetDateTime createdAt) {
    this.id = id;
    this.type = type;
    this.title = title;
    this.body = body;
    this.eventDate = eventDate;
    this.location = location;
    this.authorId = authorId;
    this.authorName = authorName;
    this.generationLabel = generationLabel;
    this.imageUrls = imageUrls;
    this.likeCount = likeCount;
    this.createdAt = createdAt;
  }

  public static EventPostResponse of(EventPost post, List<String> imageUrls) {
    return new EventPostResponse(
        post.getId(),
        post.getType(),
        post.getTitle(),
        post.getBody(),
        post.getEventDate(),
        post.getLocation(),
        post.getAuthor().getId(),
        post.getAuthor().getName(),
        post.getGeneration().getLabel(),
        imageUrls,
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

  public String getBody() {
    return body;
  }

  public LocalDate getEventDate() {
    return eventDate;
  }

  public String getLocation() {
    return location;
  }

  public Long getAuthorId() {
    return authorId;
  }

  public String getAuthorName() {
    return authorName;
  }

  public String getGenerationLabel() {
    return generationLabel;
  }

  public List<String> getImageUrls() {
    return imageUrls;
  }

  public int getLikeCount() {
    return likeCount;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
