package com.study.auth.presentation.dto;

public record LoginResponse(String accessToken, String tokenType) {

  public LoginResponse(String accessToken) {
    this(accessToken, "Bearer");
  }
}
