package com.study.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {

  /** 로그에 비밀번호가 찍히는 것을 원천 차단. */
  @Override
  public String toString() {
    return "LoginRequest[email=" + email + ", password=***]";
  }
}
