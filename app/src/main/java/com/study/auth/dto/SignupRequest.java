package com.study.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank @Email String email,

    // 최소 8자 — NIST 800-63B 권장. 대문자/특수문자 강제는 오히려 보안에 역효과라는 게 최신 가이드라인.
    @NotBlank @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다") String password) {

  /** 로그에 비밀번호가 찍히는 것을 원천 차단. */
  @Override
  public String toString() {
    return "SignupRequest[email=" + email + ", password=***]";
  }
}
