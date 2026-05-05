package com.study.shared.extevent.sessionboard;

/**
 * sessionboard의 행사 게시글이 작성됐다는 도메인 사실.
 *
 * <p>발행자는 sessionboard 모듈 — {@code EventPost} 엔티티 작성 시점 (해커톤 후기 등).
 *
 * <p>주의: 여기 "Event"는 sessionboard 도메인 용어 (행사). Spring/integration event와 별개. 클래스명에 {@code Session}
 * prefix를 붙여 use-site 모호성 방지 (enum {@code session_event_post}와 일관).
 *
 * @param userId 게시글 작성자 (auth.User.id)
 * @param postId 작성된 게시글 ID
 */
public record SessionEventPostCreated(Long userId, Long postId) {}
