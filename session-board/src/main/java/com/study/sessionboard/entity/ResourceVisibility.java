package com.study.sessionboard.entity;

public enum ResourceVisibility {
  PUBLIC,
  MEMBER,
  PRIVATE;

  public String toDbValue() {
    return name().toLowerCase();
  }

  public static ResourceVisibility fromDbValue(String value) {
    return valueOf(value.toUpperCase());
  }
}
