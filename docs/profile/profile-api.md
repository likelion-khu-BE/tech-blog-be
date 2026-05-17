# Profile API 정의서

## 개요

| 항목 | 내용 |
|------|------|
| Base URL | `/api` |
| 인증 | `Authorization: Bearer {accessToken}` (모든 쓰기 요청 필수) |
| Content-Type | `application/json` |
| 날짜 형식 | ISO 8601 (`2025-05-18T10:00:00Z`) |

---

## 담당 분리

| 섹션 | 담당 | 테이블 |
|------|------|--------|
| 1. 멤버 | 세인 | `member` |
| 2. 기수 | 세인 | `generation`, `member_generation` |
| 3. 기술 스택 | 시현 | `tech_stack` |
| 4. 멤버 기술스택 | 시현 | `member_tech_stack` |
| 5. 팀 | 시현 | `team_profile`, `team_member`, `team_member_role`, `team_tech_stack`, `team_image` |
| 6. 활동 기록 | 근엽 | `activity` |

---

## 공통 타입

### SessionType
```
"backend" | "frontend" | "design" | "ai" | "pm" | "etc"
```

### GenerationRole
```
"member" | "operating"
```

### RoleInTeam
```
"backend" | "frontend" | "design" | "ai" | "pm" | "infra" | "etc"
```

### TeamMemberStatus
```
"accepted" | "left" | "kicked"
```
> `pending`, `rejected`는 초대 코드 방식에서 사용하지 않음

### TechStackCategory
```
"language" | "framework" | "ai" | "design" | "tool" | "infra" | "etc"
```

### ActivityType
```
"blog_post" | "blog_comment" | "blog_post_like" | "blog_post_like_received" |
"qna_question" | "qna_answer" | "qna_accepted" | "qna_answer_upvote" |
"qna_answer_downvote" | "qna_comment" |
"session_speak" | "session_event_post" | "session_event_comment" |
"session_event_post_like" | "session_event_post_like_received"
```

> 좋아요는 양방향 — 누른 사람(`*_like`) / 받은 사람(`*_like_received`) 별도.
> Q&A 답변 vote는 참여 자체로 활동(+1) — upvote/downvote 무관. 답변 받는 측엔 활동 X (채택 시스템이 별도 보상).

### ContributionPeriodType
```
"month" | "three_month" | "year" | "all"
```

### 공통 에러 응답
```json
{
  "status": 404,
  "message": "멤버를 찾을 수 없습니다."
}
```

| HTTP 상태 | 의미 |
|-----------|------|
| `400` | 요청 값 오류 |
| `401` | 인증 토큰 없음 또는 만료 |
| `403` | 권한 없음 (본인 또는 팀장이 아님) |
| `404` | 리소스 없음 |
| `409` | 중복 (이미 해당 팀에 가입됨 등) |

---

## 1. 멤버 (Members)
> **담당: 세인**

### 1-1. 내 프로필 조회

```
GET /profile/members/me
```

> 로그인한 유저 본인의 프로필을 조회한다.

**Response `200 OK`**
```json
{
  "id": 1,
  "name": "홍길동",
  "department": "컴퓨터공학과",
  "sessionType": "backend",
  "profileImageUrl": "https://...",
  "githubUrl": "https://github.com/honggildong",
  "displayedEmail": "gildong@example.com",
  "intro": "백엔드 개발자를 꿈꿉니다.",
  "linksJson": "{\"notion\": \"https://...\"}",
  "techStacks": [
    { "techStackId": 1, "name": "Java", "category": "language", "logoUrl": "https://...", "proficiency": 4 }
  ],
  "createdAt": "2025-03-01T09:00:00Z",
  "updatedAt": "2025-05-01T12:00:00Z"
}
```

---

### 1-2. 내 프로필 수정

```
PATCH /profile/members/me
```

