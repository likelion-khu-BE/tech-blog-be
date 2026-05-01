package com.study.profile.application;

import com.study.profile.application.dto.MemberSummaryDto;
import java.util.Optional;

/**
 * 외부 BC가 작성자 정보를 조회할 때 호출하는 빈.
 *
 * <p>외부 BC(blog/qna/sessionboard)가 자기 응답에 작성자 이름·프사를 표시할 때 이 빈을 직접 주입받아 호출. HTTP X — 모놀리식 jar 안에서 빈
 * 직접 의존.
 *
 * <p>식별자 단 분리: 입력은 {@code userId}({@code auth.User.id}, 모든 BC 공유), 응답에 {@code memberId}({@code
 * Member.id}, 라우팅 토큰)을 포함. 외부 BC는 Member 도메인을 import 안 해도 됨.
 *
 * <p>관련 문서: {@code docs/profile/member-query-service.md}
 */
public interface MemberQueryService {

  /**
   * 작성자 단건 조회.
   *
   * @param userId auth가 발급한 식별자 ({@code @CurrentUser}로 받은 값 그대로)
   * @return 작성자 요약 정보 (없으면 {@link Optional#empty()} — profile-init 미완료 가능). 호출부가 처리 정책 결정.
   */
  Optional<MemberSummaryDto> getById(Long userId);
}
