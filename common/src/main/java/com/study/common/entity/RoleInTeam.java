package com.study.common.entity;

/**
 * 팀 내에서 멤버가 맡은 역할 분야를 나타내는 열거형(Enum).
 *
 * <p>TeamMemberRole 테이블의 role 컬럼에 저장되며, 한 팀원이 여러 역할을 동시에 가질 수 있다 (예: backend + infra).
 *
 * <p>backend : 백엔드 개발 frontend : 프론트엔드 개발 design : UI/UX 디자인 ai : AI/ML 개발 pm : 기획(Project Manager)
 * infra : 인프라/DevOps etc : 기타
 */
public enum RoleInTeam {
  backend,
  frontend,
  design,
  ai,
  pm,
  infra,
  etc
}
