package com.study.qna.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.qna.application.VoteService;
import com.study.qna.application.dto.request.vote.VoteCreateRequest;
import com.study.qna.application.dto.response.vote.MyVoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Q&A 투표", description = "답변 추천/비추천 투표 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class VoteController {

  private final VoteService voteService;

  @Operation(summary = "투표 생성", description = "답변에 UPVOTE 또는 DOWNVOTE를 등록합니다. 본인 답변 투표 및 중복 투표는 불가합니다.")
  @PostMapping("/answers/{answerId}/votes")
  public ResponseEntity<Void> createVote(
      @PathVariable Long answerId,
      @CurrentUser CustomUserDetails user,
      @Valid @RequestBody VoteCreateRequest request) {
    voteService.createVote(answerId, request, user.userId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(summary = "투표 취소", description = "등록한 투표를 취소합니다. 투표가 없으면 404를 반환합니다.")
  @DeleteMapping("/answers/{answerId}/votes")
  public ResponseEntity<Void> cancelVote(
      @PathVariable Long answerId, @CurrentUser CustomUserDetails user) {
    voteService.cancelVote(answerId, user.userId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "내 투표 조회", description = "해당 답변에 대한 내 투표 타입을 조회합니다. 투표가 없으면 type이 null로 반환됩니다.")
  @GetMapping("/answers/{answerId}/votes/me")
  public ResponseEntity<MyVoteResponse> getMyVote(
      @PathVariable Long answerId, @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(voteService.getMyVote(answerId, user.userId()));
  }
}
