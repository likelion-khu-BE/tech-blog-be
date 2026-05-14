package com.study.qna.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.qna.application.VoteService;
import com.study.qna.application.dto.request.vote.VoteCreateRequest;
import com.study.qna.application.dto.response.vote.MyVoteResponse;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class VoteController {

  private final VoteService voteService;

  @PostMapping("/answers/{answerId}/votes")
  public ResponseEntity<Void> createVote(
      @PathVariable Long answerId,
      @CurrentUser CustomUserDetails user,
      @Valid @RequestBody VoteCreateRequest request) {
    voteService.createVote(answerId, request, user.userId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/answers/{answerId}/votes")
  public ResponseEntity<Void> cancelVote(
      @PathVariable Long answerId, @CurrentUser CustomUserDetails user) {
    voteService.cancelVote(answerId, user.userId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/answers/{answerId}/votes/me")
  public ResponseEntity<MyVoteResponse> getMyVote(
      @PathVariable Long answerId, @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(voteService.getMyVote(answerId, user.userId()));
  }
}
