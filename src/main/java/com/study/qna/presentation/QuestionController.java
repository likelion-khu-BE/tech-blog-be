package com.study.qna.presentation;

import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import com.study.qna.application.QuestionService;
import com.study.qna.application.dto.request.question.QuestionCreateRequest;
import com.study.qna.application.dto.request.question.QuestionSearchCondition;
import com.study.qna.application.dto.request.question.QuestionStatusUpdateRequest;
import com.study.qna.application.dto.request.question.QuestionUpdateRequest;
import com.study.qna.application.dto.response.question.QuestionDetailResponse;
import com.study.qna.application.dto.response.question.QuestionSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Q&A 질문", description = "질문 CRUD 및 상태 관리 API")
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

  private final QuestionService questionService;

  @Operation(summary = "질문 목록 조회", description = "키워드·상태·태그·기수 필터와 정렬·페이징으로 질문 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<List<QuestionSummaryResponse>> getQuestions(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long tagId,
      @RequestParam(required = false) Integer generation,
      @RequestParam(defaultValue = "latest") String sort,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    QuestionSearchCondition condition =
        new QuestionSearchCondition(keyword, status, tagId, generation, sort, page, size);
    return ResponseEntity.ok(questionService.getQuestions(condition));
  }

  @Operation(summary = "질문 상세 조회", description = "질문 ID로 상세 정보를 조회합니다. 조회 시 조회수가 1 증가합니다.")
  @GetMapping("/{questionId}")
  public ResponseEntity<QuestionDetailResponse> getQuestion(@PathVariable Long questionId) {
    return ResponseEntity.ok(questionService.getQuestion(questionId));
  }

  @Operation(summary = "질문 등록", description = "새 질문을 등록합니다. 태그 ID 목록을 함께 전달할 수 있습니다.")
  @PostMapping
  public ResponseEntity<QuestionDetailResponse> createQuestion(
      @CurrentUser CustomUserDetails user, @Valid @RequestBody QuestionCreateRequest request) {
    QuestionDetailResponse result = questionService.createQuestion(request, user.userId());
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @Operation(summary = "질문 수정", description = "작성자만 제목·본문·태그를 수정할 수 있습니다. null 필드는 변경하지 않습니다.")
  @PatchMapping("/{questionId}")
  public ResponseEntity<QuestionDetailResponse> updateQuestion(
      @PathVariable Long questionId,
      @CurrentUser CustomUserDetails user,
      @Valid @RequestBody QuestionUpdateRequest request) {
    return ResponseEntity.ok(questionService.updateQuestion(questionId, request, user.userId()));
  }

  @Operation(summary = "질문 상태 변경", description = "작성자만 상태를 변경할 수 있습니다. OPEN → RESOLVED 전이만 허용됩니다.")
  @PatchMapping("/{questionId}/status")
  public ResponseEntity<QuestionDetailResponse> updateQuestionStatus(
      @PathVariable Long questionId,
      @CurrentUser CustomUserDetails user,
      @Valid @RequestBody QuestionStatusUpdateRequest request) {
    return ResponseEntity.ok(
        questionService.updateQuestionStatus(questionId, request, user.userId()));
  }

  @Operation(summary = "질문 삭제", description = "작성자만 질문을 삭제할 수 있습니다.")
  @DeleteMapping("/{questionId}")
  public ResponseEntity<Void> deleteQuestion(
      @PathVariable Long questionId, @CurrentUser CustomUserDetails user) {
    questionService.deleteQuestion(questionId, user.userId());
    return ResponseEntity.noContent().build();
  }
}
