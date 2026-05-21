# 활동 기록 API 정의서

## 개요

| 항목 | 내용 |
|------|------|
| Base URL | `/api` |
| 인증 | `Authorization: Bearer {accessToken}` (모든 쓰기 요청 필수) |
| Content-Type | `application/json` |
| 날짜 형식 | ISO 8601 (`2025-05-18T10:00:00Z`) |

---

## 공통 타입

### EventType
```
"hackathon" | "ideathon" | "workshop" | "project" | "meetup"
```

### EventPostStatus
```
"DRAFT" | "PUBLISHED" | "HIDDEN"
```

### Author
```json
{
  "id": 1,
  "name": "김지현",
  "initial": "김지"
}
```

### 공통 에러 응답
```json
{
  "status": 404,
  "message": "해당 게시글을 찾을 수 없습니다."
}
```

| HTTP 상태 | 의미 |
|-----------|------|
| `400` | 요청 값 오류 |
| `401` | 인증 토큰 없음 또는 만료 |
| `403` | 본인 게시글/댓글이 아님 |
| `404` | 리소스 없음 |

---

## 1. 활동 기록 (EventPosts)

### 1-1. 목록 조회

```
GET /session-board/{generationNumber}/event-posts
```

> `status = PUBLISHED` 인 게시글만 반환한다.

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `type` | `EventType` | 아니오 | — | 카테고리 필터 |
| `page` | `number` | 아니오 | `0` | 페이지 번호 (0-based) |
| `size` | `number` | 아니오 | `20` | 페이지 크기 |

**Response `200 OK`**
```json
{
  "content": [
    {
      "id": 1,
      "type": "hackathon",
      "title": "24시간 해커톤 후기 — 우리가 만든 AI 식단 관리 앱",
      "author": { "id": 1, "name": "김지현", "initial": "김지" },
      "createdAt": "2025-05-18T10:00:00Z",
      "excerpt": "지난 주말, 팀원 4명이 모여 24시간 동안...",
      "tags": ["Spring Boot", "React", "GPT API"],
      "likeCount": 12,
      "commentCount": 5,
      "hasThumb": true,
      "thumbUrl": "https://s3.amazonaws.com/..."
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "hasNext": true
}
```

| 필드 | 설명 |
|------|------|
| `excerpt` | `body` 앞부분 최대 100자 미리보기 |
| `hasThumb` | 첨부 이미지가 1개 이상이면 `true` |
| `thumbUrl` | `hasThumb = true`일 때 `order = 0` 이미지의 S3 presigned URL, 없으면 `null` |
| `commentCount` | 댓글 + 답글 합산 수 |

---

### 1-2. 상세 조회

```
GET /session-board/{generationNumber}/event-posts/{eventPostId}
```

**Response `200 OK`**
```json
{
  "id": 1,
  "type": "HACKATHON",
  "status": "PUBLISHED",
  "title": "24시간 해커톤 후기 — 우리가 만든 AI 식단 관리 앱",
  "author": { "id": 1, "name": "김지현", "initial": "김지" },
  "createdAt": "2025-05-18T10:00:00Z",
  "updatedAt": null,
  "excerpt": "지난 주말, 팀원 4명이 모여 24시간 동안...",
  "body": "<p>지난 주말...</p>",
  "tags": ["Spring Boot", "React", "GPT API"],
  "images": [
    { "order": 0, "url": "https://s3.amazonaws.com/..." },
    { "order": 1, "url": "https://s3.amazonaws.com/..." }
  ],
  "likeCount": 12,
  "likedByMe": true,
  "commentCount": 5
}
```

| 필드 | 설명 |
|------|------|
| `type` | 대문자 반환 (`"HACKATHON"` 등). 목록 조회의 소문자와 다름 |
| `images[].url` | S3 URL. 유효 기간이 있으므로 캐시 금지 |
| `images[].order` | 0-based 정렬 순서 |
| `likedByMe` | 현재 항상 `false` (로그인 여부 무관, 미구현) |

---

### 1-3. 작성

```
POST /session-board/{generationNumber}/event-posts
```

