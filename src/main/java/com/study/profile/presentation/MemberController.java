package com.study.profile.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.profile.application.MemberService;
import com.study.profile.application.TeamService;
import com.study.profile.application.dto.MemberDto;
import com.study.profile.application.dto.MemberSummaryDto;
import com.study.profile.application.dto.MemberTechStackUpdateRequest;
import com.study.profile.application.dto.MemberUpdateRequest;
import com.study.profile.application.dto.MemberUpdateResponse;
import com.study.profile.application.dto.TeamDto.MyTeamResponse;
import com.study.profile.application.dto.TechStackItemDto;
import com.study.profile.domain.member.SessionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import com.study.shared.s3.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "멤버 프로필", description = "멤버 조회·수정 API")
@RestController
@RequestMapping("/api/profile/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;
  private final TeamService teamService;

  @Operation(summary = "내 프로필 조회")
  @GetMapping("/me")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMIN', 'MEMBER')")
  public ResponseEntity<MemberDto> getMyProfile(@CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(memberService.getMyProfile(user.userId()));
  }

  @Operation(summary = "내가 속한 팀 목록 조회", description = "로그인한 멤버가 accepted 상태로 참여 중인 팀 목록을 반환합니다.")
  @GetMapping("/me/teams")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMIN', 'MEMBER')")
  public ResponseEntity<List<MyTeamResponse>> getMyTeams(@CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(teamService.getMyTeams(user.userId()));
  }

  @Operation(summary = "내 기술 스택 수정", description = "기존 목록을 전체 교체합니다. 빈 배열 []이면 전체 삭제.")
  @PutMapping("/me/tech-stacks")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMIN', 'MEMBER')")
  public ResponseEntity<List<TechStackItemDto>> updateMyTechStacks(
      @RequestBody List<MemberTechStackUpdateRequest> req, @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(memberService.updateMyTechStacks(user.userId(), req));
  }

  @Operation(
      summary = "프로필 이미지 presigned URL 발급",
      description = "S3에 직접 업로드할 presigned PUT URL을 발급합니다. 반환된 key를 프로필 수정 요청의 profileImageKey에 포함하세요.")
  @PostMapping("/me/profile-image/presigned-url")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMIN', 'MEMBER')")
  public ResponseEntity<PresignedUrlResponse> issueProfileImagePresignedUrl(
      @RequestParam String filename, @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(
        memberService.issueProfileImagePresignedUrl(user.userId(), filename));
  }

  @PatchMapping("/me")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMIN', 'MEMBER')")
  public ResponseEntity<MemberUpdateResponse> updateMyProfile(
      @Valid @RequestBody MemberUpdateRequest req, @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(memberService.updateMyProfile(user.userId(), req));
  }

  @GetMapping
  public ResponseEntity<List<MemberSummaryDto>> getMembers(
      @RequestParam(required = false) Integer generationId,
      @RequestParam(required = false) SessionType sessionType) {
    return ResponseEntity.ok(memberService.getMembers(generationId, sessionType));
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<MemberDto> getMemberById(@PathVariable Long memberId) {
    return ResponseEntity.ok(memberService.getMemberById(memberId));
  }

  @Operation(summary = "멤버 기술 스택 조회", description = "특정 멤버가 보유한 기술 스택 목록을 반환합니다.")
  @GetMapping("/{memberId}/tech-stacks")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMIN', 'MEMBER')")
  public ResponseEntity<List<TechStackItemDto>> getMemberTechStacks(@PathVariable Long memberId) {
    return ResponseEntity.ok(memberService.getMemberTechStacks(memberId));
  }
}
