package com.study.qna.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

    @NotBlank
    private String content;

    /** 대댓글인 경우 부모 댓글 ID. 루트 댓글이면 null. */
    private Long parentId;
}
