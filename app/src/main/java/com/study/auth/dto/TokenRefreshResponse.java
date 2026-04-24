package com.study.auth.dto;

public record TokenRefreshResponse(String accessToken, String tokenType) {

  public TokenRefreshResponse(String accessToken) {
    this(accessToken, "Bearer");
  }
}
