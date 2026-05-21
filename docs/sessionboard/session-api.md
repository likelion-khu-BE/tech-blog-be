# Session API 명세서

## 공통

| 항목 | 내용 |
|------|------|
| Base URL | `/api` |
| 인증 | `Authorization: Bearer {accessToken}` |
| Content-Type | `application/json` |
| 날짜 형식 | ISO 8601 (`2025-05-18T10:00:00Z`) |

### 공통 에러 응답
```json
{
  "status": 404,
  "message": "해당 세션을 찾을 수 없습니다."
}
```

| HTTP 상태 | 의미 |
|-----------|------|
| `400` | 요청 값 오류 |
| `401` | 인증 토큰 없음 또는 만료 |
| `403` | 본인 리소스가 아님 또는 권한 없음 |
| `404` | 리소스 없음 |
| `409` | 중복 작성 (예: 회고 1인 1회 제한) |

---

## 데이터 타입

```typescript
type SessionStatus    = 'SCHEDULED' | 'ONGOING' | 'DONE'
type ResourceType     = 'SLIDE' | 'CODE' | 'LINK' | 'DOCUMENT'
type ResourceVisibility = 'PUBLIC' | 'MEMBER' | 'PRIVATE'
```

### Author
```json
{ "id": 1, "name": "김지현", "initial": "김지" }
```

---

## 1. 세션 (Session)

### 1-1. 세션 목록 조회

```
GET /session-board/{generationNumber}/sessions
```

| Query | Type | 필수 | 설명 |
|-------|------|------|------|
| `status` | `SessionStatus` | 아니오 | 상태 필터 |

**Response `200 OK`**
```json
{
  "sessions": [
    {
      "id": 1,
      "weekLabel": "W1-1",
      "title": "Spring Boot 환경 세팅 & 프로젝트 구조",
      "status": "DONE",
      "startedAt": "2025-03-28T10:00:00Z",
      "speakers": [
        { "id": 10, "name": "최현우", "role": "발표자" }
      ],
      "rating": 4.6,
      "noteCount": 5,
      "resourceCount": 3
    }
  ]
}
```

---

### 1-2. 세션 단건 조회

```
GET /session-board/{generationNumber}/sessions/{sessionId}
```

**Response `200 OK`**
```json
{
  "id": 1,
  "weekLabel": "W1-1",
  "title": "Spring Boot 환경 세팅 & 프로젝트 구조",
  "status": "DONE",
  "startedAt": "2025-03-28T10:00:00Z",
  "speakers": [
    { "id": 10, "name": "최현우", "role": "발표자" }
  ],
  "rating": 4.6,
  "noteCount": 5,
  "resourceCount": 3
}
```

---

### 1-3. 세션 생성

```
POST /session-board/{generationNumber}/sessions
```

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `weekLabel` | `string` | 예 | 예: `W1` |
| `title` | `string` | 예 | — |
| `status` | `SessionStatus` | 아니오 | 기본값 `SCHEDULED` |
| `startedAt` | `string` | 아니오 | ISO 8601 |
| `speakerIds` | `number[]` | 아니오 | Member ID 목록 |

```json
{
  "weekLabel": "W1",
  "title": "세션 제목",
  "status": "SCHEDULED",
  "startedAt": "2025-05-17T12:00:00Z",
  "speakerIds": [10]
}
```

**Response `201 Created`**
```json
{
  "id": 8
}
```

---

### 1-4. 세션 수정

```
PUT /session-board/{generationNumber}/sessions/{sessionId}
```

**Request Body** — 1-3과 동일 구조 (변경 필드만 포함 가능)

**Response `200 OK`**
```json
{
  "id": 8,
  "updatedAt": "2025-05-17T13:00:00Z"
}
```

---

### 1-5. 세션 삭제

```
DELETE /session-board/{generationNumber}/sessions/{sessionId}
```

**Response `204 No Content`**

---

## 2. 세션 노트 (Session Note) _(미구현)_

### 2-1. 노트 목록 조회

```
GET /session-board/{generationNumber}/sessions/{sessionId}/notes
```

| Query | Type | 필수 | 설명 |
|-------|------|------|------|
| `q` | `string` | 아니오 | 전문 검색 (body) |

**Response `200 OK`**
```json
{
  "notes": [
    {
      "id": 1,
      "author": { "id": 10, "name": "김지현", "initial": "김지" },
      "body": "SERIALIZABLE 격리 수준은 성능 오버헤드가 크므로...",
      "links": [
        { "label": "MySQL 공식 문서", "url": "https://...", "order": 0 },
        { "label": "Baeldung — Transaction Isolation", "url": "https://...", "order": 1 }
      ],
      "createdAt": "2025-05-15T09:30:00Z"
    }
  ]
}
```

---

### 2-2. 노트 작성

```
POST /session-board/{generationNumber}/sessions/{sessionId}/notes
```

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `body` | `string` | 예 | — |
| `links` | `object[]` | 아니오 | `{ label, url, order }` 배열 |

```json
{
  "body": "노트 본문 내용",
  "links": [
    { "label": "참고 문서", "url": "https://...", "order": 0 }
  ]
}
```

**Response `201 Created`** — 생성된 노트 객체 (2-1 배열 아이템과 동일 구조)

---

### 2-3. 노트 수정

```
PUT /session-board/{generationNumber}/sessions/{sessionId}/notes/{noteId}
```

**Request Body** — 2-2와 동일 구조

**Response `200 OK`** — 수정된 노트 객체

---

