package com.study.sessionboard.presentation.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventPostUpdateResponse {
  private Long id;
  private OffsetDateTime updatedAt;
}
