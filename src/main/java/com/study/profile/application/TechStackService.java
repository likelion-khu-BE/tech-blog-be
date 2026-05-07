package com.study.profile.application;

import com.study.profile.application.dto.TechStackDto.TechStackListResponse;
import com.study.profile.application.dto.TechStackDto.TechStackResponse;
import com.study.profile.domain.techstack.TechStackCategory;
import com.study.profile.infrastructure.TechStackRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 8. Service — 비즈니스 로직을 담당하는 계층
//    Controller는 "어떤 요청이 왔는지"만 알고,
//    실제로 "무엇을 어떻게 처리할지"는 Service가 결정한다.
@Service
// 9. @RequiredArgsConstructor — final 필드를 파라미터로 받는 생성자를 자동 생성한다.
//    덕분에 아래 techStackRepository 필드에 Spring이 자동으로 의존성을 주입한다.
@RequiredArgsConstructor
public class TechStackService {

  private final TechStackRepository techStackRepository;

  // 10. @Transactional(readOnly = true) — 이 메서드는 DB를 읽기만 하고 변경하지 않는다는 표시.
  //     JPA가 변경 감지(dirty checking)를 생략해서 성능이 약간 좋아진다.
  @Transactional(readOnly = true)
  public TechStackListResponse getTechStacks(String category) {
    List<TechStackResponse> list;

    // 11. category 파라미터가 없으면(null 또는 빈 문자열) 전체 조회
    //     있으면 해당 카테고리로 필터링해서 조회
    if (category == null || category.isBlank()) {
      list = techStackRepository.findAllByOrderByNameAsc().stream()
          .map(TechStackResponse::from)  // 엔티티 → DTO 변환 (6번에서 만든 from() 사용)
          .toList();
    } else {
      // 12. 클라이언트가 "Language" 또는 "LANGUAGE" 같은 대소문자 섞인 값을 보내도 처리되도록
      //     toLowerCase()로 소문자로 바꾼 뒤 Enum으로 변환한다.
      //     Enum에 없는 값이면 IllegalArgumentException → 400 Bad Request로 응답된다.
      TechStackCategory cat;
      try {
        cat = TechStackCategory.valueOf(category.toLowerCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("유효하지 않은 category 값입니다: " + category);
      }
      list = techStackRepository.findAllByCategoryOrderByNameAsc(cat).stream()
          .map(TechStackResponse::from)
          .toList();
    }

    return new TechStackListResponse(list);
  }
}
