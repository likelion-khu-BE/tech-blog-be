# Blog 서비스 구현 리서치 문서

> 작성 기준: `feat/blog-dev-v0.0.1` 브랜치 기준 구현 코드 분석  
> 작성일: 2026-04-12

---

## 목차

1. [전체 아키텍처 개요](#1-전체-아키텍처-개요)
2. [DB 스키마 상세](#2-db-스키마-상세)
3. [게시글(Post) 도메인](#3-게시글post-도메인)
4. [댓글(Comment) 도메인](#4-댓글comment-도메인)
5. [어드민(Admin) 도메인](#5-어드민admin-도메인)
6. [공통 인프라](#6-공통-인프라)
7. [CASCADE 전략 및 데이터 정합성](#7-cascade-전략-및-데이터-정합성)
8. [인증 현황 (MockAuth)](#8-인증-현황-mockauth)

---

## 1. 전체 아키텍처 개요

```
Client
  │
  ▼
[Controller Layer]          ← HTTP 요청/응답 담당, @Valid 검증
  │
  ▼
[Service Layer]             ← 비즈니스 로직, @Transactional 관리
  │
  ▼
[Repository Layer]          ← Spring Data JPA + Specification
  │
  ▼
[Database (PostgreSQL)]     ← posts / comments / 관계 테이블 6개
```

**패키지 구조**

```
com.study.blog
├── post/           ← 게시글 핵심 도메인
│   ├── dto/        ← Request / Response DTO
│   └── (entity, repository, service, controller, specification)
├── comment/        ← 댓글 도메인
│   └── dto/
├── admin/          ← 어드민 전용 API
│   └── dto/
└── common/
    ├── ApiResponse  ← 통일된 응답 래퍼
    ├── auth/        ← MockAuth (임시 인증)
    └── exception/   ← BlogException, BlogErrorCode, GlobalExceptionHandler
```

**공통 응답 형식**

모든 API는 `ApiResponse<T>`로 감싸서 반환한다.

```json
// 성공
{ "data": { ... }, "message": null }

// 실패
{ "data": null, "message": "에러 메시지" }
```

---

## 2. DB 스키마 상세

### 2-1. posts

| 컬럼          | 타입         | 제약                    | 설명                      |
|-------------|------------|----------------------|-------------------------|
| id          | BIGINT      | PK, AUTO_INCREMENT   | 게시글 식별자                 |
| user_id     | UUID        | NOT NULL             | 작성자 (users 테이블 외부 참조)   |
| title       | VARCHAR     | NOT NULL             | 제목                      |
| content     | TEXT        | NOT NULL             | 본문                      |
| board       | VARCHAR(20) | NOT NULL             | 게시판 분류 (예: 백엔드, AI)     |
| category    | VARCHAR(20) | NOT NULL             | 세부 카테고리 (예: CI/CD, LLM) |
| status      | VARCHAR(20) | NOT NULL             | `PUBLISHED` / `DRAFT`   |
| generation  | VARCHAR(10) | NOT NULL             | 기수 (예: 13기)             |
| repost_from_id | BIGINT  | NULL 가능              | 원본 게시글 ID (리포스트 시)      |
| created_at  | TIMESTAMP   | NOT NULL             | 생성 시각 (자동)              |
| updated_at  | TIMESTAMP   | NOT NULL             | 수정 시각 (자동)              |

### 2-2. post_tags

| 컬럼       | 타입         | 제약                          | 설명              |
|----------|------------|------------------------------|-----------------|
| post_id  | BIGINT      | PK(복합), FK → posts(id) CASCADE | 게시글 참조          |
| tag_name | VARCHAR(50) | PK(복합)                       | 태그 이름           |

- 복합 PK `(post_id, tag_name)` → 같은 게시글에 동일 태그 중복 불가
- `ON DELETE CASCADE`: 게시글 삭제 시 태그 자동 삭제

### 2-3. post_likes

| 컬럼      | 타입    | 제약                          | 설명       |
|---------|-------|------------------------------|----------|
| post_id | BIGINT | PK(복합), FK → posts(id) CASCADE | 게시글 참조   |
| user_id | UUID  | PK(복합)                       | 좋아요 누른 유저 |

- 복합 PK `(post_id, user_id)` → 한 유저가 같은 게시글에 중복 좋아요 불가
- `ON DELETE CASCADE`: 게시글 삭제 시 자동 삭제

### 2-4. post_bookmarks

| 컬럼      | 타입    | 제약                          | 설명       |
|---------|-------|------------------------------|----------|
| post_id | BIGINT | PK(복합), FK → posts(id) CASCADE | 게시글 참조   |
| user_id | UUID  | PK(복합)                       | 북마크한 유저  |

- `ON DELETE CASCADE`: 게시글 삭제 시 자동 삭제

### 2-5. comments

| 컬럼         | 타입      | 제약                                  | 설명                 |
|------------|---------|--------------------------------------|--------------------|
| id         | BIGINT   | PK, AUTO_INCREMENT                   | 댓글 식별자             |
| post_id    | BIGINT   | NOT NULL (논리적 참조, FK 없음)             | 소속 게시글             |
| user_id    | UUID     | NOT NULL                             | 작성자                |
| parent_id  | BIGINT   | NULL 가능, FK → comments(id) CASCADE  | 부모 댓글 (대댓글 구조)    |
| content    | TEXT     | NOT NULL                             | 댓글 내용              |
| created_at | TIMESTAMP | NOT NULL                            | 생성 시각              |

- `parent_id IS NULL` → 최상위 댓글
- `parent_id IS NOT NULL` → 대댓글 (1단계만 지원)
- `parent_id ON DELETE CASCADE`: 부모 댓글 삭제 시 자식 댓글 자동 삭제
- `post_id`는 DB FK 없이 애플리케이션 레벨에서만 관리 (게시글 삭제 시 댓글은 별도 처리 필요)

> **주의**: `comments.post_id`에는 DB 레벨 FK가 없다. 어드민 강제 삭제 시 `postRepository.delete(post)` 전에 명시적으로 댓글을 삭제하거나 DB `ON DELETE CASCADE`로 처리해야 한다. 현재 어드민 `forceDeletePost`는 태그만 명시 삭제하고 있으며, `post_likes`·`post_bookmarks`·`comments`는 DB CASCADE로 처리된다.

### 2-6. comment_likes

| 컬럼         | 타입    | 제약                              | 설명          |
|------------|-------|----------------------------------|-------------|
| comment_id | BIGINT | PK(복합), FK → comments(id) CASCADE | 댓글 참조       |
| user_id    | UUID  | PK(복합)                           | 좋아요 누른 유저   |

- `ON DELETE CASCADE`: 댓글 삭제 시 자동 삭제

---

## 3. 게시글(Post) 도메인

### 3-1. GET /api/blog/posts — 게시글 목록 조회

**요청 파라미터 (모두 optional)**

| 파라미터       | 타입     | 설명                   |
|------------|--------|----------------------|
| board      | String | 게시판 필터               |
| category   | String | 카테고리 필터              |
| generation | String | 기수 필터                |
| authorId   | UUID   | 작성자 필터               |
| keyword    | String | 제목/본문 키워드 검색 (LIKE)  |
| page       | int    | 페이지 번호 (기본값 0)       |
| size       | int    | 페이지 크기 (기본값 10)      |

**처리 흐름**

1. `PostSpecification`으로 JPA Criteria API 기반 동적 쿼리를 조립한다.
2. `published()` 조건이 항상 기본 포함 → **DRAFT 게시글은 목록에서 제외**.
3. 각 필터 파라미터가 `null`이면 `cb.conjunction()`(항상 참)으로 단락된다.
4. `keyword`는 `title LIKE %keyword%` OR `content LIKE %keyword%` 조건이다.
5. `createdAt DESC` 정렬 + `PageRequest`로 페이지네이션 처리.
6. 결과 각 게시글마다 `post_tags`·`post_likes` 테이블을 조회해 태그 목록과 좋아요 수를 조립한다.

**응답: `Page<PostSummaryResponse>`**

```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Spring Boot + GitHub Actions CI/CD 구축",
        "board": "백엔드",
        "category": "CI/CD",
        "generation": "13기",
        "status": "PUBLISHED",
        "authorId": "00000000-0000-0000-0000-000000000001",
        "tags": ["Spring Boot", "GitHub Actions"],
        "likeCount": 1,
        "createdAt": "2026-04-10T10:00:00"
      }
    ],
    "totalElements": 3,
    "totalPages": 1,
    "size": 10,
    "numberOfElements": 3
  }
}
```

> `PostSummaryResponse`는 `content` 필드를 포함하지 않는다. 목록에서 본문을 제외해 응답 크기를 줄인다.

---

### 3-2. GET /api/blog/posts/{id} — 게시글 단건 조회

**처리 흐름**

1. `postId`로 `Post` 엔티티 조회. 없으면 `POST_NOT_FOUND(404)`.
2. 게시글 `status == DRAFT`이고 요청자(`MockAuth.MOCK_USER_ID`)가 작성자가 아니면 `FORBIDDEN(403)`.
3. 정상 접근 시: 태그 목록, 좋아요 수, 북마크 수, 현재 유저의 좋아요·북마크 여부를 조회해 `PostResponse`로 반환.

**응답: `PostResponse`** — 목록과 달리 `content`, `bookmarkCount`, `liked`, `bookmarked`, `repostFromId`, `updatedAt` 포함.

```json
{
  "data": {
    "id": 1,
    "title": "Spring Boot + GitHub Actions CI/CD 구축",
    "content": "GitHub Actions와 AWS EC2로 배포 파이프라인을 구성합니다.",
    "board": "백엔드",
    "category": "CI/CD",
    "generation": "13기",
    "status": "PUBLISHED",
    "repostFromId": null,
    "authorId": "00000000-...",
    "tags": ["Spring Boot", "GitHub Actions"],
    "likeCount": 1,
    "bookmarkCount": 0,
    "liked": true,
    "bookmarked": false,
    "createdAt": "2026-04-10T10:00:00",
    "updatedAt": "2026-04-10T10:00:00"
  }
}
```

---

### 3-3. POST /api/blog/posts — 게시글 작성

**요청 바디: `PostCreateRequest`**

```json
{
  "title": "제목",          // @NotBlank
  "content": "본문",        // @NotBlank
  "board": "백엔드",        // @NotBlank
  "category": "CI/CD",    // @NotBlank
  "status": "PUBLISHED",   // @NotNull (PUBLISHED | DRAFT)
  "generation": "13기",    // @NotBlank
  "tags": ["Spring Boot"], // optional
  "repostFromId": null     // optional, 리포스트 원본 ID
}
```

**처리 흐름**

1. `@Valid` 검증 → 실패 시 Spring의 `MethodArgumentNotValidException` → 400.
2. `Post` 엔티티 빌더로 생성 후 `postRepository.save()`.
3. `tags` 리스트를 `distinct()` 처리 후 각 태그를 `PostTag(post, tagName)`으로 저장.
   - 태그 중복 방지는 Java 스트림 `distinct()`로 처리 (DB PK 충돌 방지).
4. 저장된 Post를 `PostResponse`로 변환해 HTTP 201과 함께 반환.

---

### 3-4. PUT /api/blog/posts/{id} — 게시글 수정

**요청 바디: `PostUpdateRequest`** — `title`, `content`, `board`, `category`, `status`, `tags`

**처리 흐름**

1. `postId`로 Post 조회. 없으면 404.
2. 요청자가 작성자가 아니면 `FORBIDDEN(403)`.
3. `post.update(...)` 호출 → 엔티티 필드 직접 수정 (더티 체킹으로 UPDATE 쿼리 발생).
4. 기존 태그 전부 `deleteByPost(post)` 후, 새 태그 재삽입 (덮어쓰기 방식).
5. `PostResponse` 반환.

> `generation` 필드는 수정 시 변경 불가 (`PostUpdateRequest`에 포함 안 됨).

---

### 3-5. DELETE /api/blog/posts/{id} — 게시글 삭제

**처리 흐름**

1. `postId`로 Post 조회. 없으면 404.
2. 요청자가 작성자가 아니면 403.
3. `postTagRepository.deleteByPost(post)` → 태그 먼저 삭제.
4. `postRepository.delete(post)` → DB `ON DELETE CASCADE`로 `post_likes`, `post_bookmarks`도 자동 삭제.
5. HTTP 204 반환.

> 사용자 본인만 삭제 가능. 관리자 강제 삭제는 Admin API 별도.

---

### 3-6. POST /api/blog/posts/{id}/like — 좋아요 토글

**처리 흐름 (토글 패턴)**

```
POST /api/blog/posts/1/like
  → DB에 (post_id=1, user_id=현재유저) 행이 있으면?
      YES → DELETE → { "liked": false }
      NO  → INSERT → { "liked": true }
```

- `postLikeRepository.findByIdPostIdAndIdUserId(postId, userId)` 로 존재 여부 확인.
- `Optional.map(delete).orElseGet(insert)` 패턴으로 분기.
- 응답: `{ "data": { "liked": true/false } }`

---

### 3-7. POST /api/blog/posts/{id}/bookmark — 북마크 토글

좋아요 토글과 동일한 패턴. `post_bookmarks` 테이블을 대상으로 동작.

응답: `{ "data": { "bookmarked": true/false } }`

---

## 4. 댓글(Comment) 도메인

### 4-1. GET /api/blog/posts/{postId}/comments — 댓글 목록

**처리 흐름 (트리 조립)**

1. `commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId)` → 해당 게시글의 모든 댓글을 **생성 시각 오름차순**으로 단일 쿼리 조회.
2. `LinkedHashMap<Long, CommentResponse>`를 순서 보장 맵으로 생성 (삽입 순서 유지).
3. 첫 번째 루프: 모든 댓글을 `CommentResponse`로 변환해 맵에 저장. `parentId == null`이면 `roots` 리스트에 추가.
4. 두 번째 루프: `parentId != null`인 댓글을 부모 `CommentResponse`의 `replies` 리스트에 추가.
5. `roots` 리스트 반환 → 각 루트 댓글에 `replies` 배열이 중첩.

**응답 구조**

```json
{
  "data": [
    {
      "id": 1,
      "content": "정말 유익한 글이네요!",
      "userId": "00000000-...",
      "parentId": null,
      "likeCount": 0,
      "liked": false,
      "createdAt": "...",
      "replies": [
        {
          "id": 3,
          "content": "github-hosted runner 쓰시면 편해요!",
          "userId": "00000000-...",
          "parentId": 1,
          "likeCount": 0,
          "liked": false,
          "createdAt": "...",
          "replies": []
        }
      ]
    },
    {
      "id": 2,
      "content": "EC2 Runner 설정이 어렵던데 어떻게 하셨나요?",
      "parentId": null,
      "replies": []
    }
  ]
}
```

> 현재 구현은 **1단계 대댓글만** 지원한다. 대댓글에 다시 대댓글을 달아도 두 번째 루프가 부모의 `replies`에만 추가하는 방식이라, 2단계 이상 중첩은 구조적으로 처리되지 않는다.

---

### 4-2. POST /api/blog/posts/{postId}/comments — 댓글 작성

**요청 바디**

```json
{
  "content": "댓글 내용",  // @NotBlank
  "parentId": null        // null → 최상위 댓글, 숫자 → 대댓글
}
```

**처리 흐름**

1. `parentId`가 있으면 `commentRepository.findById(parentId)` → 없으면 `PARENT_COMMENT_NOT_FOUND(404)`.
2. `Comment` 엔티티 빌더로 생성. `parent` 필드에 조회한 Comment 엔티티 참조 설정.
3. `commentRepository.save()` → DB에서 `parent_id` FK로 저장.
4. HTTP 201, `CommentResponse` 반환 (likeCount=0, liked=false로 초기화).

---

### 4-3. PUT /api/blog/comments/{id} — 댓글 수정

1. 댓글 조회. 없으면 404.
2. 작성자 일치 검증. 불일치 시 403.
3. `comment.updateContent(content)` → 더티 체킹으로 UPDATE.
4. 현재 좋아요 수 조회 후 `CommentResponse` 반환.

---

### 4-4. DELETE /api/blog/comments/{id} — 댓글 삭제

1. 댓글 조회. 없으면 404.
2. 작성자 일치 검증. 403.
3. `commentRepository.delete(comment)`.
   - DB `ON DELETE CASCADE`로 `comment_likes`도 자동 삭제.
   - `parent_id ON DELETE CASCADE`로 자식 댓글(대댓글)도 자동 삭제.
4. HTTP 204.

---

### 4-5. POST /api/blog/comments/{id}/like — 댓글 좋아요 토글

게시글 좋아요와 동일한 토글 패턴. `comment_likes` 테이블 대상.

응답: `{ "data": { "liked": true/false } }`

---

## 5. 어드민(Admin) 도메인

어드민 API는 인증 없이 접근 가능한 상태 (현재 구현 기준). 운영 환경에서는 인증/인가 필수.

### 5-1. GET /api/blog/admin/stats — 블로그 통계 조회

**처리 흐름**

1. `postRepository.count()` → 전체 게시글 수.
2. `postRepository.count(Specification: status == PUBLISHED)` → 발행된 게시글 수.
3. `totalPosts - publishedPosts` → 임시저장 수.
4. `commentRepository.count()` → 전체 댓글 수.

**응답**

```json
{
  "data": {
    "totalPosts": 4,
    "publishedPosts": 3,
    "draftPosts": 1,
    "totalComments": 3
  }
}
```

---

### 5-2. GET /api/blog/admin/posts — 전체 게시글 목록 (DRAFT 포함)

**요청 파라미터**

| 파라미터 | 기본값 | 설명        |
|------|-----|-----------|
| page | 0   | 페이지 번호    |
| size | 20  | 페이지 크기    |

**처리 흐름**

1. `postRepository.findAll(PageRequest, Sort)` — Specification 없이 **전체 게시글** 조회 (DRAFT 포함).
2. `createdAt DESC` 정렬.
3. 각 게시글마다 태그 목록 + 좋아요 수 조회 후 `AdminPostResponse`로 변환.

**응답 구조** — `PostSummaryResponse`와 동일하나 `AdminPostResponse`는 `status` 필드 포함 (DRAFT 여부 확인 가능).

---

### 5-3. PATCH /api/blog/admin/posts/{id}/status — 게시글 상태 강제 변경

**요청 바디**

```json
{ "status": "DRAFT" }   // or "PUBLISHED", @NotNull
```

**처리 흐름**

1. `postId`로 Post 조회. 없으면 404.
2. `post.changeStatus(status)` → 더티 체킹으로 `status` 컬럼만 UPDATE.
3. HTTP 200, `ApiResponse<Void>` 반환 (data: null).

> 어드민은 작성자 소유권 검증 없이 모든 게시글의 상태를 변경할 수 있다.

---

### 5-4. DELETE /api/blog/admin/posts/{id} — 게시글 강제 삭제

**처리 흐름**

1. `postId`로 Post 조회. 없으면 404.
2. `postTagRepository.deleteByPost(post)` → 태그 명시 삭제 (DB CASCADE 미적용 구간 대비).
3. `postRepository.delete(post)` → DB `ON DELETE CASCADE`로 `post_likes`, `post_bookmarks`, `comments`(+`comment_likes`) 자동 삭제.
4. HTTP 204.

> 어드민은 작성자 소유권 없이 모든 게시글 삭제 가능.

---

## 6. 공통 인프라

### 6-1. 예외 처리 흐름

```
Service → throw BlogException(BlogErrorCode.XXX)
        ↓
GlobalExceptionHandler.handleBlogException()
        ↓
ResponseEntity<ApiResponse<Void>> {
  status: BlogErrorCode.status (404 / 403)
  body: { "data": null, "message": "에러 메시지" }
}
```

**정의된 에러 코드**

| 코드                        | HTTP | 메시지                |
|---------------------------|------|--------------------|
| `POST_NOT_FOUND`          | 404  | 게시글을 찾을 수 없습니다     |
| `COMMENT_NOT_FOUND`       | 404  | 댓글을 찾을 수 없습니다      |
| `PARENT_COMMENT_NOT_FOUND`| 404  | 부모 댓글을 찾을 수 없습니다   |
| `FORBIDDEN`               | 403  | 권한이 없습니다           |

Bean Validation 실패(`@Valid`) 시 Spring 기본 `MethodArgumentNotValidException`이 발생하며 400 응답. 커스텀 핸들러로 별도 처리하지 않아 Spring Boot 기본 에러 응답 포맷으로 반환된다.

---

### 6-2. 페이지네이션 공통

| 도메인          | 기본 page | 기본 size | 정렬         |
|--------------|---------|---------|------------|
| 게시글 목록       | 0       | 10      | createdAt DESC |
| 어드민 게시글 목록   | 0       | 20      | createdAt DESC |

응답은 Spring Data의 `Page<T>` 래퍼로 반환되며 `data.content`, `data.totalElements`, `data.totalPages`, `data.size`, `data.numberOfElements` 필드 포함.

---

## 7. CASCADE 전략 및 데이터 정합성

JPA `@OnDelete(action = OnDeleteAction.CASCADE)` 애너테이션은 DB 레벨 `ON DELETE CASCADE`를 DDL에 반영한다. Hibernate가 JPA `CascadeType`과 별도로 **DB FK 제약에 CASCADE를 직접 설정**하는 방식이다.

**CASCADE 의존 관계 전체 맵**

```
posts
  ├── post_tags        (ON DELETE CASCADE via @OnDelete)
  ├── post_likes       (ON DELETE CASCADE via @OnDelete)
  ├── post_bookmarks   (ON DELETE CASCADE via @OnDelete)
  └── comments
        ├── comment_likes  (ON DELETE CASCADE via @OnDelete on comments.id)
        └── comments (자기참조, parent_id → ON DELETE CASCADE)
```

**게시글 삭제 시 실제 실행 순서 (어드민 forceDeletePost 기준)**

1. 애플리케이션: `postTagRepository.deleteByPost(post)` → `post_tags` 삭제 (명시적)
2. 애플리케이션: `postRepository.delete(post)` → DB에 DELETE 전송
3. DB (CASCADE):
   - `post_likes` 자동 삭제
   - `post_bookmarks` 자동 삭제
   - `comments` → 각 댓글 삭제 시 `comment_likes`도 CASCADE 삭제
   - 자식 댓글(대댓글)도 `parent_id CASCADE`로 연쇄 삭제

> `post_tags`를 명시 삭제하는 이유: `post_tags`에도 `@OnDelete(CASCADE)`가 있지만, 명시 삭제를 추가함으로써 JPA 1차 캐시 정합성을 보장하고 테스트 환경(H2)에서의 안정성을 높인다.

---

## 8. 인증 현황 (MockAuth)

현재 모든 인증은 `MockAuth.MOCK_USER_ID` 상수로 처리된다.

```java
public class MockAuth {
    public static final UUID MOCK_USER_ID =
        UUID.fromString("00000000-0000-0000-0000-000000000001");
}
```

- 모든 `Controller`에서 `UUID userId = MockAuth.MOCK_USER_ID;` 로 고정.
- JWT 등 실제 인증 미구현. 추후 Spring Security + JWT 필터 체인으로 교체 예정.
- 어드민 API도 인증 없음. 운영 전 반드시 롤 기반 접근 제어(RBAC) 적용 필요.

---

*이 문서는 `feat/blog-dev-v0.0.1` 기준 구현 코드를 직접 분석하여 작성되었습니다.*
