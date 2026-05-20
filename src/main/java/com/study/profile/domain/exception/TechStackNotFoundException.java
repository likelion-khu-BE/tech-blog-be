package com.study.profile.domain.exception;

/** 기술 스택을 찾을 수 없을 때 (잘못된 techStackId). 404로 매핑. */
public class TechStackNotFoundException extends ProfileException {
  public TechStackNotFoundException(Long id) {
    super("기술 스택을 찾을 수 없습니다: " + id);
  }
}
