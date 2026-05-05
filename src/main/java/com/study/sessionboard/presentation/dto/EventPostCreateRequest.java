package com.study.sessionboard.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EventPostCreateRequest {
  @NotBlank private String type;

  @NotBlank @Size(max = 100) private String title;

  @NotBlank @Size(max = 10000) private String body;

  @Size(max = 10) private List<@Size(max = 20) String> tags;
}
