package com.study.profile.application.dto;

/**
 * 외부 BC에 노출하는 작성자 요약 정보.
 *
 * <p>외부 BC(blog/qna/sessionboard)가 글 응답 등에 작성자를 표시할 때 받는 DTO. {@link
 * com.study.profile.domain.member.Member} 엔티티 직접 노출 X — 도메인 진화에도 외부 계약 안정.
 *
 * @param memberId 프로필 페이지 라우팅 토큰 ({@code Member.id}) — {@code /profile/members/{memberId}}로 박기만 하면
 *     됨
 * @param name 멤버 이름
 * @param profileImageUrl 프사 URL (없으면 {@code null})
 */
public record MemberSummaryDto(Long memberId, String name, String profileImageUrl) {}
