package com.study.shared.extevent.sessionboard;

/**
 * 행사 게시글 좋아요 취소 사건. 양방향.
 *
 * @param likerId 좋아요 취소한 사람 (auth.User.id)
 * @param postId 게시글
 * @param postOwnerId 게시글 작성자 (auth.User.id)
 */
public record SessionEventPostUnliked(Long likerId, Long postId, Long postOwnerId) {}
