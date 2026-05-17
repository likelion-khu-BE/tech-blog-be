package com.study.profile.application;

import com.study.profile.application.dto.TeamDto.GenerationSummary;
import com.study.profile.application.dto.TeamDto.TeamCreateRequest;
import com.study.profile.application.dto.TeamDto.TeamCreateResponse;
import com.study.profile.application.dto.TeamDto.TeamDetailResponse;
import com.study.profile.application.dto.TeamDto.TeamListResponse;
import com.study.profile.application.dto.TeamDto.TeamMemberSummary;
import com.study.profile.application.dto.TeamDto.InviteCodeResponse;
import com.study.profile.application.dto.TeamDto.TeamJoinRequest;
import com.study.profile.application.dto.TeamDto.TeamJoinResponse;
import com.study.profile.application.dto.TeamDto.TeamLeadTransferRequest;
import com.study.profile.application.dto.TeamDto.TeamLeadTransferResponse;
import com.study.profile.application.dto.TeamDto.TeamMemberRoleUpdateRequest;
import com.study.profile.application.dto.TeamDto.TeamMemberRoleUpdateResponse;
import com.study.profile.application.dto.TeamDto.MyTeamResponse;
import com.study.profile.application.dto.TeamDto.TeamUpdateRequest;
import com.study.profile.application.dto.TeamDto.TeamUpdateResponse;
import com.study.profile.application.dto.TeamDto.TechStackSummary;
import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.member.Member;
import com.study.profile.domain.team.TeamImage;
import com.study.profile.domain.team.TeamMember;
import com.study.profile.domain.team.TeamMemberStatus;
import com.study.profile.domain.team.TeamProfile;
import com.study.profile.domain.techstack.TeamTechStack;
import com.study.profile.domain.techstack.TechStack;
import com.study.profile.infrastructure.GenerationRepository;
import com.study.profile.infrastructure.MemberRepository;
import com.study.profile.infrastructure.TeamMemberRepository;
import com.study.profile.infrastructure.TeamRepository;
import com.study.profile.infrastructure.TechStackRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TeamService {

  @PersistenceContext private EntityManager entityManager;

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

  @Transactional
  public TeamUpdateResponse updateTeam(Long teamId, TeamUpdateRequest req, Long userId) {
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

    TeamProfile team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));

    if (!teamMemberRepository.existsByTeamIdAndMemberIdAndIsLeadTrue(teamId, member.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "팀장만 수정할 수 있습니다.");
    }

    Generation generation = team.getGeneration();
    if (req.generationNumber() != null) {
      generation =
          generationRepository
              .findById(req.generationNumber())
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "기수를 찾을 수 없습니다."));
    }

    team.update(
        generation,
        req.name() != null ? req.name() : team.getName(),
        req.description() != null ? req.description() : team.getDescription(),
        req.projectUrl() != null ? req.projectUrl() : team.getProjectUrl(),
        req.githubUrl() != null ? req.githubUrl() : team.getGithubUrl());

    team.updateImages(req.imageUrls());

    if (req.techStackIds() != null) {
      List<TechStack> techStacks =
          req.techStackIds().isEmpty()
              ? List.of()
              : techStackRepository.findAllById(req.techStackIds());
      team.clearTechStacks();
      entityManager.flush(); // DELETE 먼저 실행 후 INSERT — unique constraint 위반 방지
      team.addTechStacks(techStacks);
    }

    teamRepository.saveAndFlush(team);

    return new TeamUpdateResponse(team.getId(), team.getUpdatedAt());
  }

  @Transactional
  public TeamJoinResponse joinTeam(TeamJoinRequest req, Long userId) {
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

    TeamProfile team =
        teamRepository
            .findByInviteCode(req.inviteCode())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유효하지 않은 초대 코드입니다."));

    if (team.getInviteCodeExpiresAt().isBefore(Instant.now())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "만료된 초대 코드입니다.");
    }

    Optional<TeamMember> existing = teamMemberRepository.findByTeamIdAndMemberId(team.getId(), member.getId());

    if (existing.isPresent()) {
      TeamMember teamMember = existing.get();
      if (teamMember.getStatus() == TeamMemberStatus.accepted) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 팀입니다.");
      }
      teamMember.rejoin();
      return new TeamJoinResponse(team.getId(), team.getName());
    }

    teamMemberRepository.save(TeamMember.createByInviteCode(team, member));

    return new TeamJoinResponse(team.getId(), team.getName());
  }

  @Transactional
  public TeamMemberRoleUpdateResponse updateMemberRoles(
      Long teamId, Long targetMemberId, TeamMemberRoleUpdateRequest req, Long userId) {
    Member requestMember =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

    teamRepository
        .findById(teamId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));

    boolean isLead = teamMemberRepository.existsByTeamIdAndMemberIdAndIsLeadTrue(teamId, requestMember.getId());
    boolean isSelf = requestMember.getId().equals(targetMemberId);

    if (!isLead && !isSelf) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "팀장 또는 본인만 역할을 수정할 수 있습니다.");
    }

    TeamMember target =
        teamMemberRepository
            .findByTeamIdAndMemberIdAndStatus(teamId, targetMemberId, TeamMemberStatus.accepted)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 팀에 존재하지 않는 멤버입니다."));

    target.updateRoles(req.roles());

    List<String> updatedRoles = target.getRoles().stream().map(r -> r.getRole().name()).toList();
    return new TeamMemberRoleUpdateResponse(targetMemberId, updatedRoles);
  }

  @Transactional
  public void leaveTeam(Long teamId, Long userId) {
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

    teamRepository
        .findById(teamId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));

    TeamMember teamMember =
        teamMemberRepository
            .findByTeamIdAndMemberIdAndStatus(teamId, member.getId(), TeamMemberStatus.accepted)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 팀에 속해 있지 않습니다."));

    if (teamMember.isLead()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "팀장은 탈퇴할 수 없습니다. 팀을 해산하려면 팀 삭제를 이용해주세요.");
    }

    teamMember.leave();
  }

  @Transactional
  public void kickMember(Long teamId, Long targetMemberId, Long userId) {
    Member requestMember =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

    teamRepository
        .findById(teamId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));

    teamMemberRepository
        .findByTeamIdAndMemberIdAndIsLeadTrue(teamId, requestMember.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "팀장만 강퇴할 수 있습니다."));

    if (requestMember.getId().equals(targetMemberId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "팀장 본인은 강퇴할 수 없습니다.");
    }

    TeamMember target =
        teamMemberRepository
            .findByTeamIdAndMemberIdAndStatus(teamId, targetMemberId, TeamMemberStatus.accepted)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 팀에 존재하지 않는 멤버입니다."));

    target.kick();
  }

  @Transactional
  public TeamLeadTransferResponse transferLead(Long teamId, TeamLeadTransferRequest req, Long userId) {
    Member currentLeaderMember =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

    teamRepository
        .findById(teamId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));

    TeamMember currentLeader =
        teamMemberRepository
            .findByTeamIdAndMemberIdAndIsLeadTrue(teamId, currentLeaderMember.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "팀장만 양도할 수 있습니다."));

    TeamMember newLeader =
        teamMemberRepository
            .findByTeamIdAndMemberIdAndStatus(teamId, req.memberId(), TeamMemberStatus.accepted)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "대상 멤버가 해당 팀의 accepted 상태가 아닙니다."));

    currentLeader.updateLead(false);
    newLeader.updateLead(true);

    return new TeamLeadTransferResponse(teamId, req.memberId());
  }

  @Transactional
  public InviteCodeResponse regenerateInviteCode(Long teamId, Long userId) {
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

    TeamProfile team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));

    if (!teamMemberRepository.existsByTeamIdAndMemberIdAndIsLeadTrue(teamId, member.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "팀장만 초대 코드를 재생성할 수 있습니다.");
    }

    String newCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    team.regenerateInviteCode(newCode);

    return new InviteCodeResponse(team.getInviteCode(), team.getInviteCodeExpiresAt());
  }

  @Transactional
  public void deleteTeam(Long teamId, Long userId) {
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

    TeamProfile team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));

    if (!teamMemberRepository.existsByTeamIdAndMemberIdAndIsLeadTrue(teamId, member.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "팀장만 삭제할 수 있습니다.");
    }

    teamMemberRepository.deleteByTeamId(teamId);
    teamRepository.delete(team);
  }

  @Transactional(readOnly = true)
  public List<TeamListResponse> getTeams(Integer generationNumber) {
    List<TeamProfile> teams =
        generationNumber != null
            ? teamRepository.findByGenerationNumber(generationNumber)
            : teamRepository.findAll();

    return teams.stream().map(this::toListResponse).toList();
  }

  @Transactional(readOnly = true)
  public TeamDetailResponse getTeamDetail(Long teamId, Long userId) {
    TeamProfile team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));

    List<TeamMember> acceptedMembers =
        teamMemberRepository.findByTeamIdAndStatus(teamId, TeamMemberStatus.accepted);

    boolean isTeamMember =
        userId != null
            && acceptedMembers.stream()
                .anyMatch(tm -> tm.getMember().getUser().getId().equals(userId));

    GenerationSummary generation =
        team.getGeneration() != null
            ? new GenerationSummary(team.getGeneration().getNumber())
            : null;

    List<TechStackSummary> techStacks =
        team.getTechStacks().stream()
            .map(TeamTechStack::getTechStack)
            .map(
                ts ->
                    new TechStackSummary(
                        ts.getId(), ts.getName(), ts.getCategory().name(), ts.getLogoUrl()))
            .toList();

    List<String> imageUrls = team.getImages().stream().map(TeamImage::getImageUrl).toList();

    List<TeamMemberSummary> members =
        acceptedMembers.stream()
            .map(
                tm ->
                    new TeamMemberSummary(
                        tm.getMember().getId(),
                        tm.getMember().getName(),
                        tm.getMember().getSessionType().name(),
                        tm.getMember().getProfileImageUrl(),
                        tm.isLead(),
                        tm.getRoles().stream().map(r -> r.getRole().name()).toList()))
            .toList();

    return new TeamDetailResponse(
        team.getId(),
        team.getName(),
        team.getDescription(),
        team.getProjectUrl(),
        team.getGithubUrl(),
        generation,
        techStacks,
        imageUrls,
        members,
        isTeamMember ? team.getInviteCode() : null,
        isTeamMember ? team.getInviteCodeExpiresAt() : null,
        team.getUpdatedAt());
  }

  @Transactional(readOnly = true)
  public List<MyTeamResponse> getMyTeams(Long userId) {
    Member member =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

    List<TeamMember> myTeamMembers =
        teamMemberRepository.findByMemberIdAndStatus(member.getId(), TeamMemberStatus.accepted);

    return myTeamMembers.stream()
        .map(
            tm -> {
              TeamProfile team = tm.getTeam();
              GenerationSummary generation =
                  team.getGeneration() != null
                      ? new GenerationSummary(team.getGeneration().getNumber())
                      : null;
              List<TechStackSummary> techStacks =
                  team.getTechStacks().stream()
                      .map(TeamTechStack::getTechStack)
                      .map(
                          ts ->
                              new TechStackSummary(
                                  ts.getId(), ts.getName(), ts.getCategory().name(), ts.getLogoUrl()))
                      .toList();
              List<String> roles = tm.getRoles().stream().map(r -> r.getRole().name()).toList();
              String thumbUrl =
                  team.getImages().isEmpty() ? null : team.getImages().get(0).getImageUrl();
              return new MyTeamResponse(
                  team.getId(),
                  team.getName(),
                  team.getDescription(),
                  generation,
                  techStacks,
                  tm.isLead(),
                  roles,
                  thumbUrl);
            })
        .toList();
  }

  private TeamListResponse toListResponse(TeamProfile team) {
    GenerationSummary generation =
        team.getGeneration() != null
            ? new GenerationSummary(team.getGeneration().getNumber())
            : null;

    List<TechStackSummary> techStacks =
        team.getTechStacks().stream()
            .map(TeamTechStack::getTechStack)
            .map(
                ts ->
                    new TechStackSummary(
                        ts.getId(), ts.getName(), ts.getCategory().name(), ts.getLogoUrl()))
            .toList();

    String thumbUrl = team.getImages().isEmpty() ? null : team.getImages().get(0).getImageUrl();

    int memberCount = team.getMembers().size();

    return new TeamListResponse(
        team.getId(),
        team.getName(),
        team.getDescription(),
        generation,
        techStacks,
        memberCount,
        thumbUrl);
  }
}
