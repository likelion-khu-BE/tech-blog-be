package com.study.profile.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.profile.application.TeamService;
import com.study.profile.application.dto.TeamDto.TeamCreateRequest;
import com.study.profile.application.dto.TeamDto.TeamCreateResponse;
import com.study.profile.application.dto.TeamDto.TeamDetailResponse;
import com.study.profile.application.dto.TeamDto.TeamListResponse;
import com.study.profile.application.dto.TeamDto.InviteCodeResponse;
import com.study.profile.application.dto.TeamDto.TeamJoinRequest;
import com.study.profile.application.dto.TeamDto.TeamJoinResponse;
import com.study.profile.application.dto.TeamDto.TeamUpdateRequest;
import com.study.profile.application.dto.TeamDto.TeamUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "팀 프로필", description = "팀 생성·조회·수정 API")
@RestController
@RequestMapping("/api/profile/teams")
@RequiredArgsConstructor
public class TeamController {

  private final TeamService teamService;

  @Operation(summary = "팀 목록 조회", description = "기수 번호로 필터링하여 팀 목록을 조회합니다. 기수 미입력 시 전체 조회.")
  @GetMapping
  public ResponseEntity<List<TeamListResponse>> getTeams(
      @RequestParam(required = false) Integer generationNumber) {
    return ResponseEntity.ok(teamService.getTeams(generationNumber));
  }

  @Operation(summary = "팀 상세 조회", description = "팀 ID로 팀 상세 정보를 조회합니다. 비로그인 상태로도 조회 가능합니다.")
  @GetMapping("/{teamId}")
  public ResponseEntity<TeamDetailResponse> getTeamDetail(
      @PathVariable Long teamId, @CurrentUser CustomUserDetails user) {
    Long userId = user != null ? user.userId() : null;
    return ResponseEntity.ok(teamService.getTeamDetail(teamId, userId));
  }

  @Operation(summary = "팀 생성", description = "새 팀을 생성합니다. 생성한 유저가 팀장으로 자동 등록되며 초대코드가 발급됩니다.")
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<TeamCreateResponse> createTeam(
      @RequestBody TeamCreateRequest req, @CurrentUser CustomUserDetails user) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(teamService.createTeam(req, user.userId()));
  }

  @Operation(summary = "초대 코드로 팀 가입", description = "초대 코드를 입력해 팀에 가입합니다. 가입 즉시 status = accepted로 확정됩니다.")
  @PostMapping("/join")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<TeamJoinResponse> joinTeam(
      @RequestBody TeamJoinRequest req, @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(teamService.joinTeam(req, user.userId()));
  }

  @Operation(summary = "초대 코드 재생성", description = "팀장만 초대 코드를 재생성할 수 있습니다. 기존 코드는 즉시 무효화되고 만료 시각은 현재 시각 + 3일로 갱신됩니다.")
  @PostMapping("/{teamId}/invite-code/regenerate")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<InviteCodeResponse> regenerateInviteCode(
      @PathVariable Long teamId, @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(teamService.regenerateInviteCode(teamId, user.userId()));
  }

  @Operation(summary = "팀 삭제", description = "팀장만 팀을 삭제할 수 있습니다. 팀원·이미지·기술스택 모두 함께 삭제됩니다.")
  @DeleteMapping("/{teamId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<Void> deleteTeam(
      @PathVariable Long teamId, @CurrentUser CustomUserDetails user) {
    teamService.deleteTeam(teamId, user.userId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "팀 정보 수정", description = "팀장만 팀 이름·설명·기술스택·이미지 등을 수정할 수 있습니다. null 필드는 변경하지 않습니다.")
  @PatchMapping("/{teamId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<TeamUpdateResponse> updateTeam(
      @PathVariable Long teamId,
      @RequestBody TeamUpdateRequest req,
      @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(teamService.updateTeam(teamId, req, user.userId()));
  }
}
