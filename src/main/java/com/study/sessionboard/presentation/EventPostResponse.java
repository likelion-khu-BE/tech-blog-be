package com.study.sessionboard.presentation;

import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostStatus;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventPostResponse {

  private Long id;
  private Long authorId;
  private String authorName;
  private Long generationId;
  private String type;
  private String title;
  private String body;
  private List<String> tags;
  private EventPostStatus status;
  private int likeCount;
  private OffsetDateTime publishedAt;
  private OffsetDateTime createdAt;
  private List<String> imageUrls; // 이미지 URL 목록

  public static EventPostResponse from(EventPost post, List<String> imageUrls) {
    return EventPostResponse.builder()
        .id(post.getId())
        .authorId(post.getAuthor().getId())
        .authorName(post.getAuthor().getName()) // Member에 getName() 있다고 가정
        .generationId(post.getGeneration().getId())
        .type(post.getType())
        .title(post.getTitle())
        .body(post.getBody())
        .tags(post.getTags() != null ? Arrays.asList(post.getTags()) : List.of())
        .status(post.getStatus())
        .likeCount(post.getLikeCount())
        .publishedAt(post.getPublishedAt())
        .createdAt(post.getCreatedAt())
        .imageUrls(imageUrls != null ? imageUrls : List.of())
        .build();
  }
}
