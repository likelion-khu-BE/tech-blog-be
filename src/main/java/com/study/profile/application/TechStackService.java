package com.study.profile.application;

import com.study.profile.application.dto.TechStackCreateRequest;
import com.study.profile.application.dto.TechStackDto.IdResponse;
import com.study.profile.application.dto.TechStackDto.TechStackListResponse;
import com.study.profile.application.dto.TechStackDto.TechStackResponse;
import com.study.profile.domain.exception.TechStackNameDuplicateException;
import com.study.profile.domain.exception.TechStackNotFoundException;
import com.study.profile.domain.techstack.TechStack;
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
    // "all"(또는 미지정)이면 전체, 그 외엔 해당 분류만 필터링한다.
    //   전체 조회 후 Java에서 거른다 — PostgreSQL 커스텀 ENUM을 WHERE 절에 직접 쓰면 타입 불일치가 나고,
    //   tech_stack은 마스터 데이터라 수백 건 이하라 성능 문제 없음.
    boolean all = category == null || category.isBlank() || category.equalsIgnoreCase("all");

    TechStackCategory filter = null;
    if (!all) {
      try {
        filter = TechStackCategory.valueOf(category.toLowerCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("유효하지 않은 category 값입니다: " + category);
      }
    }

    final TechStackCategory cat = filter;
    List<TechStackResponse> list =
        techStackRepository.findAllByOrderByNameAsc().stream()
            .filter(ts -> cat == null || ts.getCategory() == cat)
            .map(TechStackResponse::from)
            .toList();

    return new TechStackListResponse(list);
  }

  /**
   * 기술 스택 등록 (관리자 — §3-2).
   *
   * <p>이름 중복은 미리 막아 친절한 409 메시지를 준다 (DB unique 제약에만 맡기면 "중복된 요청입니다"로 뭉뚱그려짐). 생성은 도메인 정적 팩토리 {@code
   * TechStack.create}에 위임한다.
   */
  @Transactional
  public IdResponse create(TechStackCreateRequest req) {
    if (techStackRepository.existsByName(req.name())) {
      throw new TechStackNameDuplicateException(req.name());
    }
    TechStack techStack = TechStack.create(req.name(), req.category(), req.logoUrl());
    return new IdResponse(techStackRepository.save(techStack).getId());
  }

  /**
   * 기술 스택 수정 (관리자 — §3-3).
   *
   * <p>요청 body는 name/category/logoUrl 전체를 받아 통째로 교체한다 (전체 교체 시맨틱 — 일부만 보내면 안 보낸 필드는 비워진다). 변경은 도메인
   * 메서드 {@code techStack.update}에 위임하고, 트랜잭션 종료 시 변경 감지(dirty checking)로 자동 반영된다 (save/merge 호출 안
   * 함).
   *
   * <p>{@code existsByNameAndIdNot}은 자기 자신을 제외하므로, 이름을 그대로 두든 바꾸든 "남의 이름과 겹칠 때"만 409를 던진다.
   */
  @Transactional
  public IdResponse update(Long id, TechStackCreateRequest req) {
    TechStack techStack =
        techStackRepository.findById(id).orElseThrow(() -> new TechStackNotFoundException(id));
    if (techStackRepository.existsByNameAndIdNot(req.name(), id)) {
      throw new TechStackNameDuplicateException(req.name());
    }
    techStack.update(req.name(), req.category(), req.logoUrl());
    return new IdResponse(techStack.getId());
  }

  /**
   * 기술 스택 삭제 (관리자 — §3-4).
   *
   * <p>하드 삭제다. DB의 ON DELETE CASCADE에 의해 이 기술 스택을 보유한 멤버(member_tech_stack)·팀(team_tech_stack) 연결도
   * 함께 삭제된다. 즉 사용 중이어도 막지 않는다.
   *
   * <p>TODO(후속): 사용 중(보유 멤버·팀 존재)이면 삭제를 막고 409를 주는 정책도 검토 가능. 현재는 명세대로 하드 삭제.
   */
  @Transactional
  public void delete(Long id) {
    if (!techStackRepository.existsById(id)) {
      throw new TechStackNotFoundException(id);
    }
    techStackRepository.deleteById(id);
  }
}
