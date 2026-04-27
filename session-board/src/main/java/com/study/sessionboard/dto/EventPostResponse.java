package com.study.sessionboard.dto;

import com.study.common.entity.EventPost;
import com.study.common.entity.PostStatus;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventPostResponse {

  private UUID id;
  private UUID authorId;
  private Integer generationId;
  private String type;
  private String title;
  private String body;
  private List<String> tags;
  private PostStatus status;
  private int likeCount;
  private OffsetDateTime publishedAt;
  private OffsetDateTime createdAt;

  public static EventPostResponse from(EventPost post) {
    return EventPostResponse.builder()
        .id(post.getId())
        .authorId(post.getAuthorId())
        .generationId(post.getGenerationId())
        .type(post.getType())
        .title(post.getTitle())
        .body(post.getBody())
        .tags(Arrays.asList(post.getTags()))
        .status(post.getStatus())
        .likeCount(post.getLikeCount())
        .publishedAt(post.getPublishedAt())
        .createdAt(post.getCreatedAt())
        .build();
  }
}
