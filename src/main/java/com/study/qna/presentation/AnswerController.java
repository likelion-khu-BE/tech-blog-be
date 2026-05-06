package com.study.qna.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.qna.application.AnswerService;
import com.study.qna.application.dto.request.answer.AnswerCreateRequest;
import com.study.qna.application.dto.request.answer.AnswerUpdateRequest;
import com.study.qna.application.dto.response.answer.AnswerDetailResponse;
import com.study.qna.application.dto.response.answer.AnswerListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AnswerController {

  private final AnswerService answerService;

  @GetMapping("/questions/{questionId}/answers")
  public ResponseEntity<AnswerListResponse> getAnswers(@PathVariable Long questionId) {
    return ResponseEntity.ok(answerService.getAnswers(questionId));
  }

  @PostMapping("/questions/{questionId}/answers")
  public ResponseEntity<AnswerDetailResponse> createAnswer(
      @PathVariable Long questionId,
      @CurrentUser CustomUserDetails user,
      @Valid @RequestBody AnswerCreateRequest request) {
    AnswerDetailResponse result = answerService.createAnswer(questionId, request, user.userId());
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @PatchMapping("/answers/{answerId}")
  public ResponseEntity<AnswerDetailResponse> updateAnswer(
      @PathVariable Long answerId,
      @CurrentUser CustomUserDetails user,
      @Valid @RequestBody AnswerUpdateRequest request) {
    return ResponseEntity.ok(answerService.updateAnswer(answerId, request, user.userId()));
  }

  @PostMapping("/answers/{answerId}/accept")
  public ResponseEntity<AnswerDetailResponse> acceptAnswer(
      @PathVariable Long answerId, @CurrentUser CustomUserDetails user) {
    return ResponseEntity.ok(answerService.acceptAnswer(answerId, user.userId()));
  }

  @DeleteMapping("/answers/{answerId}")
  public ResponseEntity<Void> deleteAnswer(
      @PathVariable Long answerId, @CurrentUser CustomUserDetails user) {
    answerService.deleteAnswer(answerId, user.userId());
    return ResponseEntity.noContent().build();
  }
}