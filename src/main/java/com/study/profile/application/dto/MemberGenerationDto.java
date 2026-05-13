package com.study.profile.application.dto;

import com.study.profile.domain.generation.GenerationRole;
import com.study.profile.domain.generation.MemberGeneration;

public record MemberGenerationDto(
    Integer generationId,
    String label,
    Integer number,
    GenerationRole roleInGen) {

  public static MemberGenerationDto from(MemberGeneration mg) {
    Integer number = mg.getGeneration().getNumber();
    return new MemberGenerationDto(number, number + "기", number, mg.getRoleInGen());
  }
}
