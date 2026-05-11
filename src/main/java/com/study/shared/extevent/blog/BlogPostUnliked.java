package com.study.shared.extevent.blog;

/**
 * 블로그 글 좋아요 취소 사건. 양방향.
 *
 * @param likerId 좋아요 취소한 사람 (auth.User.id)
 * @param postId 글
 * @param postOwnerId 글 작성자 (auth.User.id)
 */
public record BlogPostUnliked(Long likerId, Long postId, Long postOwnerId) {}
