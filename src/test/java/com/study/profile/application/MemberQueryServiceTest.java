package com.study.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
 * {@link MemberQueryService} 단위 테스트 — Mock 기반, DB 없음.
 *
 * <p>핵심 로직 검증: Member 매핑 + DTO 필드 변환 + 시스템 불일치 시 throw.
 */
@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {

  @Mock private MemberRepository memberRepository;

  @InjectMocks private MemberQueryService memberQueryService;

  @Nested
  @DisplayName("getById")
  class GetById {

    @Test
    @DisplayName("Member 있음 → DTO 반환, 필드 매핑 정확")
    void getById_memberExists_returnsDto() {
      // Given
      Member member = mock(Member.class);
      given(member.getId()).willReturn(100L);
      given(member.getName()).willReturn("임근엽");
      given(member.getProfileImageUrl()).willReturn("https://img/k.png");
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      // When
      MemberSummaryDto dto = memberQueryService.getById(1L);

      // Then
      assertThat(dto.memberId()).isEqualTo(100L);
      assertThat(dto.name()).isEqualTo("임근엽");
      assertThat(dto.profileImageUrl()).isEqualTo("https://img/k.png");
    }

    @Test
    @DisplayName("profileImageUrl이 null이어도 정상 매핑 (DTO 필드 null 허용)")
    void getById_nullProfileImage_mapsCorrectly() {
      // Given
      Member member = mock(Member.class);
      given(member.getId()).willReturn(100L);
      given(member.getName()).willReturn("임근엽");
      given(member.getProfileImageUrl()).willReturn(null);
      given(memberRepository.findByUserId(1L)).willReturn(Optional.of(member));

      // When
      MemberSummaryDto dto = memberQueryService.getById(1L);

      // Then
      assertThat(dto.profileImageUrl()).isNull();
    }

    @Test
    @DisplayName("Member 없음 → IllegalStateException (시스템 불일치)")
    void getById_memberNotFound_throws() {
      // Given
      given(memberRepository.findByUserId(any())).willReturn(Optional.empty());

      // When / Then
      assertThatThrownBy(() -> memberQueryService.getById(999L))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Member 없음");
    }
  }
}
