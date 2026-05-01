package com.study.profile.application;

import com.study.profile.application.dto.MemberSummaryDto;
import com.study.profile.infrastructure.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link MemberQueryService} 잠정 구현.
 *
 * <p>외부 BC가 작성자 정보 조회 시 호출. 모놀리식 jar 안 빈 직접 의존 (HTTP X).
 *
 * <p>잠정 정책: Member 없으면 {@link Optional#empty()} 반환 — 호출부가 정책 결정 (throw / 기본 표시 / skip 등).
 *
 * <p>풀스펙(5/4 이후, 세인 영역) 시 — 추가 정보 노출(예: sessionType, 기수) 필요해지면 DTO 필드 추가. *purely additive*라 외부 BC
 * 깨지지 않음.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryServiceImpl implements MemberQueryService {

  private final MemberRepository memberRepository;

  @Override
  public Optional<MemberSummaryDto> getById(Long userId) {
    return memberRepository
        .findByUserId(userId)
        .map(m -> new MemberSummaryDto(m.getId(), m.getName(), m.getProfileImageUrl()));
  }
}
