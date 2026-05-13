package com.study.profile.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.profile.application.MemberService;
import com.study.profile.application.dto.MemberDto;
import com.study.profile.application.dto.MemberSummaryDto;
import com.study.profile.application.dto.MemberUpdateRequest;
import com.study.profile.application.dto.MemberUpdateResponse;
import com.study.profile.domain.member.SessionType;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile/members")
public class MemberController {

  private final MemberService memberService;

  public MemberController(MemberService memberService) {
    this.memberService = memberService;
  }

  @GetMapping("/me")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
  public ResponseEntity<MemberDto> getMyProfile(@CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(memberService.getMyProfile(user.userId()));
  }

  @PatchMapping("/me")
  @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
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
}