**Request Body**
```json
{
  "name": "홍길동",
  "department": "컴퓨터공학과",
  "sessionType": "backend",
  "profileImageUrl": "https://...",
  "githubUrl": "https://github.com/honggildong",
  "displayedEmail": "gildong@example.com",
  "intro": "백엔드 개발자를 꿈꿉니다.",
  "linksJson": "{\"notion\": \"https://...\"}"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `name` | `string` | 예 | — |
| `department` | `string` | 아니오 | — |
| `sessionType` | `SessionType` | 예 | — |
| `profileImageUrl` | `string` | 아니오 | — |
| `githubUrl` | `string` | 아니오 | — |
| `displayedEmail` | `string` | 아니오 | — |
| `intro` | `string` | 아니오 | — |
| `linksJson` | `string` | 아니오 | JSON 문자열 |

**Response `200 OK`**
```json
{
  "id": 1,
  "name": "홍길동",
  "updatedAt": "2025-05-07T10:00:00Z"
}
```

---

### 1-3. 멤버 목록 조회

```
GET /profile/members
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `generationNumber` | `number` | 아니오 | — | 특정 기수 필터 |
| `sessionType` | `SessionType` | 아니오 | — | 세션 트랙 필터 |

**Response `200 OK`**
```json
[
  {
    "id": 1,
    "name": "홍길동",
    "department": "컴퓨터공학과",
    "sessionType": "backend",
    "profileImageUrl": "https://...",
    "intro": "백엔드 개발자를 꿈꿉니다."
  }
]
```

---

### 1-4. 멤버 상세 조회

```
GET /profile/members/{memberId}
```

**Response `200 OK`**
```json
{
  "id": 1,
  "name": "홍길동",
  "department": "컴퓨터공학과",
  "sessionType": "backend",
  "profileImageUrl": "https://...",
  "githubUrl": "https://github.com/honggildong",
  "displayedEmail": "gildong@example.com",
  "intro": "백엔드 개발자를 꿉니다.",
  "linksJson": "{\"notion\": \"https://...\"}",
  "techStacks": [
    { "techStackId": 1, "name": "Java", "category": "language", "logoUrl": "https://...", "proficiency": 4 }
  ],
  "generations": [
    { "generationNumber": 13, "roleInGen": "member" }
  ],
  "createdAt": "2025-03-01T09:00:00Z"
}
```

---

### 1-5. 내가 속한 팀 목록 조회

```
GET /profile/members/me/teams
```

> 로그인한 유저가 `status = accepted`로 가입된 팀 목록을 반환한다.

**Response `200 OK`**
```json
[
  {
    "id": 1,
    "name": "헬스케어팀",
    "description": "건강 관리 앱을 만드는 팀입니다.",
    "generation": { "number": 13 },
    "techStacks": [
      { "id": 1, "name": "Java", "category": "language", "logoUrl": "https://..." }
    ],
    "isLead": true,
    "roles": ["backend"],
    "thumbUrl": "https://..."
  }
]
```

| 필드 | 설명 |
|------|------|
| `isLead` | 해당 팀에서 내가 팀장인지 여부 |
| `roles` | 해당 팀에서 내가 맡은 역할 목록 |

---

## 2. 기수 (Generations)
> **담당: 세인**

### 2-1. 기수 목록 조회

```
GET /profile/generations
```

**Response `200 OK`**
```json
[
  {
    "number": 13,
    "startDate": "2025-03-01",
    "endDate": null,
    "isCurrent": true
  }
]
```

---

### 2-2. 기수 생성 (관리자)

```
POST /profile/generations
```

