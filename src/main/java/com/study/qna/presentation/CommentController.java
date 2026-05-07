package com.study.qna.presentation;

import com.study.qna.application.dto.request.comment.CommentCreateRequest;
import com.study.qna.application.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.study.qna.application.dto.response.comment.CommentResponse;
import org.springframework.http.HttpStatus;
import java.util.List;
import com.study.auth.infrastructure.security.CurrentUser;
import com.study.auth.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;


@RestController("qnaCommentController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/answers/{answerId}/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long answerId) {
        return ResponseEntity.ok(commentService.getComments(answerId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long answerId,
            @RequestBody @Valid CommentCreateRequest request,
            @CurrentUser CustomUserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(answerId, request, user.userId()));
    }
}
