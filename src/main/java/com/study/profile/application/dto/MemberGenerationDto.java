package com.study.profile.application.dto;

import com.study.profile.domain.generation.GenerationRole;
import com.study.profile.domain.generation.MemberGeneration;

public record MemberGenerationDto(
    Long generationId, String label, Integer number, GenerationRole roleInGen) {

  public static MemberGenerationDto from(MemberGeneration mg) {
    return new MemberGenerationDto(
        mg.getGeneration().getId(),
        mg.getGeneration().getLabel(),
        mg.getGeneration().getNumber(),
        mg.getRoleInGen());
  }
}
