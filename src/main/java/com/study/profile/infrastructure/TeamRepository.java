package com.study.profile.infrastructure;

import com.study.profile.domain.team.TeamProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<TeamProfile, Long> {

  Optional<TeamProfile> findByInviteCode(String inviteCode);

  // 이전에는 findAll() / findByGenerationNumber()를 사용했음.
  // TeamProfile의 연관 컬렉션(techStacks, images, members)이 모두 LAZY 로딩이라
  // toListResponse()에서 각 필드 접근 시마다 SELECT가 추가 발생 → N+1 문제.
  // 팀 10개면 최대 1(목록) + 10*4(연관 4개) = 41번 쿼리가 나갔음.
  // JOIN FETCH + DISTINCT로 한 번에 가져오도록 변경.
  @Query(
      "SELECT DISTINCT t FROM TeamProfile t "
          + "LEFT JOIN FETCH t.generation "
          + "LEFT JOIN FETCH t.techStacks ts "
          + "LEFT JOIN FETCH ts.techStack "
          + "LEFT JOIN FETCH t.images "
          + "LEFT JOIN FETCH t.members")
  List<TeamProfile> findAllWithDetails();

  @Query(
      "SELECT DISTINCT t FROM TeamProfile t "
          + "LEFT JOIN FETCH t.generation "
          + "LEFT JOIN FETCH t.techStacks ts "
          + "LEFT JOIN FETCH ts.techStack "
          + "LEFT JOIN FETCH t.images "
          + "LEFT JOIN FETCH t.members "
          + "WHERE t.generation.number = :generationNumber")
  List<TeamProfile> findByGenerationNumberWithDetails(
      @Param("generationNumber") Integer generationNumber);
}
