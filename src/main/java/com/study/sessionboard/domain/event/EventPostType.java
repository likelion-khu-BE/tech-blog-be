package com.study.sessionboard.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventPostType {
  HACKATHON,
  IDEATHON,
  WORKSHOP,
  PROJECT,
  MEETUP;

  @JsonValue
  public String toValue() {
    return name().toLowerCase();
  }

  @JsonCreator
  public static EventPostType from(String value) {
    return valueOf(value.toUpperCase());
  }
}
