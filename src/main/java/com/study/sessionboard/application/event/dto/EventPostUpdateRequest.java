package com.study.sessionboard.application.event.dto;

import com.study.sessionboard.domain.event.EventPostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class EventPostUpdateRequest {

  @NotNull private EventPostType type;

  @NotBlank private String title;

  private String body;

  private LocalDate eventDate;

  private String location;

  private List<String> imageUrls;

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

  public List<String> getImageUrls() {
    return imageUrls;
  }
}
