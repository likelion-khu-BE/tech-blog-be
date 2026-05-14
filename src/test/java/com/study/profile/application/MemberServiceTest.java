package com.study.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock; // 이 부분이 핵심입니다!
import static org.mockito.Mockito.when;

import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock private MemberRepository memberRepository;

  @InjectMocks private MemberService memberService;

  @Test
  @DisplayName("존재하는 userId로 조회 시 정상적으로 Member를 반환한다")
  void getMemberToUserId_Success() {
    // given
    Long userId = 1L;
    // 🌟 에러의 원인이었던 new Member() 대신 mock()을 사용합니다!
    Member mockMember = mock(Member.class);
    when(memberRepository.findByUserId(userId)).thenReturn(Optional.of(mockMember));

    // when
    Member result = memberService.getMemberToUserId(userId);

    // then
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(mockMember);
  }

  @Test
  @DisplayName("존재하지 않는 userId로 조회 시 EntityNotFoundException이 발생한다")
  void getMemberToUserId_Fail_NotFound() {
    // given
    when(memberRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

    // when & then
    assertThrows(
        EntityNotFoundException.class,
        () -> {
          memberService.getMemberToUserId(999L);
        });
  }
}
