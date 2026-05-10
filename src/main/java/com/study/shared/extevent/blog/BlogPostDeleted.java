package com.study.shared.extevent.blog;

/**
 * 블로그 글 삭제 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param postId 삭제된 글
 */
public record BlogPostDeleted(Long userId, Long postId) {}
