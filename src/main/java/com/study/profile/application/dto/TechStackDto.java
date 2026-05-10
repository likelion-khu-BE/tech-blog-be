package com.study.profile.application.dto;

import com.study.profile.domain.techstack.TechStack;
import com.study.profile.domain.techstack.TechStackCategory;
import java.util.List;

// 4. DTO (Data Transfer Object) — 계층 간 데이터를 주고받는 그릇
//    엔티티(TechStack)를 그대로 외부에 노출하지 않고,
//    필요한 필드만 골라서 응답 형태로 만든다.
public class TechStackDto {

  // 5. record — Java 16+ 문법. 불변 데이터 클래스를 한 줄로 선언한다.
  //    생성자, getter, equals, hashCode, toString 이 자동 생성된다.
  //    여기서는 클라이언트에게 돌려줄 기술 스택 1개의 모양을 정의한다.
  public record TechStackResponse(
      Long id, String name, TechStackCategory category, String logoUrl) {

    // 6. 정적 팩토리 메서드 from() — 엔티티 → DTO 변환을 한 곳에서 처리한다.
    //    Service에서 techStack.getId() 같은 변환 코드를 직접 짜지 않아도 된다.
    //    사용 예: TechStackResponse.from(techStack)
    public static TechStackResponse from(TechStack techStack) {
      return new TechStackResponse(
          techStack.getId(), techStack.getName(), techStack.getCategory(), techStack.getLogoUrl());
    }
  }

  // 7. 목록 응답 전용 DTO — List<TechStackResponse> 를 감싸서
  //    JSON 응답이 { "techStacks": [...] } 형태로 나오게 한다.
  public record TechStackListResponse(List<TechStackResponse> techStacks) {}
}
