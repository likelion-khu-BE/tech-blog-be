package com.study.shared.extevent.sessionboard;

/**
 * 행사 게시글 삭제 사건.
 *
 * @param userId 작성자 (auth.User.id)
 * @param postId 삭제된 게시글
 */
public record SessionEventPostDeleted(Long userId, Long postId) {}
