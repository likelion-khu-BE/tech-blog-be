package com.study.sessionboard.presentation;

import com.study.sessionboard.domain.event.EventPostStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EventPostRequest {

  @NotBlank(message = "제목은 필수입니다.") private String title;

  @NotBlank(message = "본문은 필수입니다.") private String body;

  private String type;

  private Long generationId;

  private EventPostStatus status; // DRAFT or PUBLISHED

  private List<String> tags; // null 허용
}
