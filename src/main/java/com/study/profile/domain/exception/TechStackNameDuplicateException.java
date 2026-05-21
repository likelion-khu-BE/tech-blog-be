package com.study.profile.domain.exception;

/** 이미 존재하는 이름으로 기술 스택을 등록/수정하려 할 때. 409로 매핑. */
public class TechStackNameDuplicateException extends ProfileException {
  public TechStackNameDuplicateException(String name) {
    super("이미 존재하는 기술 스택 이름입니다: " + name);
  }
}
