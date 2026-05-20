package com.study.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock; // 이 부분이 핵심입니다!
import static org.mockito.Mockito.when;

import com.study.profile.application.dto.TechStackItemDto;
import com.study.profile.domain.exception.MemberNotFoundException;
import com.study.profile.domain.member.Member;
import com.study.profile.domain.techstack.MemberTechStack;
import com.study.profile.domain.techstack.TechStack;
import com.study.profile.domain.techstack.TechStackCategory;
import com.study.profile.infrastructure.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

  @Test
  @DisplayName("멤버 기술 스택 조회 성공 시 보유 목록을 TechStackItemDto로 반환한다")
  void getMemberTechStacks_Success() {
    // given
    Member member = mock(Member.class);
    TechStack ts = TechStack.create("Java", TechStackCategory.language, "https://logo");
    ReflectionTestUtils.setField(ts, "id", 1L);
    MemberTechStack mts = mock(MemberTechStack.class);
    when(mts.getTechStack()).thenReturn(ts);
    when(mts.getProficiency()).thenReturn(4);
    when(member.getTechStacks()).thenReturn(List.of(mts));
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

    // when
    List<TechStackItemDto> result = memberService.getMemberTechStacks(1L);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).techStackId()).isEqualTo(1L);
    assertThat(result.get(0).name()).isEqualTo("Java");
    assertThat(result.get(0).proficiency()).isEqualTo(4);
  }

  @Test
  @DisplayName("존재하지 않는 memberId로 기술 스택 조회 시 MemberNotFoundException이 발생한다")
  void getMemberTechStacks_Fail_NotFound() {
    // given
    when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

    // when & then
    assertThrows(MemberNotFoundException.class, () -> memberService.getMemberTechStacks(999L));
  }
}
