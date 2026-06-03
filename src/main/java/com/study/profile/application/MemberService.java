package com.study.profile.application;

import com.study.auth.domain.User;
import com.study.auth.infrastructure.UserRepository;
import com.study.profile.application.dto.MemberCreateRequest;
import com.study.profile.application.dto.MemberDto;
import com.study.profile.application.dto.MemberGenerationDto;
import com.study.profile.application.dto.MemberSummaryDto;
import com.study.profile.application.dto.MemberTechStackUpdateRequest;
import com.study.profile.application.dto.MemberUpdateRequest;
import com.study.profile.application.dto.MemberUpdateResponse;
import com.study.profile.application.dto.TechStackItemDto;
import com.study.profile.domain.exception.MemberNotFoundException;
import com.study.profile.domain.member.Member;
import com.study.profile.domain.member.SessionType;
import com.study.profile.domain.techstack.MemberTechStack;
import com.study.profile.domain.techstack.TechStack;
import com.study.profile.infrastructure.MemberGenerationRepository;
import com.study.profile.infrastructure.MemberRepository;
import com.study.profile.infrastructure.MemberTechStackRepository;
import com.study.profile.infrastructure.TechStackRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class MemberService {

  @PersistenceContext private EntityManager entityManager;

  private final MemberRepository memberRepository;
  private final MemberGenerationRepository memberGenerationRepository;
  private final UserRepository userRepository;
  private final TechStackRepository techStackRepository;
  private final MemberTechStackRepository memberTechStackRepository;

  public MemberService(
      MemberRepository memberRepository,
      MemberGenerationRepository memberGenerationRepository,
      UserRepository userRepository,
      TechStackRepository techStackRepository,
      MemberTechStackRepository memberTechStackRepository) {
    this.memberRepository = memberRepository;
    this.memberGenerationRepository = memberGenerationRepository;
    this.userRepository = userRepository;
    this.techStackRepository = techStackRepository;
    this.memberTechStackRepository = memberTechStackRepository;
  }

  public Member getMemberToUserId(Long userId) {
    return memberRepository
        .findByUserId(userId)
        .orElseThrow(
            () -> new EntityNotFoundException("해당 유저의 멤버 프로필을 찾을 수 없습니다. userId: " + userId));
  }

  /** 내부용: 이벤트 리스너에서 Member 엔티티 직접 필요 시 사용 */
  @Transactional
  public Member createMemberEntity(Long userId, MemberCreateRequest req) {
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
    return memberRepository.save(member);
  }

  /** 내부용: User 승인 후 프로필 최초 생성 */
  @Transactional
  public MemberDto createMember(Long userId, MemberCreateRequest req) {
    return MemberDto.from(createMemberEntity(userId, req), List.of());
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

  public List<TechStackItemDto> getMemberTechStacks(Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(memberId));
    return member.getTechStacks().stream().map(TechStackItemDto::from).toList();
  }

  public List<MemberSummaryDto> getMembers(Integer generationId, SessionType sessionType) {
    List<Member> members;
    if (generationId != null) {
      List<Long> memberIds = memberGenerationRepository.findMemberIdsByGenerationId(generationId);
      if (memberIds.isEmpty()) {
        return List.of();
      }
      members =
          sessionType != null
              ? memberRepository.findAllByIdInAndSessionType(memberIds, sessionType)
              : memberRepository.findAllByIdIn(memberIds);
    } else {
      members =
          sessionType != null
              ? memberRepository.findAllBySessionType(sessionType)
              : memberRepository.findAllSorted();
    }
    return members.stream().map(MemberSummaryDto::from).toList();
  }

  @Transactional
  public List<TechStackItemDto> updateMyTechStacks(
      Long userId, List<MemberTechStackUpdateRequest> req) {
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

    member.getTechStacks().clear();
    entityManager.flush(); // DELETE 먼저 실행 — unique constraint 위반 방지

    for (MemberTechStackUpdateRequest item : req) {
      TechStack techStack =
          techStackRepository
              .findById(item.techStackId())
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.NOT_FOUND, "기술 스택을 찾을 수 없습니다. id: " + item.techStackId()));
      member.getTechStacks().add(MemberTechStack.create(member, techStack, item.proficiency()));
    }

    return member.getTechStacks().stream().map(TechStackItemDto::from).toList();
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
