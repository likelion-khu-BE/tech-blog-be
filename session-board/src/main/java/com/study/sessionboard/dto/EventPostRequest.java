package com.study.sessionboard.dto;

import com.study.common.entity.PostStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class EventPostRequest {

  private UUID authorId;
  private Integer generationId;
  private String type;
  private String title;
  private String body;
  private List<String> tags = new ArrayList<>();
  private PostStatus status;
  private List<String> imageUrls = new ArrayList<>();

  public String[] getTagsAsArray() {
    return tags != null ? tags.toArray(new String[0]) : new String[0];
  }
}
