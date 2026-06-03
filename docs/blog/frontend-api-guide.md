# 프론트엔드 API 연동 가이드

> **실제 백엔드 구현 기준** 문서입니다. `README.md`의 사전 합의 명세와 일부 다를 수 있으며, 이 문서가 우선합니다.
>
> 각 항목 옆의 `> 근거:` 주석은 해당 내용이 도출된 소스 파일과 라인 번호입니다.

---

## 목차

1. [기본 설정](#1-기본-설정)
2. [인증 구조](#2-인증-구조)
3. [인증 API](#3-인증-api)
4. [게시글 API](#4-게시글-api)
5. [댓글 API](#5-댓글-api)
6. [어드민 API](#6-어드민-api)
7. [유저 권한 관리 API (PRESIDENT 전용)](#7-유저-권한-관리-api-president-전용)
8. [헬스체크 API](#8-헬스체크-api)
9. [에러 처리](#9-에러-처리)
10. [공통 타입 정의](#10-공통-타입-정의)
11. [토큰 자동 갱신 구현 예시](#11-토큰-자동-갱신-구현-예시)

---

## 1. 기본 설정

### Base URL

```
개발: http://localhost:8080
```

### CORS 설정

> 근거: `src/main/java/com/study/shared/config/CorsConfig.java`

| 항목 | 값 | 근거 라인 |
|------|-----|-----------|
| Allowed Origins | `http://localhost:3000` | `:22` |
| Allowed Methods | GET, POST, PUT, DELETE, PATCH, OPTIONS | `:23` |
| Allowed Headers | `*` (전체) | `:24` |
| Credentials | `true` | `:25` |
| Max Age | `3600`초 | `:26` |

### axios 기본 설정

```js
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,       // CorsConfig.java:25 — setAllowCredentials(true)
})
```

> `fetch` 사용 시 모든 요청에 `credentials: 'include'` 옵션 추가 필요

---

## 2. 인증 구조

### 토큰 종류

> 근거: `src/main/resources/application.yml:43-44`, `src/main/java/com/study/auth/infrastructure/security/JwtProvider.java:36,45-47,63-66`

| 토큰 | 위치 | 유효기간 | 용도 |
|------|------|---------|------|
| Access Token | 응답 body → 메모리 저장 | **15분** (900,000 ms) | API 요청 시 Authorization 헤더 |
| Refresh Token | HttpOnly 쿠키 (자동 관리) | **7일** (604,800,000 ms) | Access Token 갱신 |

- Access Token 만료시간: `application.yml:43` — `jwt.access-token-expiration: 900000`
- Refresh Token 만료시간: `application.yml:44` — `jwt.refresh-token-expiration: 604800000`
- JWT 알고리즘: `JwtProvider.java:36` — `Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))`(HS256)
- Secret Key 환경변수: `application.yml:42` — `jwt.secret: ${JWT_SECRET}`

> Access Token은 `localStorage`가 아닌 **메모리(변수)**에 저장하는 것을 권장합니다.  
> Refresh Token은 브라우저 쿠키에 자동으로 저장/전송되므로 코드에서 별도 처리 불필요합니다.

### 인증이 필요한 요청

```http
Authorization: Bearer {accessToken}
```

```js
api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`
```

### 공개 엔드포인트 (인증 불필요)

> 근거: `src/main/java/com/study/shared/config/SecurityConfig.java:54-66`

```
POST /api/auth/signup       SecurityConfig.java:55
POST /api/auth/login        SecurityConfig.java:56
POST /api/auth/refresh      SecurityConfig.java:57
POST /api/auth/logout       SecurityConfig.java:58
GET  /api/health            SecurityConfig.java:59
GET  /api/health/**         SecurityConfig.java:60
GET  /api/blog/posts        SecurityConfig.java:62-63
GET  /api/blog/posts/**     SecurityConfig.java:64-65
GET  /api/blog/posts/{postId}/comments  (위 /api/blog/posts/** 에 포함)
```

나머지 모든 요청은 인증 필요:
> 근거: `SecurityConfig.java:67-68` — `.anyRequest().authenticated()`

세션 비사용(Stateless):
> 근거: `SecurityConfig.java:47` — `SessionCreationPolicy.STATELESS`

CSRF 비활성화:
> 근거: `SecurityConfig.java:46` — `.csrf(AbstractHttpConfigurer::disable)`

---

## 3. 인증 API

### 회원가입

```http
POST /api/auth/signup
```

> 근거: `src/main/java/com/study/auth/presentation/controller/AuthController.java:52`

**Request Body**

> 근거: `src/main/java/com/study/auth/presentation/dto/SignupRequest.java:8,11`

```json
{
  "email": "chanwook@khu.ac.kr",
  "password": "password123"
}
```

| 필드 | 타입 | 제약 | 근거 |
|------|------|------|------|
| `email` | String | 필수(`@NotBlank`), 이메일 형식(`@Email`) | `SignupRequest.java:8` |
| `password` | String | 필수(`@NotBlank`), 최소 8자(`@Size(min=8)`) | `SignupRequest.java:11` |

**Response** `201 Created`

> 근거: `src/main/java/com/study/auth/presentation/dto/SignupResponse.java:5`

```json
{
  "userId": 1,
  "email": "chanwook@khu.ac.kr",
  "status": "PENDING"
}
```

| 필드 | 타입 | 근거 |
|------|------|------|
| `userId` | Long | `SignupResponse.java:5` |
| `email` | String | `SignupResponse.java:5` |
| `status` | UserStatus | `SignupResponse.java:5` |

> 가입 후 상태는 `PENDING`이며, 관리자 승인(`ACTIVE`) 전까지 로그인이 차단됩니다.

---

### 로그인

```http
POST /api/auth/login
```

> 근거: `src/main/java/com/study/auth/presentation/controller/AuthController.java:58`

**Request Body**

> 근거: `src/main/java/com/study/auth/presentation/dto/LoginRequest.java:6`

```json
{
  "email": "chanwook@khu.ac.kr",
  "password": "password123"
}
```

| 필드 | 타입 | 제약 | 근거 |
|------|------|------|------|
| `email` | String | `@NotBlank`, `@Email` | `LoginRequest.java:6` |
| `password` | String | `@NotBlank` | `LoginRequest.java:6` |

**Response** `200 OK`

> 근거: `src/main/java/com/study/auth/presentation/dto/LoginResponse.java:3`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

**쿠키 자동 설정** (별도 처리 불필요)

> 근거: `AuthController.java:40,48,121-125`

```
Set-Cookie: refresh_token=...; HttpOnly; Secure; SameSite=Strict; Path=/api/auth; Max-Age=604800
```

| 쿠키 속성 | 값 | 근거 |
|-----------|-----|------|
| 쿠키명 | `refresh_token` | `AuthController.java:40` |
| `HttpOnly` | true | `AuthController.java:121` |
| `Secure` | true | `AuthController.java:122` |
| `SameSite` | `Strict` | `AuthController.java:123` |
| `Path` | `/api/auth` | `AuthController.java:48,124` |
| `Max-Age` | `604800000` ms (7일) | `AuthController.java:125` |

**로그인 후 처리 예시**

```js
const { data } = await api.post('/api/auth/login', { email, password })
// 메모리에 저장 (전역 상태 또는 변수)
setAccessToken(data.accessToken)
api.defaults.headers.common['Authorization'] = `Bearer ${data.accessToken}`
```

---

### 토큰 갱신

```http
POST /api/auth/refresh
```

> 근거: `AuthController.java:70`

> Refresh Token은 쿠키로 자동 전송됩니다 (`withCredentials: true` 필수 — `CorsConfig.java:25`).

**Response** `200 OK`

> 근거: `src/main/java/com/study/auth/presentation/dto/TokenRefreshResponse.java:3`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

새 Refresh Token 쿠키도 자동으로 갱신됩니다.

---

### 로그아웃

```http
POST /api/auth/logout
```

> 근거: `AuthController.java:84`

**Response** `204 No Content` (body 없음)

> 근거: `AuthController.java:100` — `ResponseEntity.noContent()`

```js
await api.post('/api/auth/logout')
// 메모리에서 Access Token 제거
setAccessToken(null)
delete api.defaults.headers.common['Authorization']
// Refresh Token 쿠키는 서버에서 maxAge=0으로 자동 삭제
```

---

## 4. 게시글 API

### 게시글 목록 조회

```http
GET /api/blog/posts
```

> 근거: `src/main/java/com/study/blog/presentation/post/PostController.java:36`

**Query Parameters** (모두 선택)

| 파라미터 | 타입 | 설명 | 기본값 |
|----------|------|------|--------|
| `board` | String | 게시판 필터 | - |
| `category` | String | 카테고리 필터 | - |
| `generation` | String | 기수 필터 | - |
| `authorId` | Long | 작성자 ID 필터 | - |
| `keyword` | String | 제목/태그 검색 | - |
| `page` | int | 페이지 번호 (0-indexed) | `0` |
| `size` | int | 페이지 크기 | `10` |

**Response** `200 OK` — Spring `Page<PostSummaryResponse>`

> 근거: `src/main/java/com/study/blog/application/post/dto/PostSummaryResponse.java:8-18`

```json
{
  "content": [
    {
      "id": 1,
      "title": "Spring Boot EC2 배포 자동화",
      "board": "백엔드",
      "category": "CI/CD",
      "generation": "13기",
      "status": "PUBLISHED",
      "authorId": 1,
      "tags": ["Spring Boot", "AWS"],
      "likeCount": 14,
      "createdAt": "2026-03-28T10:00:00"
    }
  ],
  "totalElements": 47,
  "totalPages": 5,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

`PostSummaryResponse` 필드 근거:

| 필드 | 근거 |
|------|------|
| `id`, `title`, `board`, `category`, `generation` | `PostSummaryResponse.java:8-12` |
| `status`, `authorId`, `tags` | `PostSummaryResponse.java:13-15` |
| `likeCount`, `createdAt` | `PostSummaryResponse.java:16-18` |

> **주의:** `content` 필드(본문)는 목록에 포함되지 않습니다. 단건 조회에서만 반환됩니다.

---

### 게시글 단건 조회

```http
GET /api/blog/posts/{id}
```

> 근거: `PostController.java:49`

로그인 유저가 요청하면 `liked`, `bookmarked` 값이 실제 상태로 반환됩니다.  
비로그인 요청 시 두 값 모두 `false`입니다.

> 근거: `PostController.java:51` — `@CurrentUser CustomUserDetails user` (nullable, `CurrentUser.java:17-20`)

**Response** `200 OK`

> 근거: `src/main/java/com/study/blog/application/post/dto/PostResponse.java:8-24`

```json
{
  "id": 1,
  "title": "Spring Boot EC2 배포 자동화",
  "content": "마크다운 본문 전체...",
  "board": "백엔드",
  "category": "CI/CD",
  "status": "PUBLISHED",
  "generation": "13기",
  "repostFromId": null,
  "authorId": 1,
  "tags": ["Spring Boot", "AWS"],
  "likeCount": 14,
  "bookmarkCount": 7,
  "liked": true,
  "bookmarked": false,
  "createdAt": "2026-03-28T10:00:00",
  "updatedAt": "2026-03-29T12:00:00"
}
```

`PostResponse` 필드 근거: `PostResponse.java:8-24` (id, title, content, board, category, status, generation, repostFromId, authorId, tags, likeCount, bookmarkCount, liked, bookmarked, createdAt, updatedAt)

---

### 게시글 작성 (로그인 필요)

```http
POST /api/blog/posts
Authorization: Bearer {accessToken}
```

> 근거: `PostController.java:56`, `@PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")`

**Request Body**

> 근거: `src/main/java/com/study/blog/application/post/dto/PostCreateRequest.java:9-16`

```json
{
  "title": "제목",
  "content": "마크다운 본문",
  "board": "백엔드",
  "category": "CI/CD",
  "status": "DRAFT",
  "generation": "13기",
  "tags": ["Spring Boot"],
  "repostFromId": null
}
```

| 필드 | 타입 | 제약 | 근거 |
|------|------|------|------|
| `title` | String | `@NotBlank` | `PostCreateRequest.java:9` |
| `content` | String | `@NotBlank` | `PostCreateRequest.java:10` |
| `board` | String | `@NotBlank` | `PostCreateRequest.java:11` |
| `category` | String | `@NotBlank` | `PostCreateRequest.java:12` |
| `status` | PostStatus | `@NotNull` (`DRAFT` \| `PUBLISHED`) | `PostCreateRequest.java:13` |
| `generation` | String | `@NotBlank` | `PostCreateRequest.java:14` |
| `tags` | List\<String\> | 선택 | `PostCreateRequest.java:15` |
| `repostFromId` | Long | 선택, 인용 글 ID | `PostCreateRequest.java:16` |

**Response** `201 Created` — `PostResponse` (단건 조회와 동일한 구조)

---

### 게시글 수정 (로그인 필요, 본인 or ADMIN)

```http
PUT /api/blog/posts/{id}
Authorization: Bearer {accessToken}
```

> 근거: `PostController.java:63`

**Request Body**

> 근거: `src/main/java/com/study/blog/application/post/dto/PostUpdateRequest.java:9-13`

```json
{
  "title": "수정된 제목",
  "content": "수정된 본문",
  "board": "백엔드",
  "category": "CI/CD",
  "status": "PUBLISHED",
  "tags": ["Spring Boot", "Docker"]
}
```

| 필드 | 제약 | 근거 |
|------|------|------|
| `title` | `@NotBlank` | `PostUpdateRequest.java:9` |
| `content` | `@NotBlank` | `PostUpdateRequest.java:10` |
| `board` | `@NotBlank` | `PostUpdateRequest.java:11` |
| `category` | `@NotBlank` | `PostUpdateRequest.java:12` |
| `status` | `@NotNull` | `PostUpdateRequest.java:13` |
| `tags` | 선택 | `PostUpdateRequest.java:14` |

**Response** `200 OK` — `PostResponse`

---

### 게시글 삭제 (로그인 필요, 본인 or ADMIN)

```http
DELETE /api/blog/posts/{id}
Authorization: Bearer {accessToken}
```

> 근거: `PostController.java:72`

**Response** `204 No Content` (body 없음)

> **주의:** body가 없으므로 응답을 JSON으로 파싱하지 마세요.

---

### 좋아요 토글 (로그인 필요)

```http
POST /api/blog/posts/{id}/like
Authorization: Bearer {accessToken}
```

> 근거: `PostController.java:80`

**Response** `200 OK`

> 근거: `PostController.java:85` — `Map.of("liked", liked)`

```json
{ "liked": true }
```

---

### 북마크 토글 (로그인 필요)

```http
POST /api/blog/posts/{id}/bookmark
Authorization: Bearer {accessToken}
```

> 근거: `PostController.java:88`

**Response** `200 OK`

> 근거: `PostController.java:93` — `Map.of("bookmarked", bookmarked)`

```json
{ "bookmarked": true }
```

---

### 내가 작성한 아티클 목록 (로그인 필요)

```http
GET /api/blog/posts/me
Authorization: Bearer {accessToken}
```

> 근거: `PostController.java:91`, `@PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")`

**Query Parameters** (모두 선택)

| 파라미터 | 타입 | 설명 | 기본값 |
|----------|------|------|--------|
| `status` | PostStatus | 상태 필터 (`DRAFT` \| `PENDING_REVIEW` \| `PUBLISHED` \| `REJECTED` \| `HIDDEN`) | 전체 |
| `page` | int | 페이지 번호 (0-indexed) | `0` |
| `size` | int | 페이지 크기 | `10` |

**Response** `200 OK` — `Page<PostSummaryResponse>` (게시글 목록 조회와 동일한 구조)

> 본인 글이므로 `DRAFT`, `PENDING_REVIEW`, `REJECTED` 상태 글도 포함됩니다.

---

### 내가 북마크한 아티클 목록 (로그인 필요)

```http
GET /api/blog/posts/bookmarks
Authorization: Bearer {accessToken}
```

> 근거: `PostController.java:108`, `@PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")`

**Query Parameters** (모두 선택)

| 파라미터 | 타입 | 설명 | 기본값 |
|----------|------|------|--------|
| `page` | int | 페이지 번호 (0-indexed) | `0` |
| `size` | int | 페이지 크기 | `10` |

**Response** `200 OK` — `Page<PostSummaryResponse>` (게시글 목록 조회와 동일한 구조)

> `PUBLISHED` 상태 글만 반환됩니다. 북마크했더라도 비공개/심사 중인 글은 제외됩니다.

---

## 5. 댓글 API

### 댓글 목록 조회

```http
GET /api/blog/posts/{postId}/comments
```

> 근거: `src/main/java/com/study/blog/presentation/comment/CommentController.java:34`

로그인 유저 요청 시 `liked` 값이 실제 상태로 반환됩니다.

**Response** `200 OK` — 트리 구조 (대댓글이 `replies`에 포함)

> 근거: `src/main/java/com/study/blog/application/comment/dto/CommentResponse.java:7-15`

```json
[
  {
    "id": 1,
    "content": "좋은 글이네요!",
    "userId": 2,
    "parentId": null,
    "likeCount": 3,
    "liked": false,
    "createdAt": "2026-03-29T14:00:00",
    "replies": [
      {
        "id": 2,
        "content": "감사합니다!",
        "userId": 1,
        "parentId": 1,
        "likeCount": 0,
        "liked": false,
        "createdAt": "2026-03-29T15:00:00",
        "replies": []
      }
    ]
  }
]
```

`CommentResponse` 필드 근거: `CommentResponse.java:7-15` (id, content, userId, parentId, likeCount, liked, createdAt, replies)

---

### 댓글 작성 (로그인 필요)

```http
POST /api/blog/posts/{postId}/comments
Authorization: Bearer {accessToken}
```

> 근거: `CommentController.java:41`

**Request Body**

> 근거: `src/main/java/com/study/blog/application/comment/dto/CommentCreateRequest.java:5`

```json
{
  "content": "댓글 내용",
  "parentId": null
}
```

| 필드 | 타입 | 설명 | 근거 |
|------|------|------|------|
| `content` | String | 필수(`@NotBlank`) | `CommentCreateRequest.java:5` |
| `parentId` | Long | 대댓글이면 부모 댓글 ID, 최상위면 `null` | `CommentCreateRequest.java:5` |

**Response** `201 Created` — `CommentResponse`

---

### 댓글 수정 (로그인 필요, 본인 or ADMIN)

```http
PUT /api/blog/comments/{id}
Authorization: Bearer {accessToken}
```

> 근거: `CommentController.java:51`

**Request Body**

> 근거: `src/main/java/com/study/blog/application/comment/dto/CommentUpdateRequest.java:5`

```json
{ "content": "수정된 댓글 내용" }
```

**Response** `200 OK` — `CommentResponse`

---

### 댓글 삭제 (로그인 필요, 본인 or ADMIN)

```http
DELETE /api/blog/comments/{id}
Authorization: Bearer {accessToken}
```

> 근거: `CommentController.java:60`

**Response** `204 No Content` (body 없음)

---

### 댓글 좋아요 토글 (로그인 필요)

```http
POST /api/blog/comments/{id}/like
Authorization: Bearer {accessToken}
```

> 근거: `CommentController.java:68`

**Response** `200 OK`

```json
{ "liked": true }
```

---

## 6. 어드민 API

> 모든 어드민 API는 `ADMIN` 또는 `PRESIDENT` 역할 필요.  
> 근거: `src/main/java/com/study/blog/presentation/admin/AdminController.java` — `@PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")`  
> `MEMBER`가 호출하면 `403 Forbidden`.

### 블로그 통계 조회

```http
GET /api/blog/admin/stats
Authorization: Bearer {accessToken}
```

> 근거: `AdminController.java:31`

**Response** `200 OK`

> 근거: `src/main/java/com/study/blog/application/admin/dto/AdminStatsResponse.java:3-4`

```json
{
  "totalPosts": 47,
  "publishedPosts": 40,
  "draftPosts": 7,
  "totalComments": 123
}
```

---

### 전체 게시글 조회 (DRAFT 포함)

```http
GET /api/blog/admin/posts?page=0&size=20
Authorization: Bearer {accessToken}
```

> 근거: `AdminController.java:36`

**Response** `200 OK` — `Page<AdminPostResponse>`

> 근거: `src/main/java/com/study/blog/application/admin/dto/AdminPostResponse.java:8-18`

```json
{
  "content": [
    {
      "id": 1,
      "title": "Spring Boot EC2 배포",
      "board": "백엔드",
      "category": "CI/CD",
      "generation": "13기",
      "status": "DRAFT",
      "authorId": 1,
      "tags": ["Spring Boot"],
      "likeCount": 0,
      "createdAt": "2026-03-28T10:00:00"
    }
  ],
  "totalElements": 52,
  "totalPages": 3,
  "number": 0,
  "size": 20
}
```

---

### 게시글 상태 변경

```http
PATCH /api/blog/admin/posts/{id}/status
Authorization: Bearer {accessToken}
```

> 근거: `AdminController.java:43`

**Request Body**

> 근거: `src/main/java/com/study/blog/application/admin/dto/PostStatusUpdateRequest.java:6` — `@NotNull PostStatus status`

```json
{ "status": "PUBLISHED" }
```

**Response** `200 OK` — `AdminPostResponse` (변경된 게시글 반환)

> 근거: `AdminController.java:46` — `ResponseEntity.ok(adminService.changePostStatus(id, req.status()))`

---

### 게시글 강제 삭제

```http
DELETE /api/blog/admin/posts/{id}
Authorization: Bearer {accessToken}
```

> 근거: `AdminController.java`

**조건**: 게시글 상태가 `HIDDEN`이고, 숨김 처리 후 **24시간 이상** 경과해야 함.

| 에러 | HTTP | 메시지 |
|------|------|--------|
| HIDDEN이 아닌 게시글 | `400` | 숨김 처리된 게시글만 삭제할 수 있습니다 |
| 24시간 미경과 | `400` | 숨김 처리 후 24시간이 지나야 삭제할 수 있습니다 |

**Response** `204 No Content` (body 없음)

---

### 게시글 숨김 처리

```http
PATCH /api/blog/admin/posts/{id}/hide
Authorization: Bearer {accessToken}
```

> 근거: `AdminController.java`

숨김 처리된 게시글은 일반 목록에서 제외됩니다. 작성자 본인은 단건 조회 가능.

**Response** `200 OK` — `AdminPostResponse`

```json
{
  "id": 278,
  "title": "테스트 게시글",
  "board": "백엔드",
  "category": "Spring",
  "generation": "15기",
  "status": "HIDDEN",
  "authorId": 1645,
  "tags": [],
  "likeCount": 0,
  "createdAt": "2026-06-01T20:05:32",
  "hiddenAt": "2026-06-01T20:24:55"
}
```

---

### 댓글 숨김 처리

```http
PATCH /api/blog/admin/comments/{id}/hide
Authorization: Bearer {accessToken}
```

> 근거: `AdminController.java`

**Response** `204 No Content` (body 없음)

---

### 댓글 강제 삭제

```http
DELETE /api/blog/admin/comments/{id}
Authorization: Bearer {accessToken}
```

> 근거: `AdminController.java`

**조건**: 댓글 상태가 `HIDDEN`이고, 숨김 처리 후 **24시간 이상** 경과해야 함.

| 에러 | HTTP | 메시지 |
|------|------|--------|
| HIDDEN이 아닌 댓글 | `400` | 숨김 처리된 댓글만 삭제할 수 있습니다 |
| 24시간 미경과 | `400` | 숨김 처리 후 24시간이 지나야 댓글을 삭제할 수 있습니다 |

**Response** `204 No Content` (body 없음)

---

## 7. 유저 권한 관리 API (PRESIDENT 전용)

> 모든 엔드포인트는 `PRESIDENT` 역할 필요.  
> 근거: `src/main/java/com/study/auth/presentation/controller/UserAdminController.java` — `@PreAuthorize("hasRole('PRESIDENT')")`  
> `ADMIN` 또는 `MEMBER`가 호출하면 `403 Forbidden`.

**Response 공통 구조**

```json
{
  "id": 1612,
  "email": "user@example.com",
  "role": "ADMIN",
  "status": "ACTIVE",
  "signupRequestedAt": "2026-05-13T13:32:59.225965Z",
  "approvedAt": null
}
```

---

### Admin 권한 부여

```http
POST /api/admin/users/{userId}/grant-admin
Authorization: Bearer {presidentToken}
```

대상 유저에게 `ADMIN` 권한을 부여합니다.

| 에러 조건 | HTTP |
|-----------|------|
| 자기 자신에게 부여 | `400` |
| 이미 ADMIN인 유저 | `400` |
| 비활성(ACTIVE 아님) 유저 | `400` |

**Response** `200 OK` — 변경된 유저 정보

---

### Admin 권한 해제

```http
POST /api/admin/users/{userId}/revoke-admin
Authorization: Bearer {presidentToken}
```

대상 유저의 `ADMIN` 권한을 `MEMBER`로 강등합니다.

| 에러 조건 | HTTP |
|-----------|------|
| 자기 자신 해제 시도 | `400` |
| ADMIN이 아닌 유저 | `400` |

**Response** `200 OK` — 변경된 유저 정보

---

### PRESIDENT 권한 이양

```http
POST /api/admin/users/{userId}/transfer-president
Authorization: Bearer {presidentToken}
```

`PRESIDENT` 권한을 대상 유저에게 이양합니다. 기존 회장은 `MEMBER`로 강등됩니다.

| 에러 조건 | HTTP |
|-----------|------|
| 자기 자신에게 이양 | `400` |
| 비활성(ACTIVE 아님) 유저 | `400` |

**Response** `200 OK` — 변경된 유저 정보 (새 PRESIDENT)

> **주의:** 이양 후 기존 PRESIDENT 토큰은 만료되지 않지만 권한이 `MEMBER`로 변경됩니다. 이양 즉시 재로그인을 권장합니다.

---

## 8. 헬스체크 API

### 라이브니스 프로브

```http
GET /api/health
```

**Response** `200 OK`

```json
{ "status": "UP" }
```

### 레디니스 프로브 (DB 연결 확인)

```http
GET /api/health/ready
```

**Response** `200 OK` / `503 Service Unavailable`

성공 시 `200 OK`:
```json
{ "status": "UP" }
```

실패 시 `503 Service Unavailable`:
```json
{ "status": "DOWN", "reason": "DB connection failed" }
```

---

## 9. 에러 처리

### 에러 응답 구조

> 근거: `src/main/java/com/study/shared/exception/GlobalExceptionHandler.java:63`  
> `Map.of("status", status.value(), "message", message)`

모든 에러 응답은 아래 형식을 따릅니다.

```json
{
  "status": 401,
  "message": "인증이 필요합니다"
}
```

### HTTP 상태 코드별 의미

| 상태 코드 | 의미 | 메시지(한국어) | 근거 | 처리 방법 |
|-----------|------|----------------|------|-----------|
| `400 Bad Request` | 요청 파라미터/바디 유효성 오류 | - | `GlobalExceptionHandler.java` | 에러 메시지를 사용자에게 표시 |
| `401 Unauthorized` | 인증 필요 (토큰 없음/만료) | `"인증이 필요합니다"` | `SecurityConfig.java:98` | 토큰 갱신 후 재시도 → 실패 시 로그인 이동 |
| `403 Forbidden` | 권한 없음 | `"접근 권한이 없습니다"` | `SecurityConfig.java:108` | 권한 없음 표시 |
| `404 Not Found` | 리소스 없음 | - | `GlobalExceptionHandler.java` | "찾을 수 없습니다" 표시 |
| `503 Service Unavailable` | DB 이상 | - | `HealthController` | "서비스 점검 중" 표시 |

### DELETE 응답 주의사항

`DELETE` 요청은 `204 No Content`로 **body가 없습니다**.

> 근거:
> - 게시글 삭제: `PostController.java:72`
> - 댓글 삭제: `CommentController.java:60`
> - 어드민 게시글 삭제: `AdminController.java:52`

```js
// 올바른 처리
await api.delete(`/api/blog/posts/${id}`)
// 잘못된 처리 — body 없어서 파싱 오류
const data = await api.delete(`/api/blog/posts/${id}`).then(r => r.data)
```

---

## 10. 공통 타입 정의

### Enum

> 근거: `src/main/java/com/study/auth/domain/model/UserRole.java`, `UserStatus.java`, `src/main/java/com/study/blog/domain/model/PostStatus.java`

```ts
type UserRole = 'PRESIDENT' | 'ADMIN' | 'MEMBER'
type UserStatus = 'PENDING' | 'ACTIVE' | 'REJECTED' | 'EXPIRED' | 'ALUMNI'
type PostStatus = 'DRAFT' | 'PENDING_REVIEW' | 'PUBLISHED' | 'REJECTED' | 'HIDDEN'
```

> - `PRESIDENT`: 서비스 최상위 관리자. DB에서 직접 초기 지정.
> - `ALUMNI`: 기수 종료 후 상태. `EXPIRED`는 가입 7일 내 미승인 만료에만 사용.
> - `HIDDEN`: 관리자가 숨김 처리한 게시글. 작성자와 ADMIN/PRESIDENT만 단건 조회 가능.

### JWT Access Token Claims

> 근거: `JwtProvider.java:45-47` — `.subject(userId.toString())`, `.claim("role", role.name())`, `.claim("type", "access")`

Access Token을 디코딩하면 아래 정보를 얻을 수 있습니다.  
(로그인 후 사용자 역할 확인 등에 활용)

```json
{
  "sub": "1",
  "role": "MEMBER",
  "type": "access",
  "iat": 1748000000,
  "exp": 1748000900
}
```

### JWT Refresh Token Claims

> 근거: `JwtProvider.java:63-66` — `.id(UUID.randomUUID())`, `.subject(userId)`, `.claim("familyId", familyId)`, `.claim("type", "refresh")`

```json
{
  "jti": "uuid",
  "sub": "1",
  "familyId": "uuid",
  "type": "refresh"
}
```

---

## 11. 토큰 자동 갱신 구현 예시

Access Token(15분)이 만료되면 `401`이 반환됩니다.

> 근거:
> - 401 응답: `SecurityConfig.java:98`
> - 토큰 갱신 엔드포인트: `AuthController.java:70`
> - Access Token 만료 15분: `application.yml:43`

Axios interceptor를 사용해 자동으로 갱신 후 재시도하는 패턴입니다.

```js
let accessToken = null

// 요청 인터셉터 — 매 요청에 토큰 삽입
api.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

// 응답 인터셉터 — 401 시 토큰 갱신 후 재시도
let isRefreshing = false
let refreshQueue = []

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          refreshQueue.push({ resolve, reject })
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // POST /api/auth/refresh — AuthController.java:70
        // withCredentials: true 덕분에 refresh_token 쿠키 자동 전송
        const { data } = await api.post('/api/auth/refresh')
        accessToken = data.accessToken
        refreshQueue.forEach(({ resolve }) => resolve(accessToken))
        refreshQueue = []
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return api(originalRequest)
      } catch (refreshError) {
        // Refresh Token도 만료 → 로그아웃 처리
        refreshQueue.forEach(({ reject }) => reject(refreshError))
        refreshQueue = []
        accessToken = null
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export { api, accessToken }
```

---

## 빠른 참조 — 엔드포인트 요약

| Method | Path | 인증 | 응답 | 비고 |
|--------|------|------|------|------|
| POST | `/api/auth/signup` | 불필요 | 201 | |
| POST | `/api/auth/login` | 불필요 | 200 | |
| POST | `/api/auth/refresh` | 불필요 (쿠키) | 200 | |
| POST | `/api/auth/logout` | 불필요 (쿠키) | **204** | |
| GET | `/api/blog/posts` | 불필요 | 200 | HIDDEN 제외 |
| GET | `/api/blog/posts/{id}` | 선택 | 200 | HIDDEN: 작성자·ADMIN·PRESIDENT만 |
| POST | `/api/blog/posts` | MEMBER/ADMIN/PRESIDENT | 201 | |
| PUT | `/api/blog/posts/{id}` | MEMBER/ADMIN/PRESIDENT | 200 | |
| DELETE | `/api/blog/posts/{id}` | MEMBER/ADMIN/PRESIDENT | **204** | |
| POST | `/api/blog/posts/{id}/like` | MEMBER/ADMIN/PRESIDENT | 200 | |
| POST | `/api/blog/posts/{id}/bookmark` | MEMBER/ADMIN/PRESIDENT | 200 | |
| GET | `/api/blog/posts/me` | MEMBER/ADMIN/PRESIDENT | 200 | status 필터 가능, 전체 상태 포함 |
| GET | `/api/blog/posts/bookmarks` | MEMBER/ADMIN/PRESIDENT | 200 | PUBLISHED만 반환 |
| GET | `/api/blog/posts/{postId}/comments` | 불필요 | 200 | HIDDEN 댓글 제외 |
| POST | `/api/blog/posts/{postId}/comments` | MEMBER/ADMIN/PRESIDENT | 201 | |
| PUT | `/api/blog/comments/{id}` | MEMBER/ADMIN/PRESIDENT | 200 | |
| DELETE | `/api/blog/comments/{id}` | MEMBER/ADMIN/PRESIDENT | **204** | |
| POST | `/api/blog/comments/{id}/like` | MEMBER/ADMIN/PRESIDENT | 200 | |
| GET | `/api/blog/admin/stats` | ADMIN/PRESIDENT | 200 | |
| GET | `/api/blog/admin/posts` | ADMIN/PRESIDENT | 200 | HIDDEN 포함 |
| PATCH | `/api/blog/admin/posts/{id}/status` | ADMIN/PRESIDENT | **200 + body** | |
| PATCH | `/api/blog/admin/posts/{id}/hide` | ADMIN/PRESIDENT | **200 + body** | 게시글 숨김 처리 |
| DELETE | `/api/blog/admin/posts/{id}` | ADMIN/PRESIDENT | **204** | HIDDEN + 24h 경과 필요 |
| PATCH | `/api/blog/admin/comments/{id}/hide` | ADMIN/PRESIDENT | **204** | 댓글 숨김 처리 |
| DELETE | `/api/blog/admin/comments/{id}` | ADMIN/PRESIDENT | **204** | HIDDEN + 24h 경과 필요 |
| POST | `/api/admin/users/{id}/grant-admin` | PRESIDENT | 200 | ADMIN 권한 부여 |
| POST | `/api/admin/users/{id}/revoke-admin` | PRESIDENT | 200 | ADMIN 권한 해제 |
| POST | `/api/admin/users/{id}/transfer-president` | PRESIDENT | 200 | 회장 권한 이양 |
| GET | `/api/health` | 불필요 | 200 | |
| GET | `/api/health/ready` | 불필요 | 200/503 | |