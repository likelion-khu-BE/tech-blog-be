package com.study.auth.presentation.dto;

public record TokenRefreshResponse(String accessToken, String tokenType) {

  public TokenRefreshResponse(String accessToken) {
    this(accessToken, "Bearer");
  }
}
