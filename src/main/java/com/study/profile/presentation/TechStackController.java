package com.study.profile.presentation;

import com.study.profile.application.TechStackService;
import com.study.profile.application.dto.TechStackCreateRequest;
import com.study.profile.application.dto.TechStackDto.IdResponse;
import com.study.profile.application.dto.TechStackDto.TechStackListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 13. Controller — HTTP 요청의 진입점
//     클라이언트(프론트엔드)가 보낸 요청을 받아서 Service에 넘기고,
//     Service가 돌려준 결과를 HTTP 응답으로 내보내는 역할만 한다.
@Tag(name = "기술 스택", description = "기술 스택 카탈로그 조회·등록·수정·삭제 API")
@RestController // @Controller + @ResponseBody. 반환값을 JSON으로 자동 변환한다.
@RequestMapping("/api/profile/tech-stacks") // 이 컨트롤러가 처리할 URL prefix
@RequiredArgsConstructor
public class TechStackController {

  private final TechStackService techStackService;

  // 14. @GetMapping — GET /api/profile/tech-stacks 요청을 이 메서드가 처리한다.
  //     @RequestParam(required = false) — URL 뒤에 ?category=language 처럼 붙는 쿼리 파라미터.
  //     required = false 이므로 생략 가능 → 생략하면 category가 null로 들어온다.
  @Operation(
      summary = "기술 스택 목록 조회",
      description = "category로 분류 필터. all(기본값)이면 전체 기술 스택을 이름순으로 반환합니다.")
  @GetMapping
  public ResponseEntity<TechStackListResponse> getTechStacks(
      @Parameter(
              description = "기술 분류 필터 — all = 전체 기술 스택",
              required = true,
              schema =
                  @Schema(
                      allowableValues = {
                        "all",
                        "language",
                        "framework",
                        "ai",
                        "design",
                        "tool",
                        "infra",
                        "etc"
                      },
                      defaultValue = "all"))
          @RequestParam(required = false, defaultValue = "all")
          String category) {
    return ResponseEntity.ok(techStackService.getTechStacks(category));
  }

  @Operation(
      summary = "기술 스택 등록 (관리자)",
      description = "name 중복 시 409. logoUrl은 외부 CDN 주소를 그대로 받습니다.")
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<IdResponse> createTechStack(
      @Valid @RequestBody TechStackCreateRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(techStackService.create(req));
  }

  @Operation(
      summary = "기술 스택 수정 (관리자)",
      description =
          "name/category/logoUrl 전체를 통째로 교체합니다 (일부만 보내면 안 보낸 필드는 비워짐). 다른 기술과 이름 중복 시 409.")
  @PutMapping("/{techStackId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<IdResponse> updateTechStack(
      @PathVariable Long techStackId, @Valid @RequestBody TechStackCreateRequest req) {
    return ResponseEntity.ok(techStackService.update(techStackId, req));
  }

  @Operation(
      summary = "기술 스택 삭제 (관리자)",
      description = "하드 삭제. 이 기술을 보유한 멤버·팀 연결도 함께 삭제됩니다(ON DELETE CASCADE).")
  @DeleteMapping("/{techStackId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteTechStack(@PathVariable Long techStackId) {
    techStackService.delete(techStackId);
    return ResponseEntity.noContent().build();
  }
}
