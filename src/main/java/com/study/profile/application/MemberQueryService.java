package com.study.profile.application;

import com.study.profile.application.dto.MemberSummaryDto;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 외부 BC가 작성자 정보를 조회할 때 호출하는 빈.
 *
 * <p>외부 BC(blog/qna/sessionboard)가 자기 응답에 작성자 이름·프사를 표시할 때 이 빈을 직접 주입받아 호출. HTTP X — 모놀리식 jar 안에서 빈
 * 직접 의존.
 *
 * <p>식별자 단 분리: 입력은 {@code userId}({@code auth.User.id}, 모든 BC 공유), 응답에 {@code memberId}({@code
 * Member.id}, 라우팅 토큰)을 포함. 외부 BC는 Member 도메인을 import 안 해도 됨.
 *
 * <p>Member 없는 호출은 시스템 불일치(profile-init 강제 + ACTIVE 사용자 = Member 보장)라 {@link
 * IllegalStateException}을 던진다. {@link MemberRepository} 레벨의 {@code Optional}을 흡수해서 비즈니스 시맨틱(보장된
 * 매핑)으로 변환.
 *
 * <p>관련 문서: {@code docs/profile/member-query-service.md}
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

  private final MemberRepository memberRepository;

  /**
   * 작성자 단건 조회.
   *
   * @param userId auth가 발급한 식별자 ({@code @CurrentUser}로 받은 값 그대로)
   * @return 작성자 요약 정보
   * @throws IllegalStateException Member 없음 (시스템 불일치 — profile-init 미완료 가능)
   */
  public MemberSummaryDto getById(Long userId) {
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "userId=" + userId + " 에 해당하는 Member 없음. profile-init 미완료 가능."));
    return new MemberSummaryDto(member.getId(), member.getName(), member.getProfileImageUrl());
  }
}
