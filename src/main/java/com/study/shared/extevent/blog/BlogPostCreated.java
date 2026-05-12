package com.study.shared.extevent.blog;

/**
 * 블로그 글 발행 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param postId 발행된 글
 */
public record BlogPostCreated(Long userId, Long postId) {}
