package com.study.sessionboard.presentation.dto;

import com.study.sessionboard.domain.event.EventPostStatus;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventPostResponse {
  private Long id;
  private String type;
  private EventPostStatus status;
  private String title;
  private AuthorResponse author;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private String excerpt;
  private String body;
  private List<String> tags;
  private List<ImageResponse> images;
  private int likeCount;
  private boolean likedByMe;
  private int commentCount;

  @Getter
  @Builder
  public static class AuthorResponse {
    private Long id;
    private String name;
    private String initial;
  }

  @Getter
  @Builder
  public static class ImageResponse {
    private int order;
    private String url;
  }
}
