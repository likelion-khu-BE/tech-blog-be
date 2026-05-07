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

}
