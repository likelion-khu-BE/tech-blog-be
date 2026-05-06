package com.study.sessionboard.application.event.dto;

import com.study.profile.domain.member.Member;

public record AuthorDto(Long id, String name, String initial) {

  public static AuthorDto from(Member member) {
    String initial =
        member.getName().length() >= 2 ? member.getName().substring(0, 2) : member.getName();
    return new AuthorDto(member.getId(), member.getName(), initial);
  }
}
