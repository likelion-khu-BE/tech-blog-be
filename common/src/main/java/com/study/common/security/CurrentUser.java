package com.study.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 현재 인증된 사용자를 컨트롤러 파라미터로 주입하는 메타 어노테이션.
 *
 * <p>사용법: {@code public ResponseEntity<?> foo(@CurrentUser CustomUserDetails user)}
 *
 * <p>내부적으로 {@link AuthenticationPrincipal}을 위임한다. 팀 모듈에서 Spring Security 어노테이션을 직접 쓰지 않아도 되도록 감싸는
 * 역할.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
public @interface CurrentUser {}
