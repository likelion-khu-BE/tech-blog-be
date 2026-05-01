package com.study.qna.application.dto.response.tag;

import com.study.qna.domain.Tag;

/** 태그 응답 DTO. */
public record TagResponse(Long id, String name) {

  public static TagResponse from(Tag tag) {
    return new TagResponse(tag.getId(), tag.getName());
  }
}