### 2-4. 노트 삭제

```
DELETE /session-board/{generationNumber}/sessions/{sessionId}/notes/{noteId}
```

**Response `204 No Content`**

---

## 3. 세션 자료 (Resource) _(미구현)_

### 3-1. 자료 목록 조회

```
GET /session-board/{generationNumber}/sessions/{sessionId}/resources
```

| Query | Type | 필수 | 설명 |
|-------|------|------|------|
| `type` | `ResourceType` | 아니오 | 종류 필터 |

**Response `200 OK`**
```json
{
  "resources": [
    {
      "id": 1,
      "type": "SLIDE",
      "name": "3주차_트랜잭션_정리.pdf",
      "uploader": { "id": 10, "name": "김지현", "initial": "김지" },
      "sizeLabel": "2.4 MB",
      "visibility": "MEMBER",
      "url": "https://s3.amazonaws.com/...",
      "uploadedAt": "2025-05-15T09:00:00Z"
    }
  ]
}
```

---

### 3-2. 자료 업로드 (파일)

```
POST /session-board/{generationNumber}/sessions/{sessionId}/resources
Content-Type: multipart/form-data
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| `file` | `File` | 예 | 업로드 파일 (PPT, PDF, ZIP, 코드 파일 등) |
| `type` | `ResourceType` | 예 | 자료 종류 |
| `name` | `string` | 아니오 | 표시 이름 (생략 시 파일명 사용) |
| `visibility` | `ResourceVisibility` | 아니오 | 기본값 `MEMBER` |

**Response `201 Created`** — 생성된 자료 객체 (3-1 배열 아이템과 동일 구조)

---

### 3-3. 자료 등록 (링크)

```
POST /session-board/{generationNumber}/sessions/{sessionId}/resources
Content-Type: application/json
```

```json
{
  "type": "LINK",
  "name": "Baeldung — @Transactional 가이드",
  "url": "https://www.baeldung.com/transaction-configuration-with-jpa-and-spring",
  "visibility": "MEMBER"
}
```

**Response `201 Created`** — 생성된 자료 객체

---

### 3-4. 자료 삭제

```
DELETE /session-board/{generationNumber}/sessions/{sessionId}/resources/{resourceId}
```

**Response `204 No Content`**

---

## 4. 세션 회고 (Retro) _(미구현)_

### 4-1. 회고 목록 조회

```
GET /session-board/{generationNumber}/sessions/{sessionId}/retros
```

**Response `200 OK`**
```json
{
  "averageRating": 4.6,
  "retros": [
    {
      "id": 1,
      "author": { "id": 10, "name": "김지현", "initial": "김지" },
      "rating": 5,
      "body": "트랜잭션 격리 수준을 이론으로만 알고 있었는데...",
      "createdAt": "2025-05-15T10:00:00Z"
    }
  ]
}
```

---

### 4-2. 회고 작성

```
POST /session-board/{generationNumber}/sessions/{sessionId}/retros
```

> 1인 1회만 작성 가능. 이미 작성한 경우 `409 Conflict` 반환.

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `rating` | `number` | 예 | 1 ~ 5 |
| `body` | `string` | 예 | — |

```json
{
  "rating": 5,
  "body": "회고 내용"
}
```

**Response `201 Created`** — 생성된 회고 객체 (4-1 배열 아이템과 동일 구조)

---

### 4-3. 회고 수정

```
PUT /session-board/{generationNumber}/sessions/{sessionId}/retros/{retroId}
```

**Request Body** — 4-2와 동일 구조

**Response `200 OK`** — 수정된 회고 객체

---

## 엔드포인트 요약

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `GET` | `/session-board/{generationNumber}/sessions` | 세션 목록 |
| `POST` | `/session-board/{generationNumber}/sessions` | 세션 생성 |
| `GET` | `/session-board/{generationNumber}/sessions/{sessionId}` | 세션 단건 조회 |
| `PUT` | `/session-board/{generationNumber}/sessions/{sessionId}` | 세션 수정 |
| `DELETE` | `/session-board/{generationNumber}/sessions/{sessionId}` | 세션 삭제 |
| `GET` | `/session-board/{generationNumber}/sessions/{sessionId}/notes` | 노트 목록 _(미구현)_ |
| `POST` | `/session-board/{generationNumber}/sessions/{sessionId}/notes` | 노트 작성 _(미구현)_ |
| `PUT` | `/session-board/{generationNumber}/sessions/{sessionId}/notes/{noteId}` | 노트 수정 _(미구현)_ |
| `DELETE` | `/session-board/{generationNumber}/sessions/{sessionId}/notes/{noteId}` | 노트 삭제 _(미구현)_ |
| `GET` | `/session-board/{generationNumber}/sessions/{sessionId}/resources` | 자료 목록 _(미구현)_ |
| `POST` | `/session-board/{generationNumber}/sessions/{sessionId}/resources` | 자료 업로드/등록 _(미구현)_ |
| `DELETE` | `/session-board/{generationNumber}/sessions/{sessionId}/resources/{resourceId}` | 자료 삭제 _(미구현)_ |
| `GET` | `/session-board/{generationNumber}/sessions/{sessionId}/retros` | 회고 목록 _(미구현)_ |
| `POST` | `/session-board/{generationNumber}/sessions/{sessionId}/retros` | 회고 작성 _(미구현)_ |
| `PUT` | `/session-board/{generationNumber}/sessions/{sessionId}/retros/{retroId}` | 회고 수정 _(미구현)_ |