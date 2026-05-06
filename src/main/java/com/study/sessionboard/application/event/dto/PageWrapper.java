package com.study.sessionboard.application.event.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageWrapper<T>(
    List<T> content, int page, int size, long totalElements, int totalPages, boolean hasNext) {

  public static <T> PageWrapper<T> from(Page<T> pageResult) {
    return new PageWrapper<>(
        pageResult.getContent(),
        pageResult.getNumber(),
        pageResult.getSize(),
        pageResult.getTotalElements(),
        pageResult.getTotalPages(),
        pageResult.hasNext());
  }
}
