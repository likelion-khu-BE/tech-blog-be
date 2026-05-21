package com.study.profile.application.dto;

import com.study.profile.domain.member.Member;
import com.study.profile.domain.member.SessionType;

public record MemberSummaryDto(
    Long id,
    String name,
    String department,
    SessionType sessionType,
    String profileImageUrl,
    String intro) {

  public static MemberSummaryDto from(Member member) {
    return new MemberSummaryDto(
        member.getId(),
        member.getName(),
        member.getDepartment(),
        member.getSessionType(),
        member.getProfileImageUrl(),
        member.getIntro());
  }
}
