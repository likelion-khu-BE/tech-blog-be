package com.study.shared.extevent.blog;

/**
 * 블로그 댓글 작성 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param commentId 작성된 댓글
 */
public record BlogCommentCreated(Long userId, Long commentId) {}
