package com.study.profile.application;

import com.study.profile.application.dto.GenerationCreateRequest;
import com.study.profile.application.dto.GenerationDto;
import com.study.profile.application.dto.GenerationMemberAddRequest;
import com.study.profile.application.dto.GenerationMemberDto;
import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.generation.MemberGeneration;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.GenerationRepository;
import com.study.profile.infrastructure.MemberGenerationRepository;
import com.study.profile.infrastructure.MemberRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GenerationService {

  private final GenerationRepository generationRepository;
  private final MemberRepository memberRepository;
  private final MemberGenerationRepository memberGenerationRepository;

  public GenerationService(
      GenerationRepository generationRepository,
      MemberRepository memberRepository,
      MemberGenerationRepository memberGenerationRepository) {
    this.generationRepository = generationRepository;
    this.memberRepository = memberRepository;
    this.memberGenerationRepository = memberGenerationRepository;
  }

  public List<GenerationDto> getGenerations() {
    return generationRepository.findAllByOrderByNumberAsc().stream()
        .map(GenerationDto::from)
        .toList();
  }

  public GenerationDto getGeneration(Integer generationId) {
    return GenerationDto.from(findById(generationId));
  }

  @Transactional
  public Map<String, Integer> createGeneration(GenerationCreateRequest req) {
    if (Boolean.TRUE.equals(req.isCurrent())) {
      unmarkCurrentGeneration(null);
      generationRepository.flush();
    }
    Generation g = Generation.create(req.number(), req.startDate(), req.endDate(), req.isCurrent());
    return Map.of("id", generationRepository.save(g).getNumber());
  }

  @Transactional
  public Map<String, Integer> updateGeneration(Integer generationId, GenerationCreateRequest req) {
    Generation g = findById(generationId);
    if (Boolean.TRUE.equals(req.isCurrent())) {
      unmarkCurrentGeneration(generationId);
      generationRepository.flush();
    }
    g.update(req.number(), req.startDate(), req.endDate(), req.isCurrent());
    return Map.of("id", g.getNumber());
  }

  public List<GenerationMemberDto> getGenerationMembers(Integer generationId) {
    findById(generationId);
    return memberGenerationRepository.findByGenerationId(generationId).stream()
        .map(GenerationMemberDto::from)
        .toList();
  }

  @Transactional
  public Map<String, Integer> addMemberToGeneration(
      Integer generationId, GenerationMemberAddRequest req) {
    Generation generation = findById(generationId);
    Member member =
        memberRepository
            .findById(req.memberId())
            .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));
    memberGenerationRepository
        .findByMemberIdAndGenerationId(req.memberId(), generationId)
        .ifPresent(
            mg -> {
              throw new IllegalStateException("이미 해당 기수에 등록된 멤버입니다.");
            });
    MemberGeneration mg = MemberGeneration.create(member, generation, req.roleInGen());
    return Map.of("id", memberGenerationRepository.save(mg).getId().intValue());
  }

  private Generation findById(Integer generationId) {
    return generationRepository
        .findById(generationId)
        .orElseThrow(() -> new IllegalArgumentException("기수를 찾을 수 없습니다."));
  }

  private void unmarkCurrentGeneration(Integer excludeId) {
    generationRepository
        .findCurrentGeneration()
        .filter(g -> !g.getNumber().equals(excludeId))
        .ifPresent(g -> g.update(g.getNumber(), g.getStartDate(), g.getEndDate(), false));
  }
}
