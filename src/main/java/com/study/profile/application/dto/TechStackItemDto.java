package com.study.profile.application.dto;

import com.study.profile.domain.techstack.MemberTechStack;
import com.study.profile.domain.techstack.TechStackCategory;

public record TechStackItemDto(
    Long techStackId,
    String name,
    TechStackCategory category,
    String logoUrl,
    Integer proficiency) {

  public static TechStackItemDto from(MemberTechStack mts) {
    return new TechStackItemDto(
        mts.getTechStack().getId(),
        mts.getTechStack().getName(),
        mts.getTechStack().getCategory(),
        mts.getTechStack().getLogoUrl(),
        mts.getProficiency());
  }
}
