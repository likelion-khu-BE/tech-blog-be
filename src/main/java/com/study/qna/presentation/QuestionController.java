package com.study.qna.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.qna.application.QuestionService;
import com.study.qna.application.dto.request.question.QuestionCreateRequest;
import com.study.qna.application.dto.response.question.QuestionDetailResponse;
import com.study.qna.application.dto.response.question.QuestionSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

  private final QuestionService questionService;

  @GetMapping
  public ResponseEntity<List<QuestionSummaryResponse>> getQuestions() {
    return ResponseEntity.ok(questionService.getQuestions());
  }

  @GetMapping("/{questionId}")
  public ResponseEntity<QuestionDetailResponse> getQuestion(@PathVariable Long questionId) {
    return ResponseEntity.ok(questionService.getQuestion(questionId));
  }

  @PostMapping
  public ResponseEntity<QuestionDetailResponse> createQuestion(
      @CurrentUser CustomUserDetails user, @Valid @RequestBody QuestionCreateRequest request) {
    QuestionDetailResponse result = questionService.createQuestion(request, user.userId());
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @DeleteMapping("/{questionId}")
  public ResponseEntity<Void> deleteQuestion(
      @PathVariable Long questionId, @CurrentUser CustomUserDetails user) {
    questionService.deleteQuestion(questionId, user.userId());
    return ResponseEntity.noContent().build();
  }
}
