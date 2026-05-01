package com.study.shared.event;

/**
 * 행사 게시글이 작성됐다는 도메인 사실.
 *
 * <p>발행자는 sessionboard 모듈 — {@code EventPost} 엔티티 작성 시점 (해커톤 후기 등).
 *
 * @param userId 게시글 작성자 (auth.User.id)
 * @param postId 작성된 게시글 ID
 */
public record EventPostCreated(Long userId, Long postId) {}
