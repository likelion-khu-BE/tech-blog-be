package com.study.profile.presentation;

import com.study.profile.application.dto.TechStackDto.TechStackListResponse;
import com.study.profile.application.TechStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 13. Controller — HTTP 요청의 진입점
//     클라이언트(프론트엔드)가 보낸 요청을 받아서 Service에 넘기고,
//     Service가 돌려준 결과를 HTTP 응답으로 내보내는 역할만 한다.
@RestController  // @Controller + @ResponseBody. 반환값을 JSON으로 자동 변환한다.
@RequestMapping("/api/profile/tech-stacks")  // 이 컨트롤러가 처리할 URL prefix
@RequiredArgsConstructor
public class TechStackController {

  private final TechStackService techStackService;

  // 14. @GetMapping — GET /api/profile/tech-stacks 요청을 이 메서드가 처리한다.
  //     @RequestParam(required = false) — URL 뒤에 ?category=language 처럼 붙는 쿼리 파라미터.
  //     required = false 이므로 생략 가능 → 생략하면 category가 null로 들어온다.
  @GetMapping
  public ResponseEntity<TechStackListResponse> getTechStacks(
      @RequestParam(required = false) String category) {

    // 15. ResponseEntity.ok() — HTTP 200 OK 상태코드와 함께 body를 응답한다.
    //     Service에서 받은 TechStackListResponse가 JSON으로 변환되어 클라이언트에 전달된다.
    return ResponseEntity.ok(techStackService.getTechStacks(category));
  }
}
