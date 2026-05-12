package com.study.profile.application;

import com.study.profile.application.dto.MemberCreateRequest;
import com.study.profile.domain.member.SessionType;
import com.study.shared.extevent.auth.UserSignedUpEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 회원가입 이벤트를 수신해 Member 프로필을 생성한다.
 *
 * <p>{@code @EventListener}는 발행자(AuthService)와 동일한 트랜잭션 내에서 동기적으로 실행된다.
 * Member 생성 실패 시 User 저장도 함께 롤백되어 불완전한 상태가 남지 않는다.
 */
@Component
@RequiredArgsConstructor
public class MemberRegistrationListener {

  private final MemberService memberService;

  @EventListener
  public void onUserSignedUp(UserSignedUpEvent event) {
    SessionType sessionType;
    try {
      sessionType = SessionType.valueOf(event.sessionType().toLowerCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "유효하지 않은 sessionType입니다: " + event.sessionType()
              + ". 허용 값: backend, frontend, design, ai, pm, etc");
    }

    MemberCreateRequest req = new MemberCreateRequest(
        event.name(), sessionType, null, null, null, null, null, null);
    memberService.createMember(event.userId(), req);
  }
}
