# KHU LikeLion Tech Blog - Backend

경희대학교 멋쟁이사자처럼 기술블로그 백엔드 레포지토리입니다.

> Frontend: https://khu-tech.blog

## 기술 스택

- Java 25, Spring Boot 3.5, Gradle
- 멀티모듈 모놀리식 아키텍처

---

## 모듈 구조

```
study-be/
├── module-common/          ← 공통 설정, 유틸리티
├── module-contract/        ← 모듈 간 계약 (인터페이스, DTO, 이벤트)
├── module-blog/            ← blog 팀
├── module-qna/             ← qna 팀
├── module-profile/         ← profile 팀
├── module-session-board/   ← session-board 팀
└── module-app/             ← 조립 및 실행
```

### 모듈 설명

| 모듈 | 역할 | 소유 |
|---|---|---|
| `module-common` | 전체 모듈에서 사용하는 공통 코드 | 관리자 |
| `module-contract` | 모듈 간 통신을 위한 인터페이스/DTO/이벤트 | 전체 팀 공동 |
| `module-blog` | 아티클, 태그 등 블로그 기능 | blog 팀 |
| `module-qna` | 질문/답변 기능 | qna 팀 |
| `module-profile` | 멤버 프로필 기능 | profile 팀 |
| `module-session-board` | 세션 게시판 기능 | session-board 팀 |
| `module-app` | 모든 모듈을 조립하여 실행 가능한 애플리케이션으로 만듦 | 관리자 |

---

## 협업 규칙

### 1. 자기 모듈에서만 작업한다

각 팀은 자신이 소유한 모듈 디렉토리에서만 개발합니다. 다른 팀의 모듈을 직접 수정하지 않습니다.

```
blog 팀    → module-blog/ 에서만 작업
qna 팀     → module-qna/ 에서만 작업
profile 팀 → module-profile/ 에서만 작업
session-board 팀 → module-session-board/ 에서만 작업
```

### 2. 다른 모듈을 직접 의존할 수 없다

각 팀 모듈의 `build.gradle`에는 `module-common`과 `module-contract`만 의존으로 선언되어 있습니다. 다른 팀 모듈의 클래스를 import하면 **컴파일이 실패**합니다.

```
허용: module-blog → module-contract, module-common
금지: module-blog → module-qna (컴파일 에러)
```

**의존 방향 다이어그램:**

```
  blog   qna   profile   session-board
    \     |      |       /
     contract  common
           \  /
           app (조립)
```

### 3. 모듈 간 통신은 contract를 통해서만

다른 모듈의 기능이 필요하면 `module-contract`에 인터페이스/DTO/이벤트를 정의하고, 구현은 해당 모듈 내부에서 합니다.

**contract에 들어가는 것:**
- 인터페이스 (Port) — 모듈 간 호출 계약
- DTO (Request/Response) — 모듈 간 데이터 전달 객체
- 이벤트 — 모듈 간 비동기 알림
- 공유 Enum

**contract에 들어가면 안 되는 것:**
- Entity (DB 매핑 객체)
- Repository
- 비즈니스 로직
- 인프라 의존성 (JPA, Spring Data 등)

### 4. contract 변경은 전체 팀 리뷰가 필요하다

contract 모듈은 모든 팀에 영향을 줍니다. contract를 수정하는 PR은 CODEOWNERS에 의해 **모든 팀에 리뷰 요청**이 갑니다. 혼자 바꾸고 머지할 수 없습니다.

### 5. 인프라 파일은 관리자만 수정한다

다음 파일/디렉토리는 관리자 소유입니다. 수정이 필요하면 관리자에게 요청하세요.

- `build.gradle` (루트)
- `settings.gradle`
- `module-common/`
- `module-app/`
- `.github/`

특히 **각 모듈의 `build.gradle`에 새 의존성을 추가할 때는 반드시 관리자 확인**을 받으세요. 다른 팀 모듈을 의존에 몰래 추가하면 아키텍처가 무너집니다.

---

## 아키텍처 검증

모듈 경계는 두 가지 수준에서 강제됩니다.

### Gradle 의존성 차단 (1차)

`build.gradle`에 선언하지 않은 모듈은 import 자체가 불가능합니다. 컴파일 단계에서 차단됩니다.

### ArchUnit 테스트 (2차)

`module-app`의 `ArchitectureTest`가 패키지 레벨에서 의존성 규칙을 검증합니다. CI에서 `./gradlew test` 실행 시 위반이 있으면 빌드가 실패합니다.

검증하는 규칙:
- 각 팀 모듈 패키지는 다른 팀 모듈 패키지에 의존할 수 없다
- contract 패키지는 팀 모듈 패키지에 의존할 수 없다
- common 패키지는 팀 모듈, contract 패키지에 의존할 수 없다

---

## 개발 환경 설정

### Java 25 설치

```bash
# SDKMAN 사용
sdk install java 25.0.2-tem
sdk use java 25.0.2-tem
```

### 빌드 및 테스트

```bash
# 전체 빌드
./gradlew clean build

# 특정 모듈만 빌드
./gradlew :module-blog:build

# 아키텍처 테스트만 실행
./gradlew :module-app:test

# 애플리케이션 실행
./gradlew :module-app:bootRun
```

---

## 브랜치 전략

- `main` — 보호된 브랜치. 직접 push 금지.
- PR을 통해서만 머지하며, CODEOWNERS 리뷰 승인 필수.
- CI 테스트 (빌드 + ArchUnit) 통과 필수.

---

## 패키지 컨벤션

각 팀 모듈의 기본 패키지:

```
com.study.blog          ← module-blog
com.study.qna           ← module-qna
com.study.profile       ← module-profile
com.study.sessionboard  ← module-session-board
com.study.contract      ← module-contract
com.study.common        ← module-common
```

모듈 내부 패키지 구조는 각 팀이 자율적으로 결정합니다.
