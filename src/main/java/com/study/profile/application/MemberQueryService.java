package com.study.profile.application;

import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.MemberRepository;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 외부 BC(blog 등)가 userId 기준으로 멤버 정보를 조회할 때 사용. */
@Service
@Transactional(readOnly = true)
public class MemberQueryService {

  private final MemberRepository memberRepository;

  public MemberQueryService(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  /** userId로 멤버 이름 단건 조회. 프로필이 없으면 "알 수 없음" 반환. */
  public String findAuthorName(Long userId) {
    return memberRepository.findByUserId(userId).map(Member::getName).orElse("알 수 없음");
  }

  /** userId 목록으로 멤버 이름 배치 조회. N+1 방지용. 반환: userId → name 맵. */
  public Map<Long, String> findAuthorNamesByUserIds(Collection<Long> userIds) {
    return memberRepository.findAllByUserIdIn(userIds).stream()
        .collect(Collectors.toMap(m -> m.getUser().getId(), Member::getName));
  }
}
