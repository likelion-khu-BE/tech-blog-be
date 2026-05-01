# QnA 팀 착수 가이드 (타 팀 구조 분석 + 실행 프롬프트)

## 1) 현재 코드베이스에서 확인한 사실
- `qna`는 `application/domain/infrastructure/presentation` 디렉토리만 있고 구현 파일은 없음.
- 공통 구조는 싱글 모듈 + DDD-Lite이며, BC는 `auth/blog/profile/qna/sessionboard/shared`.
- 전역 보안/예외는 `shared`에서 처리:
  - `shared/config/SecurityConfig`: JWT stateless, `/api/auth/**` + `/api/health/**`만 permitAll, 그 외 인증 필요.
  - `shared/config/JwtAuthenticationFilter`: `Authorization: Bearer ...` 파싱 후 `SecurityContext` 주입.
  - `auth/infrastructure/security/CurrentUser`, `CustomUserDetails`: 컨트롤러에서 현재 유저 주입.
  - `shared/exception/GlobalExceptionHandler`: auth 예외 + `@Valid` 에러를 `{status, message}` 형식으로 반환.
- 유저 공유 축:
  - `auth/domain/User`의 PK는 `Long`.
  - `profile/domain/member/Member`는 `User`를 `@OneToOne`으로 참조.
  - 즉, QnA도 작성자 식별은 `auth.User` 또는 `profile.Member` 참조 방식 중 하나를 명확히 선택해야 함.

## 2) 팀별 구현 성숙도 패턴
- `auth`: 가장 완성도 높음. `presentation(dto+controller) -> application(service) -> domain(entity/exception) -> infrastructure(repo/security)` 패턴이 명확.
- `blog`: CRUD/페이징/필터/좋아요 등 유즈케이스 구현은 되어 있으나 DTO는 `record`와 `class` 혼재.
- `profile`: 도메인 엔티티는 잘 잡혀 있으나 application/presentation/infrastructure 일부는 placeholder.
- `sessionboard`: 현재 domain 엔티티 중심.

## 3) QnA 팀이 따라야 할 기준 (권장)
- 레이어: `presentation -> application -> domain`, `infrastructure`는 repo/외부 연동.
- DTO: `record` 우선 (`docs/conventions/DTO.md` 기준).
- API 응답: 성공은 데이터 직접 반환, 실패는 `{status, message}`.
- 유저 주입: 컨트롤러에서 `@CurrentUser CustomUserDetails user` 받고 서비스에는 `Long userId` 전달.
- ID 타입: QnA 신규 코드는 `Long` 통일 (`docs/conventions/ID-타입.md` 기준).
- 예외: `qna/domain/exception/QnaException` 베이스 + 구체 예외 정의 후 `GlobalExceptionHandler`에 매핑 추가.

## 4) QnA 디렉토리 추천 골격
```text
src/main/java/com/study/qna
├─ domain
│  ├─ question
│  │  ├─ Question.java
│  │  ├─ QuestionStatus.java
│  │  └─ QuestionVote.java (선택)
│  ├─ answer
│  │  ├─ Answer.java
│  │  └─ AnswerVote.java (선택)
│  └─ exception
│     ├─ QnaException.java
│     ├─ QuestionNotFoundException.java
│     ├─ AnswerNotFoundException.java
│     └─ ForbiddenQnaActionException.java
├─ application
│  ├─ QuestionService.java
│  ├─ AnswerService.java
│  └─ dto
│     ├─ CreateQuestionRequest.java
│     ├─ UpdateQuestionRequest.java
│     ├─ QuestionResponse.java
│     ├─ CreateAnswerRequest.java
│     └─ AnswerResponse.java
├─ infrastructure
│  ├─ QuestionRepository.java
│  ├─ AnswerRepository.java
│  └─ QuestionSpecification.java (목록 검색 필요 시)
└─ presentation
   ├─ QuestionController.java
   └─ AnswerController.java
```

## 5) 바로 복붙해서 쓰는 "QnA 착수 프롬프트"
아래 프롬프트를 코딩 에이전트/팀 내부 작업지시용으로 그대로 사용.

```text
프로젝트: tech-blog-be

목표:
qna bounded context를 기존 팀 구조(auth/blog/profile/shared)에 맞춰 구현 시작.
이미 우리 팀이 가진 ERD, API 설계서, Entity 코드 초안, DTO 초안을 우선 반영하되,
현재 레포 컨벤션과 공통 인프라를 반드시 준수해서 통합해라.

반드시 지킬 구조/규칙:
1) 패키지 구조
- com.study.qna.{domain,application,infrastructure,presentation}
- domain: 엔티티/enum/도메인 예외
- application: 유스케이스 서비스 + application dto
- infrastructure: Spring Data Repository, 조회 스펙/쿼리
- presentation: Controller + request/response dto

2) 인증/인가 연동
- shared/config/SecurityConfig를 그대로 따름 (qna API는 인증 필요)
- 컨트롤러에서 @CurrentUser CustomUserDetails user로 유저 주입
- 서비스 메서드에는 Long userId를 파라미터로 전달
- 서비스 내부에서 SecurityUtils 직접 호출하지 않음(신규 코드 기준)

3) 유저/작성자 모델링
- auth.domain.User를 기준으로 연동
- 작성자 참조는 아래 중 하나로 일관되게 선택:
  A안) userId(Long)만 저장
  B안) User 엔티티 연관관계(@ManyToOne)
- 선택 이유를 코드 주석 또는 ADR 스타일 코멘트 3~5줄로 남길 것

4) DTO/응답/예외 컨벤션
- DTO는 record 우선
- 컨트롤러는 @Valid 사용
- 성공 응답: ResponseEntity로 데이터 직접 반환 (200/201/204)
- 에러 응답: {status, message}
- qna/domain/exception/QnaException 베이스 생성
- 주요 예외(404/403/409 등) 추가 후 GlobalExceptionHandler에 매핑

5) 이번 1차 구현 범위 (MVP)
- Question CRUD
- Answer CRUD (질문 하위 리소스)
- Question 목록 조회(페이지네이션) + 단건 조회
- 작성자 본인만 수정/삭제 가능

6) 산출물
- 필요한 Java 파일 생성/수정
- 엔드포인트 목록 정리(메서드, 경로, 인증 필요 여부)
- 우리 팀 ERD/API/DTO 초안과 충돌나는 지점 요약
- 실행 가능한 테스트 초안(최소 컨트롤러/서비스 레벨)

작업 순서:
1. qna domain 엔티티/enum/예외 생성
2. repository 생성
3. service 유즈케이스 구현
4. controller + request/response dto 연결
5. GlobalExceptionHandler qna 예외 매핑 추가
6. 기본 테스트 작성
7. 변경 파일 목록과 미해결 TODO 보고
```

## 6) QnA 팀 체크포인트 (착수 전 합의 권장)
- 작성자 참조를 `User`로 둘지 `Member`로 둘지 (프로필 공개정보 필요 여부 기준).
- 질문/답변 상태값(`OPEN/CLOSED`, `DELETED` 소프트삭제 여부).
- 채택답변(accept answer) 필요 여부.
- 좋아요/북마크/신고를 1차 범위에 넣을지 여부.
