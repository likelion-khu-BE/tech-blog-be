package com.study.profile.infrastructure;

import com.study.profile.domain.generation.MemberGeneration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberGenerationRepository extends JpaRepository<MemberGeneration, Long> {

  @Query(
      "SELECT mg FROM MemberGeneration mg JOIN FETCH mg.member WHERE mg.generation.number = :generationId ORDER BY mg.joinedAt ASC")
  List<MemberGeneration> findByGenerationId(@Param("generationId") Integer generationId);

  @Query(
      "SELECT mg FROM MemberGeneration mg JOIN FETCH mg.generation WHERE mg.member.id = :memberId")
  List<MemberGeneration> findByMemberId(@Param("memberId") Long memberId);

  @Query(
      "SELECT mg FROM MemberGeneration mg WHERE mg.member.id = :memberId AND mg.generation.number = :generationId")
  Optional<MemberGeneration> findByMemberIdAndGenerationId(
      @Param("memberId") Long memberId, @Param("generationId") Integer generationId);

  @Query("SELECT mg.member.id FROM MemberGeneration mg WHERE mg.generation.number = :generationId")
  List<Long> findMemberIdsByGenerationId(@Param("generationId") Integer generationId);
}
