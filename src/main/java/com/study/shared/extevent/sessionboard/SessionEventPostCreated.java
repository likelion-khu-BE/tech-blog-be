package com.study.shared.extevent.sessionboard;

/**
 * 행사 게시글 작성 사건. EventPost 작성 시점.
 *
 * @param userId 작성자 (auth.User.id)
 * @param postId 작성된 게시글
 */
public record SessionEventPostCreated(Long userId, Long postId) {}
