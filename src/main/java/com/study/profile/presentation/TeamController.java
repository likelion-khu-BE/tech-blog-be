package com.study.profile.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.profile.application.TeamService;
import com.study.profile.application.dto.TeamDto.TeamCreateRequest;
import com.study.profile.application.dto.TeamDto.TeamCreateResponse;
import com.study.profile.application.dto.TeamDto.TeamDetailResponse;
import com.study.profile.application.dto.TeamDto.TeamListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile/teams")
@RequiredArgsConstructor
public class TeamController {

  private final TeamService teamService;

  @GetMapping
  public ResponseEntity<List<TeamListResponse>> getTeams(
      @RequestParam(required = false) Integer generationNumber) {
    return ResponseEntity.ok(teamService.getTeams(generationNumber));
  }

  @GetMapping("/{teamId}")
  public ResponseEntity<TeamDetailResponse> getTeamDetail(
      @PathVariable Long teamId, @CurrentUser CustomUserDetails user) {
    Long userId = user != null ? user.userId() : null;
    return ResponseEntity.ok(teamService.getTeamDetail(teamId, userId));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<TeamCreateResponse> createTeam(
      @RequestBody TeamCreateRequest req, @CurrentUser CustomUserDetails user) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(teamService.createTeam(req, user.userId()));
  }
}
