package com.study.common.entity.member;

/**
 * 멤버가 속한 세션(트랙)의 종류를 나타내는 열거형(Enum).
 *
 * <p>backend : 백엔드 트랙 frontend : 프론트엔드 트랙 design : 디자인 트랙 ai : AI 트랙 pm : 기획(PM) 트랙 etc : 기타
 */
public enum SessionType {
  backend,
  frontend,
  design,
  ai,
  pm,
  etc
}