**Request Body**
```json
{
  "number": 13,
  "startDate": "2025-03-01",
  "endDate": null,
  "isCurrent": true
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `number` | `number` | 예 | 중복 불가 |
| `startDate` | `string` | 예 | `yyyy-MM-dd` |
| `endDate` | `string` | 아니오 | `yyyy-MM-dd`, 진행 중이면 null |
| `isCurrent` | `boolean` | 아니오 | `true`는 동시에 1개만 가능 |

**Response `201 Created`**
```json
{ "number": 13 }
```

---

### 2-3. 기수 상세 조회

```
GET /profile/generations/{generationId}
```

**Response `200 OK`**
```json
{
  "number": 13,
  "startDate": "2025-03-01",
  "endDate": null,
  "isCurrent": true,
  "createdAt": "2025-01-01T00:00:00Z"
}
```

---

### 2-4. 기수 수정 (관리자)

```
PATCH /profile/generations/{generationId}
```

**Request Body** — 2-2와 동일 구조

**Response `200 OK`**
```json
{ "number": 13 }
```

---

### 2-5. 기수 멤버 목록 조회

```
GET /profile/generations/{generationId}/members
```

**Response `200 OK`**
```json
[
  {
    "memberId": 1,
    "name": "홍길동",
    "sessionType": "backend",
    "profileImageUrl": "https://...",
    "roleInGen": "member",
    "joinedAt": "2025-03-01T09:00:00Z"
  }
]
```

---

### 2-6. 기수에 멤버 등록 (관리자)

```
POST /profile/generations/{generationId}/members
```

**Request Body**
```json
{
  "memberId": 1,
  "roleInGen": "member"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `memberId` | `number` | 예 | — |
| `roleInGen` | `GenerationRole` | 예 | — |

**Response `201 Created`**
```json
{ "id": 5 }
```

---

## 3. 기술 스택 (Tech Stacks)
> **담당: 시현**
>
> 기술 스택은 시드 데이터로 제공된다 (`tech_stack_seed.json`). 일반 사용자는 읽기만 가능하고, 관리자만 추가/수정/삭제할 수 있다.

### 3-1. 기술 스택 목록 조회

```
GET /profile/tech-stacks
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `category` | `TechStackCategory` | 아니오 | 카테고리 필터 |

**Response `200 OK`**
```json
[
  {
    "id": 1,
    "name": "Java",
    "category": "language",
    "logoUrl": "https://..."
  }
]
```

---

### 3-2. 기술 스택 등록 (관리자)

```
POST /profile/tech-stacks
```

**Request Body**
```json
{
  "name": "Bun",
  "category": "framework",
  "logoUrl": "https://..."
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `name` | `string` | 예 | 중복 불가 |
| `category` | `TechStackCategory` | 예 | — |
| `logoUrl` | `string` | 아니오 | — |

**Response `201 Created`**
```json
{ "id": 120 }
```

---

### 3-3. 기술 스택 수정 (관리자)

```
PATCH /profile/tech-stacks/{techStackId}
```

**Request Body** — 3-2와 동일 구조

**Response `200 OK`**
```json
{ "id": 1 }
```

---

### 3-4. 기술 스택 삭제 (관리자)

```
DELETE /profile/tech-stacks/{techStackId}
```

**Response `204 No Content`**

---

## 4. 멤버 기술 스택 (Member Tech Stacks)
> **담당: 시현**

### 4-1. 멤버 기술 스택 조회

```
GET /profile/members/{memberId}/tech-stacks
```

**Response `200 OK`**
```json
[
  {
    "techStackId": 1,
    "name": "Java",
    "category": "language",
    "logoUrl": "https://...",
    "proficiency": 4
  }
]
```

---

### 4-2. 내 기술 스택 수정

```
PUT /profile/members/me/tech-stacks
```

> 기존 목록을 전체 교체한다. 빈 배열 `[]`을 보내면 전체 삭제.

**Request Body**
```json
[
  { "techStackId": 1, "proficiency": 4 },
  { "techStackId": 2, "proficiency": 3 }
]
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `techStackId` | `number` | 예 | 존재하는 tech_stack id |
| `proficiency` | `number` | 아니오 | `1` ~ `5`, 미입력 시 null |

**Response `200 OK`**
```json
[
  {
    "techStackId": 1,
    "name": "Java",
    "category": "language",
    "logoUrl": "https://...",
    "proficiency": 4
  }
]
```

---

## 5. 팀 (Teams)
> **담당: 시현**
>
### 5-1. 팀 목록 조회

```
GET /profile/teams
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `generationNumber` | `number` | 아니오 | 기수 필터 |

**Response `200 OK`**
```json
[
  {
    "id": 1,
    "name": "헬스케어팀",
    "description": "건강 관리 앱을 만드는 팀입니다.",
    "generation": { "number": 13 },
    "techStacks": [
      { "id": 1, "name": "Java", "category": "language", "logoUrl": "https://..." }
    ],
    "memberCount": 4,
    "thumbUrl": "https://..."
  }
]
```

| 필드 | 설명 |
|------|------|
| `thumbUrl` | 팀 이미지가 1개 이상이면 첫 번째 이미지 URL, 없으면 `null` |

---

### 5-2. 팀 생성

```
POST /profile/teams
```

> 팀을 생성한 사람이 자동으로 팀장(`isLead = true`, `status = accepted`)이 된다.

**Request Body**
```json
{
  "name": "헬스케어팀",
  "description": "건강 관리 앱을 만드는 팀입니다.",
  "projectUrl": "https://...",
  "githubUrl": "https://github.com/...",
  "generationNumber": 13,
  "imageUrls": ["https://...", "https://..."],
  "techStackIds": [1, 2, 3]
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `name` | `string` | 예 | — |
| `description` | `string` | 아니오 | — |
| `projectUrl` | `string` | 아니오 | — |
| `githubUrl` | `string` | 아니오 | — |
| `generationNumber` | `number` | 아니오 | 존재하는 generation number |
| `imageUrls` | `string[]` | 아니오 | — |
| `techStackIds` | `number[]` | 아니오 | 존재하는 tech_stack id 목록 |

**Response `201 Created`**
```json
{
  "id": 1,
  "inviteCode": "A1B2C3",
  "inviteCodeExpiresAt": "2025-05-10T10:00:00Z"
}
```

---

### 5-3. 팀 상세 조회

```
GET /profile/teams/{teamId}
```

**Response `200 OK`**
```json
{
  "id": 1,
  "name": "헬스케어팀",
  "description": "건강 관리 앱을 만드는 팀입니다.",
  "projectUrl": "https://...",
  "githubUrl": "https://github.com/...",
  "generation": { "number": 13 },
  "imageUrls": ["https://...", "https://..."],
  "techStacks": [
    { "id": 1, "name": "Java", "category": "language", "logoUrl": "https://..." }
  ],
  "members": [
    {
      "memberId": 1,
      "name": "홍길동",
      "sessionType": "backend",
      "profileImageUrl": "https://...",
      "isLead": true,
      "roles": ["backend", "infra"]
    }
  ],
  "inviteCode": "A1B2C3D4",
  "inviteCodeExpiresAt": "2025-05-10T10:00:00Z",
  "updatedAt": "2025-05-07T10:00:00Z"
}
```

> `inviteCode` / `inviteCodeExpiresAt` 는 요청자가 해당 팀의 팀원(`accepted`)인 경우에만 반환되며, 그 외에는 `null`.

---

### 5-4. 팀 정보 수정 (팀장만)

```
PATCH /profile/teams/{teamId}
```

> 팀장만 호출 가능. 타인 요청 시 `403`.

**Request Body**
```json
{
  "name": "헬스케어팀 v2",
  "description": "...",
  "projectUrl": "https://...",
  "githubUrl": "https://github.com/...",
  "generationNumber": 13,
  "imageUrls": ["https://..."],
  "techStackIds": [1, 3]
}
```

> `imageUrls` / `techStackIds` — `null`이면 기존 유지, 빈 배열 `[]`이면 전체 삭제

**Response `200 OK`**
```json
{ "id": 1, "updatedAt": "2025-05-07T10:00:00Z" }
```

---

### 5-5. 팀 삭제 (팀장만)

```
DELETE /profile/teams/{teamId}
```

> 팀장만 호출 가능. 삭제 시 `team_member`, `team_member_role`, `team_tech_stack`, `team_image` 모두 CASCADE 삭제.

**Response `204 No Content`**

---

### 5-6. 초대 코드 조회 (팀장만)

```
GET /profile/teams/{teamId}/invite-code
```

> 팀장만 조회 가능. 팀원 초대 시 이 코드를 공유한다.

**Response `200 OK`**
```json
{
  "inviteCode": "A1B2C3",
  "inviteCodeExpiresAt": "2025-05-10T10:00:00Z"
}
```

---

### 5-7. 초대 코드 재생성 (팀장만)

```
POST /profile/teams/{teamId}/invite-code/regenerate
```

> 기존 코드를 무효화하고 새 코드를 발급한다. 팀장만 호출 가능.
> 재생성 시 만료 시각은 현재 시각 + 3일로 갱신된다.

**Response `200 OK`**
```json
{
  "inviteCode": "X9Y8Z7",
  "inviteCodeExpiresAt": "2025-05-10T10:00:00Z"
}
```

---

### 5-8. 초대 코드로 팀 가입

```
POST /profile/teams/join
```

> 로그인한 유저가 초대 코드를 입력해 팀에 가입한다. 가입 즉시 `status = accepted`.

**Request Body**
```json
{ "inviteCode": "A1B2C3" }
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `inviteCode` | `string` | 예 | — |

**Response `200 OK`**
```json
{
  "teamId": 1,
  "teamName": "헬스케어팀"
}
```

| 에러 | HTTP | 메시지 |
|------|------|--------|
| 존재하지 않는 코드 | `404` | 유효하지 않은 초대 코드입니다 |
| 만료된 코드 | `400` | 만료된 초대 코드입니다 |
| 이미 가입된 팀 | `409` | 이미 가입된 팀입니다 |

---

### 5-9. 팀장 양도 (팀장만)

```
PATCH /profile/teams/{teamId}/lead
```

> 팀장만 호출 가능. 기존 팀장의 `isLead`는 `false`, 새 팀장의 `isLead`는 `true`로 변경된다.
> 대상 멤버는 `status = accepted` 상태여야 한다 — `400`.

**Request Body**
```json
{ "memberId": 3 }
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `memberId` | `number` | 예 | 해당 팀의 accepted 팀원 |

**Response `200 OK`**
```json
{
  "teamId": 1,
  "newLeadMemberId": 3
}
```

---

### 5-10. 팀원 역할 수정 (팀장만)

```
PUT /profile/teams/{teamId}/members/{memberId}/roles
```

> 팀장만 호출 가능. 기존 역할 목록을 전체 교체한다.

**Request Body**
```json
{ "roles": ["backend", "infra"] }
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `roles` | `RoleInTeam[]` | 예 | 1개 이상 |

**Response `200 OK`**
```json
{ "memberId": 2, "roles": ["backend", "infra"] }
```

---

### 5-10. 팀원 강퇴 (팀장만)

```
DELETE /profile/teams/{teamId}/members/{memberId}
```

> 팀장만 호출 가능. 강퇴된 팀원의 `status`는 `kicked`로 변경된다.
> 팀장 본인은 강퇴할 수 없다 — `400`.

**Response `204 No Content`**

---

### 5-11. 팀 탈퇴

```
DELETE /profile/teams/{teamId}/members/me
```

> 팀장은 탈퇴 불가 — `400`. 팀을 해산하려면 팀 삭제(5-5)를 사용.
> 탈퇴 시 `status`는 `left`로 변경된다.

**Response `204 No Content`**

---

## 6. 활동 기록 (Activities)
> **담당: 근엽**
>
> 활동 기록은 blog / qna / sessionboard 도메인에서 이벤트 방식으로 자동 적재된다. 이 섹션은 **읽기 전용** API만 제공한다.

### 점수 기준

| ActivityType | 점수 |
|---|---|
| `blog_post` | +30 |
| `blog_comment` | +3 |
| `blog_post_like` | +1 |
| `blog_post_like_received` | +1 |
| `qna_question` | +10 |
| `qna_answer` | +10 |
| `qna_accepted` | +25 |
| `qna_answer_upvote` | +1 |
| `qna_answer_downvote` | +1 |
| `qna_comment` | +3 |
| `session_speak` | +50 |
| `session_event_post` | +30 |
| `session_event_comment` | +3 |
| `session_event_post_like` | +1 |
| `session_event_post_like_received` | +1 |

---

### 6-1. 멤버 활동 목록 조회

```
GET /profile/members/{memberId}/activities
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `type` | `ActivityType` | 아니오 | — | 활동 종류 필터 |
| `page` | `number` | 아니오 | `0` | 페이지 번호 (0-based) |
| `size` | `number` | 아니오 | `20` | 페이지 크기 |

**Response `200 OK`**
```json
{
  "content": [
    {
      "id": 1,
      "type": "blog_post",
      "referenceId": 42,
      "score": 30,
      "createdAt": "2025-05-01T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 35,
  "totalPages": 2,
  "hasNext": true
}
```

---

### 6-2. 멤버 기여도 요약

```
GET /profile/members/{memberId}/contributions
```

> `activity` 테이블에서 기간별 점수를 집계해 반환한다.

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `period` | `ContributionPeriodType` | 아니오 | `all` | 집계 기간 |

**Response `200 OK`**
```json
{
  "memberId": 1,
  "name": "홍길동",
  "period": "month",
  "totalScore": 214,
  "breakdown": {
    "blog_post": 60,
    "blog_comment": 3,
    "blog_post_like": 3,
    "blog_post_like_received": 12,
    "qna_question": 10,
    "qna_answer": 20,
    "qna_accepted": 50,
    "qna_comment": 3,
    "session_speak": 50,
    "session_event_post": 0,
    "session_event_comment": 3,
    "session_event_post_like": 0,
    "session_event_post_like_received": 0
  }
}
```

---

### 6-3. 기여도 랭킹

```
GET /profile/contributions/ranking
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `period` | `ContributionPeriodType` | 아니오 | `all` | 집계 기간 |
| `generationNumber` | `number` | 아니오 | — | 특정 기수 필터 |
| `limit` | `number` | 아니오 | `10` | 반환할 순위 수 |

**Response `200 OK`**
```json
[
  {
    "rank": 1,
    "memberId": 1,
    "name": "홍길동",
    "profileImageUrl": "https://...",
    "totalScore": 120
  },
  {
    "rank": 2,
    "memberId": 3,
    "name": "김지수",
    "profileImageUrl": "https://...",
    "totalScore": 95
  }
]
```

---

## 엔드포인트 요약

| 구현  | 메서드 | 경로 | 설명 | 담당 |
|-----|--------|------|------|------|
| [x] | `GET` | `/profile/members/me` | 내 프로필 조회 | 세인 |
| [x] | `PATCH` | `/profile/members/me` | 내 프로필 수정 | 세인 |
| [ ] | `GET` | `/profile/members/me/teams` | 내가 속한 팀 목록 | 시현 |
| [x] | `GET` | `/profile/members` | 멤버 목록 | 세인 |
| [x] | `GET` | `/profile/members/{memberId}` | 멤버 상세 | 세인 |
| [x] | `GET` | `/profile/generations` | 기수 목록 | 세인 |
| [x] | `POST` | `/profile/generations` | 기수 생성 (관리자) | 세인 |
| [x] | `GET` | `/profile/generations/{generationNumber}` | 기수 상세 | 세인 |
| [x] | `PATCH` | `/profile/generations/{generationNumber}` | 기수 수정 (관리자) | 세인 |
| [x] | `GET` | `/profile/generations/{generationNumber}/members` | 기수 멤버 목록 | 세인 |
| [x] | `POST` | `/profile/generations/{generationNumber}/members` | 기수에 멤버 등록 (관리자) | 세인 |
| ✅   | `GET` | `/profile/tech-stacks` | 기술 스택 목록 | 시현 |
| [ ] | `POST` | `/profile/tech-stacks` | 기술 스택 등록 (관리자) | 시현 |
| [ ] | `PATCH` | `/profile/tech-stacks/{techStackId}` | 기술 스택 수정 (관리자) | 시현 |
| [ ] | `DELETE` | `/profile/tech-stacks/{techStackId}` | 기술 스택 삭제 (관리자) | 시현 |
| [ ] | `GET` | `/profile/members/{memberId}/tech-stacks` | 멤버 기술 스택 조회 | 시현 |
| [ ] | `PUT` | `/profile/members/me/tech-stacks` | 내 기술 스택 수정 | 시현 |
| ✅   | `GET` | `/profile/teams` | 팀 목록 | 시현 |
| ✅   | `POST` | `/profile/teams` | 팀 생성 | 시현 |
| ✅   | `GET` | `/profile/teams/{teamId}` | 팀 상세 | 시현 |
| [x] | `PATCH` | `/profile/teams/{teamId}` | 팀 정보 수정 (팀장) | 시현 |
| [x] | `DELETE` | `/profile/teams/{teamId}` | 팀 삭제 (팀장) | 시현 |
| [ ] | `GET` | `/profile/teams/{teamId}/invite-code` | 초대 코드 조회 (팀장) | 시현 |
| [ ] | `POST` | `/profile/teams/{teamId}/invite-code/regenerate` | 초대 코드 재생성 (팀장) | 시현 |
| [ ] | `POST` | `/profile/teams/join` | 초대 코드로 팀 가입 | 시현 |
| [ ] | `PATCH` | `/profile/teams/{teamId}/lead` | 팀장 양도 (팀장) | 시현 |
| [ ] | `PUT` | `/profile/teams/{teamId}/members/{memberId}/roles` | 팀원 역할 수정 (팀장) | 시현 |
| [ ] | `DELETE` | `/profile/teams/{teamId}/members/{memberId}` | 팀원 강퇴 (팀장) | 시현 |
| [ ] | `DELETE` | `/profile/teams/{teamId}/members/me` | 팀 탈퇴 | 시현 |
| [ ] | `GET` | `/profile/members/{memberId}/activities` | 멤버 활동 목록 | 근엽 |
| [ ] | `GET` | `/profile/members/{memberId}/contributions` | 멤버 기여도 요약 | 근엽 |
| [ ] | `GET` | `/profile/contributions/ranking` | 기여도 랭킹 | 근엽 |