**Request Body**
```json
{
  "type": "hackathon",
  "title": "24시간 해커톤 후기",
  "body": "<p>지난 주말...</p>",
  "tags": ["Spring Boot", "React"]
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `type` | `EventType` | 예 | — |
| `title` | `string` | 예 | 최대 100자 |
| `body` | `string` | 예 | 최대 10,000자 |
| `tags` | `string[]` | 아니오 | 태그당 최대 20자, 최대 10개 |

> 생성된 게시글의 `status`는 `PUBLISHED`로 고정된다.

**Response `201 Created`**
```json
{
  "id": 6
}
```

---

### 1-4. 수정

```
PUT /session-board/{generationNumber}/event-posts/{eventPostId}
```

> 본인 작성 글만 수정 가능. 타인 요청 시 `403`.

**Request Body** — 1-3과 동일 구조

**Response `200 OK`**
```json
{
  "id": 6,
  "updatedAt": "2025-05-19T08:30:00Z"
}
```

---

### 1-5. 삭제

```
DELETE /session-board/{generationNumber}/event-posts/{eventPostId}
```

> 본인 작성 글만 삭제 가능.

**Response `204 No Content`**

---

### 1-6. 좋아요 토글 _(미구현)_

```
POST /session-board/{generationNumber}/event-posts/{eventPostId}/like
```

> 이미 좋아요된 상태에서 호출하면 취소.

**Response `200 OK`**
```json
{
  "likedByMe": true,
  "likeCount": 13
}
```

---

## 2. 댓글 (Comments) _(미구현)_

### 2-1. 목록 조회

```
GET /session-board/{generationNumber}/event-posts/{eventPostId}/comments
```

> 최상위 댓글과 각 댓글의 답글을 함께 반환.

**Response `200 OK`**
```json
[
  {
    "id": 1,
    "author": { "id": 2, "name": "박서윤", "initial": "박서" },
    "content": "고생하셨어요! 새벽 3시 CORS 저도 당해본 적 있어요 ㅠㅠ",
    "createdAt": "2025-05-18T12:00:00Z",
    "updatedAt": null,
    "replies": [
      {
        "id": 10,
        "author": { "id": 1, "name": "김지현", "initial": "김지" },
        "content": "맞아요 ㅋㅋ 다음엔 CORS 먼저 확인하겠습니다",
        "createdAt": "2025-05-18T12:30:00Z",
        "updatedAt": null
      }
    ]
  }
]
```

---

### 2-2. 댓글 작성

```
POST /session-board/{generationNumber}/event-posts/{eventPostId}/comments
```

**Request Body**
```json
{
  "content": "고생하셨어요!"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `content` | `string` | 예 | 최대 500자 |

**Response `201 Created`**
```json
{
  "id": 8,
  "author": { "id": 2, "name": "박서윤", "initial": "박서" },
  "content": "고생하셨어요!",
  "createdAt": "2025-05-18T12:00:00Z",
  "updatedAt": null,
  "replies": []
}
```

---

### 2-3. 댓글 수정

```
PATCH /session-board/{generationNumber}/event-posts/{eventPostId}/comments/{commentId}
```

> 본인 댓글만 수정 가능.

**Request Body**
```json
{
  "content": "수정된 댓글 내용입니다."
}
```

**Response `200 OK`**
```json
{
  "id": 8,
  "content": "수정된 댓글 내용입니다.",
  "updatedAt": "2025-05-18T13:00:00Z"
}
```

---

### 2-4. 댓글 삭제

```
DELETE /session-board/{generationNumber}/event-posts/{eventPostId}/comments/{commentId}
```

> 본인 댓글만 삭제 가능. 삭제 시 하위 답글도 함께 삭제.

**Response `204 No Content`**

---

## 3. 답글 (Replies) _(미구현)_

### 3-1. 답글 작성

```
POST /session-board/{generationNumber}/event-posts/{eventPostId}/comments/{commentId}/replies
```

**Request Body**
```json
{
  "content": "저도 같은 경험이 있어요!"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `content` | `string` | 예 | 최대 500자 |

**Response `201 Created`**
```json
{
  "id": 20,
  "author": { "id": 3, "name": "이민준", "initial": "이민" },
  "content": "저도 같은 경험이 있어요!",
  "createdAt": "2025-05-18T14:00:00Z",
  "updatedAt": null
}
```

---

### 3-2. 답글 수정

```
PATCH /session-board/{generationNumber}/event-posts/{eventPostId}/comments/{commentId}/replies/{replyId}
```

> 본인 답글만 수정 가능.

**Request Body**
```json
{
  "content": "수정된 답글 내용입니다."
}
```

**Response `200 OK`**
```json
{
  "id": 20,
  "content": "수정된 답글 내용입니다.",
  "updatedAt": "2025-05-18T15:00:00Z"
}
```

---

### 3-3. 답글 삭제

```
DELETE /session-board/{generationNumber}/event-posts/{eventPostId}/comments/{commentId}/replies/{replyId}
```

> 본인 답글만 삭제 가능.

**Response `204 No Content`**

---

## 엔드포인트 요약

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `GET` | `/session-board/{generationNumber}/event-posts` | 활동 기록 목록 |
| `POST` | `/session-board/{generationNumber}/event-posts` | 활동 기록 작성 |
| `GET` | `/session-board/{generationNumber}/event-posts/{eventPostId}` | 활동 기록 상세 |
| `PUT` | `/session-board/{generationNumber}/event-posts/{eventPostId}` | 활동 기록 수정 |
| `DELETE` | `/session-board/{generationNumber}/event-posts/{eventPostId}` | 활동 기록 삭제 |
| `POST` | `/session-board/{generationNumber}/event-posts/{eventPostId}/like` | 좋아요 토글 _(미구현)_ |
| `GET` | `/session-board/{generationNumber}/event-posts/{eventPostId}/comments` | 댓글 목록 (답글 포함) _(미구현)_ |
| `POST` | `/session-board/{generationNumber}/event-posts/{eventPostId}/comments` | 댓글 작성 _(미구현)_ |
| `PATCH` | `/session-board/{generationNumber}/event-posts/{eventPostId}/comments/{commentId}` | 댓글 수정 _(미구현)_ |
| `DELETE` | `/session-board/{generationNumber}/event-posts/{eventPostId}/comments/{commentId}` | 댓글 삭제 _(미구현)_ |
| `POST` | `/session-board/{generationNumber}/event-posts/{eventPostId}/comments/{commentId}/replies` | 답글 작성 _(미구현)_ |
| `PATCH` | `/session-board/{generationNumber}/event-posts/{eventPostId}/comments/{commentId}/replies/{replyId}` | 답글 수정 _(미구현)_ |
| `DELETE` | `/session-board/{generationNumber}/event-posts/{eventPostId}/comments/{commentId}/replies/{replyId}` | 답글 삭제 _(미구현)_ |