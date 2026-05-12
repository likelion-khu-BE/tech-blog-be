package com.study.profile.application;

import com.study.profile.application.dto.TeamDto.TeamCreateRequest;
import com.study.profile.application.dto.TeamDto.TeamCreateResponse;
import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.member.Member;
import com.study.profile.domain.team.TeamMember;
import com.study.profile.domain.team.TeamProfile;
import com.study.profile.domain.techstack.TechStack;
import com.study.profile.infrastructure.GenerationRepository;
import com.study.profile.infrastructure.MemberRepository;
import com.study.profile.infrastructure.TeamMemberRepository;
import com.study.profile.infrastructure.TeamRepository;
import com.study.profile.infrastructure.TechStackRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TeamService {

  private final TeamRepository teamRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final MemberRepository memberRepository;
  private final GenerationRepository generationRepository;
  private final TechStackRepository techStackRepository;

  @Transactional
  public TeamCreateResponse createTeam(TeamCreateRequest req, Long userId) {
    // 1. 요청한 유저의 Member 조회
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

    // 2. generationNumber가 있으면 Generation 조회, 없으면 null
    Generation generation = null;
    if (req.generationNumber() != null) {
      generation =
          generationRepository
              .findById(req.generationNumber())
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "기수를 찾을 수 없습니다."));
    }

    // 3. 초대 코드 생성 (UUID 앞 8자리, 대문자)
    String inviteCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    Instant inviteCodeExpiresAt = Instant.now().plus(3, ChronoUnit.DAYS);

    // 4. TeamProfile 생성
    TeamProfile team =
        TeamProfile.create(
            generation,
            req.name(),
            req.description(),
            req.projectUrl(),
            req.githubUrl(),
            inviteCode,
            inviteCodeExpiresAt);

    // 5. 이미지 추가
    team.updateImages(req.imageUrls());

    // 6. 기술 스택 추가
    if (req.techStackIds() != null && !req.techStackIds().isEmpty()) {
      List<TechStack> techStacks = techStackRepository.findAllById(req.techStackIds());
      team.updateTechStacks(techStacks);
    }

    teamRepository.save(team);

    // 7. 팀 생성자를 팀장으로 등록 (isLead=true, status=accepted)
    TeamMember teamMember = TeamMember.create(team, member, null, true);
    teamMemberRepository.save(teamMember);

    return new TeamCreateResponse(
        team.getId(), team.getInviteCode(), team.getInviteCodeExpiresAt());
  }
}
