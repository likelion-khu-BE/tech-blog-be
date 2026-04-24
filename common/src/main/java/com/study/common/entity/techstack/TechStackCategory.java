package com.study.common.entity.techstack;

/**
 * 기술 스택(TechStack)이 어느 분야에 속하는지 분류하는 열거형(Enum).
 *
 * <p>예) Java → language, Spring → backend, React → frontend
 *
 * <p>language : 프로그래밍 언어 (Java, Python, Kotlin 등) framework : 프레임워크/기술 (Spring, Django, React, Vue
 * 등) ai : AI/ML 관련 기술 (PyTorch, TensorFlow 등) design : 디자인 툴 (Figma, Photoshop 등) tool : 개발 도구
 * (Git, Jira 등) infra : 인프라/클라우드 (AWS, Docker, Kubernetes 등) etc : 기타
 */
public enum TechStackCategory {
  language,
  framework,
  ai,
  design,
  tool,
  infra,
  etc
}
