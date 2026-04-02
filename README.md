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

---

## 모듈 간 통신

다른 모듈의 기능이 필요하면 `contract`에 인터페이스(Port)를 정의하고, 해당 모듈이 구현한다.

```
contract/  → BlogPort 인터페이스 정의
blog/      → BlogService가 BlogPort 구현
profile/   → ProfileService에서 BlogPort 주입받아 사용 (소비자)
```

### Port 규칙

- contract의 모든 인터페이스는 **`*Port`로 끝나야** 한다 (네이밍 컨벤션, ArchUnit 강제)
- **Port 구현체는 다른 모듈의 Port를 호출할 수 없다** (순환 의존 방지, ArchUnit 강제)
- 여러 모듈의 데이터를 조합해야 하면 **소비자(Controller 등)가 각 Port를 주입받아 조합**한다

```java
// BlogService (Port 구현체) — 자기 repository만 사용
@Override
public int countByMember(Long memberId) {
    return blogRepository.countByAuthorId(memberId);  // OK
    // profilePort.getProfile(memberId);              // 컴파일은 되지만 ArchUnit 실패
}

// ProfileController (소비자) — 여러 Port를 조합
public ContributionResponse getContribution(Long memberId) {
    int blogCount = blogPort.countByMember(memberId);
    int qnaCount = qnaPort.countByMember(memberId);
    return new ContributionResponse(blogCount, qnaCount);  // 여기서 조합
}
```

### contract에 허용하는 것

인터페이스(Port), DTO, 이벤트, 공유 Enum

### contract에 금지하는 것

Entity, Repository, 비즈니스 로직, 인프라 의존성(JPA 등)

> contract 변경 PR은 CODEOWNERS에 의해 **전체 팀 리뷰** 필요

---

## 아키텍처 검증

모듈 경계는 **3중으로 강제**된다.

| 수준 | 방식 | 시점 |
|---|---|---|
| 1차 | Gradle 의존성 — 미선언 모듈 import 불가 | 컴파일 |
| 2차 | ArchUnit — 패키지 레벨 의존 규칙, Port 격리 규칙 | 테스트 (CI) |
| 3차 | CODEOWNERS — contract 변경 시 전체 팀 리뷰 | PR |

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
