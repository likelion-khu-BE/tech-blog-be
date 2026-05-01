package com.study.shared.event;

/**
 * 세션보드 게시글이 작성됐다는 도메인 사실.
 *
 * <p>발행자는 sessionboard 모듈 — 정확한 도메인 매핑(예: {@code EventPost})은 sessionboard 팀과 합의 시 확정.
 *
 * @param userId 게시글 작성자 (auth.User.id)
 * @param postId 작성된 게시글의 식별자
 */
public record SessionPostCreated(Long userId, Long postId) {}
