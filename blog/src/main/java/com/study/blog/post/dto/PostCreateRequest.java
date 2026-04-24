package com.study.blog.post.dto;

import com.study.common.entity.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PostCreateRequest {

  @NotBlank private String title;

  @NotBlank private String content;

  @NotBlank private String board;

  @NotBlank private String category;

  @NotNull private PostStatus status;

  @NotBlank private String generation;

  private List<String> tags;

  private Long repostFromId;

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public String getBoard() {
    return board;
  }

  public String getCategory() {
    return category;
  }

  public PostStatus getStatus() {
    return status;
  }

  public String getGeneration() {
    return generation;
  }

  public List<String> getTags() {
    return tags;
  }

  public Long getRepostFromId() {
    return repostFromId;
  }
}
