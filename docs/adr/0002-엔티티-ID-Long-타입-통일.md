# 0002. 엔티티 ID Long 타입 통일

Date: 2026-04-13

## Status

Accepted

## Context

엔티티 PK 타입을 뭘로 쓸지 정해야 한다. 후보는 세 가지:

- **Long** — DB의 `BIGINT`, auto increment
- **UUID** — 랜덤 생성, 128비트
- **Integer** — DB의 `INT`, auto increment

### Long

- PostgreSQL `BIGINT` + `IDENTITY`와 1:1 매핑
- 범위: 약 920경 (9.2 × 10^18). 스터디 프로젝트는 물론이고 대부분의 서비스에서 부족할 일이 없다
- 인덱스 성능: 8바이트 정수 비교라 빠름
- URL/로그에서 읽기 쉬움: `/users/42` vs `/users/550e8400-e29b-41d4-a716-446655440000`

### UUID

- 분산 시스템에서 충돌 없이 ID 생성 가능 (DB 없이도)
- 16바이트라 인덱스 크기가 Long 대비 2배
- B-Tree 인덱스에서 랜덤 삽입 → 페이지 분할 빈번 → 쓰기 성능 저하
- 이 프로젝트는 단일 DB, 단일 서버. 분산 ID가 필요한 상황이 아님

### Integer

- 4바이트로 가장 작지만, 범위가 약 21억. 대규모 서비스에서 부족할 수 있음
- Long으로 쓰면 되는 걸 굳이 Integer로 아낄 이유가 없다

### 팀 합의 (김우진, 박세인)

단일 DB 환경에서 UUID의 이점이 없고, Integer는 범위가 애매하다. Long이 가장 실용적.

## Decision

**모든 엔티티의 PK는 `Long` 타입을 사용한다.**

서비스 메서드, 포트 인터페이스, DTO에서 ID를 주고받을 때도 `Long`으로 통일한다.

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

## Consequences

**좋은 점**
- 팀 전체가 ID 타입으로 고민할 필요 없음 — 무조건 Long
- 모듈 간 포트에서 `Long userId`, `Long postId` 등 타입이 통일되어 혼란 없음
- PostgreSQL BIGINT와 자연스럽게 매핑

**안 좋은 점**
- 나중에 분산 시스템으로 전환하면 UUID 마이그레이션 필요 (현재 단일 DB라 해당 없음)
- ID가 순차적이라 외부 노출 시 전체 데이터 규모 추측 가능 (API 보안 레이어에서 처리)
