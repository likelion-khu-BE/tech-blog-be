package com.study.shared.extevent.blog;

/**
 * 블로그 댓글 삭제 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param postId 댓글이 달린 글 (consumer 활용은 자유 — 시그니처 일관 유지용)
 * @param commentId 삭제된 댓글
 */
public record BlogCommentDeleted(Long userId, Long postId, Long commentId) {}
