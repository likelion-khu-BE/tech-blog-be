package com.study.sessionboard.application.event.dto;

import com.study.sessionboard.domain.event.EventPostComment;
import java.time.OffsetDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        AuthorDto author,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<CommentResponse> replies) {

    public static CommentResponse of(EventPostComment comment, List<CommentResponse> replies) {
        return new CommentResponse(
                comment.getId(),
                AuthorDto.from(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                replies);
    }
}