package com.study.profile.application;

import com.study.auth.domain.User;
import com.study.auth.infrastructure.UserRepository;
import com.study.profile.application.dto.MemberCreateRequest;
import com.study.profile.application.dto.MemberDto;
import com.study.profile.application.dto.MemberGenerationDto;
import com.study.profile.application.dto.MemberSummaryDto;
import com.study.profile.application.dto.MemberUpdateRequest;
import com.study.profile.application.dto.MemberUpdateResponse;
import com.study.profile.domain.member.Member;
import com.study.profile.domain.member.SessionType;
import com.study.profile.infrastructure.MemberGenerationRepository;
import com.study.profile.infrastructure.MemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberGenerationRepository memberGenerationRepository;
  private final UserRepository userRepository;

  public MemberService(
      MemberRepository memberRepository,
      MemberGenerationRepository memberGenerationRepository,
      UserRepository userRepository) {
    this.memberRepository = memberRepository;
    this.memberGenerationRepository = memberGenerationRepository;
    this.userRepository = userRepository;
  }

  /** 내부용: User 승인 후 프로필 최초 생성 */
  @Transactional
  public MemberDto createMember(Long userId, MemberCreateRequest req) {
    if (memberRepository.existsByUserId(userId)) {
      throw new IllegalStateException("이미 프로필이 존재합니다.");
    }
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    Member member =
        Member.create(
            user,
            req.name(),
            req.sessionType(),
            req.department(),
            req.profileImageUrl(),
            req.githubUrl(),
            req.displayedEmail(),
            req.intro(),
            req.linksJson());
    return MemberDto.from(memberRepository.save(member), List.of());
  }

  public MemberDto getMyProfile(Long userId) {
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));
    List<MemberGenerationDto> generations =
        memberGenerationRepository.findByMemberId(member.getId()).stream()
            .map(MemberGenerationDto::from)
            .toList();
    return MemberDto.from(member, generations);
  }

  public MemberDto getMemberById(Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));
    List<MemberGenerationDto> generations =
        memberGenerationRepository.findByMemberId(memberId).stream()
            .map(MemberGenerationDto::from)
            .toList();
    return MemberDto.from(member, generations);
  }

  public List<MemberSummaryDto> getMembers(Long generationId, SessionType sessionType) {
    List<Member> members;
    if (generationId != null) {
      List<Long> memberIds = memberGenerationRepository.findMemberIdsByGenerationId(generationId);
      members = memberRepository.findAllByIdInFiltered(memberIds, sessionType);
    } else {
      members = memberRepository.findAllFiltered(sessionType);
    }
    return members.stream().map(MemberSummaryDto::from).toList();
  }

  @Transactional
  public MemberUpdateResponse updateMyProfile(Long userId, MemberUpdateRequest req) {
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));
    member.update(
        req.name(),
        req.sessionType(),
        req.department(),
        req.profileImageUrl(),
        req.githubUrl(),
        req.displayedEmail(),
        req.intro(),
        req.linksJson());
    return MemberUpdateResponse.from(member);
  }
}
