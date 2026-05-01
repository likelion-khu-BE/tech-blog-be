package com.study.shared.event;

/**
 * 블로그 댓글이 작성됐다는 도메인 사실.
 *
 * @param userId 댓글 작성자 (auth.User.id)
 * @param commentId 작성된 댓글의 식별자
 */
public record BlogCommentCreated(Long userId, Long commentId) {}
