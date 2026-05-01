package com.study.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.study.profile.application.dto.MemberSummaryDto;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link MemberQueryServiceImpl} 단위 테스트 — Mock 기반, DB 없음.
 *
 * <p>핵심 로직 검증: Member 있음/없음 분기 + DTO 필드 매핑.
 *
 * <p>JPA 쿼리({@code findByUserId})와 트랜잭션 전파는 Spring 책임이라 검증 대상 아님.
 */
@ExtendWith(MockitoExtension.class)
class MemberQueryServiceImplTest {

  @Mock private MemberRepository memberRepository;

  @InjectMocks private MemberQueryServiceImpl memberQueryService;

  @Nested
  @DisplayName("getById")
  class GetById {

    @Test
    @DisplayName("Member 있음 → Optional.of(dto), 필드 매핑 정확")
    void getById_memberExists_returnsDto() {
      // Given
      Member member = mock(Member.class);
      given(member.getId()).willReturn(100L);
      given(member.getName()).willReturn("임근엽");
      given(member.getProfileImageUrl()).willReturn("https://img/k.png");
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      // When
      Optional<MemberSummaryDto> result = memberQueryService.getById(1L);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().memberId()).isEqualTo(100L);
      assertThat(result.get().name()).isEqualTo("임근엽");
      assertThat(result.get().profileImageUrl()).isEqualTo("https://img/k.png");
    }

    @Test
    @DisplayName("profileImageUrl이 null이어도 정상 매핑 (record 자체는 null 허용)")
    void getById_nullProfileImage_mapsCorrectly() {
      // Given
      Member member = mock(Member.class);
      given(member.getId()).willReturn(100L);
      given(member.getName()).willReturn("임근엽");
      given(member.getProfileImageUrl()).willReturn(null);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      // When
      Optional<MemberSummaryDto> result = memberQueryService.getById(1L);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().profileImageUrl()).isNull();
    }

    @Test
    @DisplayName("Member 없음 → Optional.empty()")
    void getById_memberNotFound_returnsEmpty() {
      // Given
      given(memberRepository.findByUserId(999L)).willReturn(Optional.empty());

      // When
      Optional<MemberSummaryDto> result = memberQueryService.getById(999L);

      // Then
      assertThat(result).isEmpty();
    }
  }
}
