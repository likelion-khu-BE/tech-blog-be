package com.study.profile.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.study.auth.domain.User;
import com.study.auth.infrastructure.UserRepository;
import com.study.profile.application.MemberService;
import com.study.profile.domain.member.Member;
import com.study.profile.domain.member.SessionType;
import com.study.profile.infrastructure.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceIntegrationTest {

  @Autowired private MemberService memberService;

  @Autowired private MemberRepository memberRepository;

  @Autowired private UserRepository userRepository;

  @Test
  @DisplayName("존재하는 userId로 조회 시 정상적으로 Member를 반환한다")
  void getMemberToUserId_Success() {
    // given
    User user = User.create("test@khu.ac.kr", "password");
    userRepository.save(user);

    Member member =
        Member.create(user, "테스터", SessionType.backend, "컴퓨터공학과", null, null, null, null, null);
    memberRepository.save(member);

    // when
    Member result = memberService.getMemberToUserId(user.getId());

    // then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("테스터");
    assertThat(result.getUser().getId()).isEqualTo(user.getId());
  }

  @Test
  @DisplayName("존재하지 않는 userId로 조회 시 EntityNotFoundException이 발생한다")
  void getMemberToUserId_Fail_NotFound() {
    // given
    Long nonExistentUserId = 999L;

    // when & then
    assertThrows(
        EntityNotFoundException.class,
        () -> {
          memberService.getMemberToUserId(nonExistentUserId);
        });
  }
}
