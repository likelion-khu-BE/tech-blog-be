package com.study.qna.application.dto.response.comment;

import com.study.qna.application.dto.response.common.MemberSummaryResponse;
import com.study.qna.domain.Comment;
import java.time.Instant;

/**
 * 댓글 응답 DTO.
 */
public record CommentResponse(
    Long id,
    String content,
    MemberSummaryResponse author,
    Instant createdAt,
    Instant updatedAt) {

  public static CommentResponse of(Comment comment, MemberSummaryResponse author) {
    return new CommentResponse(
        comment.getId(),
        comment.getContent(),
        author,
        comment.getCreatedAt(),
        comment.getUpdatedAt());
  }
}
