package com.study.shared.extevent.blog;

/**
 * 블로그 글 좋아요 사건. 양방향 — 누른 사람·받은 사람 양쪽 식별.
 *
 * @param likerId 좋아요 누른 사람 (auth.User.id)
 * @param postId 글
 * @param postOwnerId 글 작성자 (auth.User.id)
 */
public record BlogPostLiked(Long likerId, Long postId, Long postOwnerId) {}
