package com.study.blog.shared.auth;

import java.util.UUID;

/** Spring Security 연동 전 임시로 사용하는 Mock 인증 유틸. */
public final class MockAuth {

  public static final UUID MOCK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  public static final String ADMIN_TOKEN = "dev-admin-token-2026";

  private MockAuth() {}
}
