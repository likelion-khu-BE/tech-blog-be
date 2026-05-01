package com.study.profile.infrastructure;

import com.study.profile.domain.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Member 엔티티 Repository.
 *
 * <p>세인 영역(Member 도메인) 인프라. 현재는 외부 BC 호출용 {@link com.study.profile.application.MemberQueryService}가
 * {@code userId → Member} 매핑에 사용.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

  /**
   * {@code auth.User.id}로 Member 조회.
   *
   * <p>외부 BC가 {@code @CurrentUser}로 받은 {@code userId}를 우리한테 넘기면, 우리는 이걸 Member로 매핑. 식별자 단 분리({@code
   * userId} vs {@code memberId}) 패턴의 입력 → 내부 변환 지점.
   *
   * @param userId auth.User.id
   * @return Member ({@link Optional} — profile-init 미완료 시 빈)
   */
  Optional<Member> findByUserId(Long userId);
}
