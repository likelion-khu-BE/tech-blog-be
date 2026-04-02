# KHU LikeLion Tech Blog - Backend

경희대 멋쟁이사자처럼 기술블로그 백엔드

> Frontend: https://khu-tech.blog

**Java 21 · Spring Boot 3.5 · Gradle · 멀티모듈 모놀리식**

---

## 모듈 구조

```
study-be/
├── common/          공통 설정, 유틸리티
├── contract/        모듈 간 계약 (인터페이스, DTO, 이벤트)
├── blog/            blog 팀
├── qna/             qna 팀
├── profile/         profile 팀
├── session-board/   session-board 팀
└── app/             조립 및 실행
```

### 의존 방향

```
  blog   qna   profile   session-board
    \     |      |       /
     contract  common
           \  /
           app
```

- 팀 모듈은 `common`과 `contract`만 의존 가능
- 팀 모듈끼리 직접 의존 → **컴파일 에러**

---

## 모듈 간 통신

다른 모듈의 기능이 필요하면 `contract`에 인터페이스(Port)를 정의하고, 해당 모듈이 구현한다.

```
contract/  → BlogPort 인터페이스 정의
blog/      → BlogService가 BlogPort 구현
profile/   → ProfileService에서 BlogPort 주입받아 사용
```

**contract에 허용**: 인터페이스(Port), DTO, 이벤트, 공유 Enum

**contract에 금지**: Entity, Repository, 비즈니스 로직, 인프라 의존성(JPA 등)

> contract 변경 PR은 CODEOWNERS에 의해 **전체 팀 리뷰** 필요

---

## 아키텍처 검증

모듈 경계는 **2중으로 강제**된다.

| 수준 | 방식 | 시점 |
|---|---|---|
| 1차 | Gradle 의존성 — 미선언 모듈 import 불가 | 컴파일 |
| 2차 | ArchUnit 테스트 — 패키지 레벨 의존 규칙 | 테스트 (CI) |

---

## 협업 규칙

1. **자기 모듈에서만 작업** — 다른 팀 모듈 직접 수정 금지
2. **모듈 간 통신은 contract 경유** — 직접 참조 금지
3. **의존성 추가 시 관리자 확인** — `build.gradle`에 임의로 팀 모듈 추가 금지
4. **인프라 파일은 관리자 소유** — `common/`, `app/`, `build.gradle`, `settings.gradle`, `.github/`

---

## 개발 환경

```bash
# Java 21 설치 (SDKMAN)
sdk install java 21.0.7-tem

# 전체 빌드
./gradlew clean build

# 특정 모듈만 빌드
./gradlew :blog:build

# 아키텍처 테스트만
./gradlew :app:test

# 실행
./gradlew :app:bootRun
```

---

## 브랜치 전략

- `main` — 보호 브랜치, 직접 push 금지
- PR + CODEOWNERS 리뷰 승인 + CI 통과 필수
