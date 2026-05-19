package com.study.profile.application.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 페이지 응답 래퍼. Spring {@link Page} JSON 직렬화 기본 형식이 클라이언트가 안 쓰는 필드 많고 키 이름 불일치 → 명세 형식으로 정리.
 *
 * <p>profile API 응답 명세: {@code {content, page, size, totalElements, totalPages, hasNext}}.
 *
 */
public record PageWrapper<T>(
    List<T> content, int page, int size, long totalElements, int totalPages, boolean hasNext) {

  public static <T> PageWrapper<T> from(Page<T> page) {
    return new PageWrapper<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.hasNext());
  }
}
