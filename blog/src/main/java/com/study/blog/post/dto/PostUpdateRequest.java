package com.study.blog.post.dto;

import com.study.common.entity.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PostUpdateRequest {

  @NotBlank
  private String title;

  @NotBlank
  private String content;

  @NotBlank
  private String board;

  @NotBlank
  private String category;

  @NotNull
  private PostStatus status;

  private List<String> tags;

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

  public List<String> getTags() {
    return tags;
  }
}
