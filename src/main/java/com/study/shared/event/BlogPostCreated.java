package com.study.shared.event;

/**
 * 블로그 글이 발행됐다는 도메인 사실.
 *
 * <p>blog 모듈이 글 저장 직후 발행. profile 모듈이 받아 활동 이력·점수에 반영.
 *
 * @param userId 글 작성자 (auth.User.id — 모든 BC 공유 키)
 * @param postId 작성된 글의 식별자 (활동 카드 클릭 시 라우팅용)
 */
public record BlogPostCreated(Long userId, Long postId) {}
