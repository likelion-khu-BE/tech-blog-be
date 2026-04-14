package com.study.qna.dto.response.comment;

import com.study.common.entity.qna.Comment;
import com.study.qna.dto.response.common.MemberSummaryResponse;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;
    private MemberSummaryResponse author;
    private Instant createdAt;
    /** 대댓글 목록 (1단계까지만 허용). */
    private List<CommentResponse> children;

    /** Comment 엔티티로부터 재귀적으로 children을 매핑한다. */
    public static CommentResponse from(Comment comment) {
        List<CommentResponse> children = comment.getChildren().stream()
            .map(CommentResponse::from)
            .toList();

        return CommentResponse.builder()
            .id(comment.getId())
            .content(comment.getContent())
            .author(MemberSummaryResponse.from(comment.getMember()))
            .createdAt(comment.getCreatedAt())
            .children(children)
            .build();
    }
}
