package com.study.shared.extevent.sessionboard;

/**
 * 행사 게시글 좋아요 사건. 양방향 — 누른 사람·받은 사람 양쪽 식별.
 *
 * @param likerId 좋아요 누른 사람 (auth.User.id)
 * @param postId 게시글
 * @param postOwnerId 게시글 작성자 (auth.User.id)
 */
public record SessionEventPostLiked(Long likerId, Long postId, Long postOwnerId) {}
