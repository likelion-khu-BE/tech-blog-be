package com.study.profile.infrastructure;

import com.study.profile.domain.techstack.TechStack;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {

  // 2. 메서드 이름 규칙만 지키면 Spring Data JPA가 SQL을 자동으로 만들어준다.
  //    findAllBy          → SELECT * FROM tech_stack
  //    OrderByNameAsc     → ORDER BY name ASC
  //    → 결과: 전체 스택을 이름 오름차순으로 조회
  List<TechStack> findAllByOrderByNameAsc();

  // 같은 이름의 기술 스택이 이미 있는지 (등록 시 중복 차단용 — §3-2)
  boolean existsByName(String name);

  // 자기 자신(id)을 제외하고 같은 이름이 있는지 (수정 시 남의 이름과 중복 차단용 — §3-3)
  boolean existsByNameAndIdNot(String name, Long id);
}
