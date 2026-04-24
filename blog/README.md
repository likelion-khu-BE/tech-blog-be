# Blog 모듈 — 개발 가이드

경희대 멋쟁이사자처럼 기술 블로그 (`blog` 팀)

---

## 팀 구성

| 이름 | 역할 | 담당 |
|------|------|------|
| 장찬욱 | 팀장 | AdminPage, DetailPage, Header 컴포넌트 |
| 노희윤 | 팀원 | MainPage, BoardPage, ProfilePage |
| 김주연 | 팀원 | LoginPage, RegisterPage, WritePage / EditPage |

---

## 페이지별 구현 목록

### 장찬욱

#### `Header` 컴포넌트 (`src/components/Header.jsx`)
- [ ] 로그인 상태에 따라 로그인/로그아웃 버튼 토글
- [ ] 로그인된 사용자 아바타 및 이름 표시
- [ ] 관리자(ADMIN) 역할일 때 어드민 메뉴 노출
- [ ] 로그아웃 시 토큰 제거 및 홈으로 리디렉트

#### `AdminPage` (`/admin`)
- [ ] 가입 승인 대기 회원 목록 조회 API 연동
- [ ] 회원 승인 / 거절 처리
- [ ] 전체 회원 목록 조회 (검색 포함)
- [ ] 회원 Role 변경 (MEMBER ↔ ADMIN 승격/강등)
- [ ] 전체 게시글 목록 조회 (검색 포함)
- [ ] 게시글 강제 삭제 (관리자 권한)
- [ ] Draft 게시글 목록 조회
- [ ] 대시보드 통계 수치 API 연동 (회원 수, 게시글 수 등)
- [ ] ADMIN 역할 없으면 접근 차단 (라우터 가드)

#### `DetailPage` (`/post/:id`)
- [ ] 게시글 단건 조회 API 연동 (`id` 기반)
- [ ] 좋아요 토글 및 좋아요 수 반영
- [ ] 북마크 토글 및 북마크 수 반영
- [ ] 링크 공유 (클립보드 복사)
- [ ] 댓글 목록 조회
- [ ] 댓글 작성
- [ ] 댓글 좋아요
- [ ] 댓글 답글 (대댓글)
- [ ] 본인 게시글일 때만 수정 / 삭제 버튼 노출
- [ ] Draft 상태 배너 (본인에게만 표시)
- [ ] Repost 기능 (글 인용 작성으로 이동)

---

### 노희윤

#### `MainPage` (`/`)
- [ ] 최신 게시글 목록 API 연동 (미리보기 카드)
- [ ] 게시판 탭 필터 (전체 / AI / 백엔드 / 해커톤)
- [ ] 기수 / 태그 필터 적용
- [ ] 멤버 목록 API 연동 (멤버 소개 섹션)
- [ ] 히어로 섹션 통계 수치 API 연동 (멤버 수, 게시글 수)
- [ ] "게시글 더 보기" → BoardPage 이동

#### `BoardPage` (`/board`)
- [ ] 게시글 목록 API 연동 (페이지네이션 포함)
- [ ] 게시판 탭 필터 (전체 / AI / 백엔드 / 해커톤)
- [ ] 기수 / 저자 필터
- [ ] 카테고리 칩 필터 (LLM, CI/CD 등)
- [ ] 제목 · 태그 키워드 검색
- [ ] 페이지네이션 (이전 / 다음 / 페이지 번호)
- [ ] 로그인 사용자에게만 글쓰기 버튼 노출

#### `ProfilePage` (`/profile/:id`, `/members`)
- [ ] 회원 프로필 정보 API 연동
- [ ] 해당 회원의 게시글 목록 조회 (탭: 전체 / Published / Draft)
- [ ] 북마크 탭 — 내가 북마크한 글 목록 (본인일 때만)
- [ ] Draft 탭 본인에게만 표시
- [ ] GitHub / 블로그 / LinkedIn 외부 링크 연결 표시
- [ ] 게시글 수 / 활동 기수 통계 표시

---

### 김주연

#### `LoginPage` (`/login`)
- [ ] 이메일 / 비밀번호 입력 및 유효성 검사
- [ ] 로그인 API 연동 (JWT 토큰 수신 및 저장)
- [ ] 로그인 상태 유지 (Remember Me) 처리
- [ ] 로그인 실패 시 오류 메시지 표시
- [ ] 이미 로그인된 상태면 홈으로 리디렉트

#### `RegisterPage` (`/register`)
- [ ] 이름 / 이메일 / 비밀번호 / 기수 입력 폼
- [ ] 비밀번호 확인 일치 검사
- [ ] 이메일 중복 확인 API 연동
- [ ] 회원가입 API 연동 (가입 후 승인 대기 안내)
- [ ] 폼 유효성 검사 (필수 필드, 이메일 형식, 비밀번호 규칙)
- [ ] 약관 동의 체크박스 처리

#### `WritePage` / `EditPage` (`/write`, `/edit/:id`)
- [ ] 제목 / 본문 / 태그 입력 폼
- [ ] 마크다운 에디터 연동 (라이브 프리뷰 권장)
- [ ] 게시판 선택 (AI / 백엔드 / 해커톤)
- [ ] 태그 입력 및 삭제 (Chip 형태)
- [ ] Draft 저장 API 연동 (임시 저장)
- [ ] 발행 (Publish) API 연동
- [ ] 수정 시 기존 데이터 불러오기 (`/edit/:id`)
- [ ] 비로그인 상태 접근 차단

---

## API 명세 (사전 합의)

> 기능 구현 전에 반드시 읽어주세요.  
> 여기서 정한 필드 이름과 값을 기준으로 세 명이 동일하게 작성해야 합니다.

---

### 공통 데이터 구조

세 명 모두 쓰는 데이터의 형태입니다. 필드 이름을 이것과 다르게 쓰면 나중에 합칠 때 깨집니다.

#### 게시글 (Post)

```json
{
  "id": 1,
  "title": "Spring Boot 애플리케이션을 EC2에 자동 배포하기",
  "content": "마크다운 본문 전체",
  "preview": "본문 앞 100자 요약 (목록에서 사용)",
  "board": "백엔드",
  "category": "CI/CD",
  "tags": ["Spring Boot", "AWS EC2"],
  "generation": "13기",
  "status": "published",
  "author": {
    "id": 1,
    "name": "장찬욱"
  },
  "likeCount": 14,
  "bookmarkCount": 7,
  "commentCount": 3,
  "isLiked": true,
  "isBookmarked": false,
  "createdAt": "2026-03-28"
}
```

| 필드 | 가능한 값 |
|------|-----------|
| `board` | `"AI"` \| `"백엔드"` \| `"해커톤"` |
| `category` | AI: `"LLM"` \| `"MLOps"` \| `"모델 서빙"` \| `"모델 학습"` / 백엔드: `"CI/CD"` \| `"DevOps"` \| `"Security"` / 해커톤: `"해커톤 후기"` |
| `generation` | `"11기"` \| `"12기"` \| `"13기"` |
| `status` | `"published"` \| `"draft"` |
| `isLiked` | 현재 로그인 유저가 좋아요 눌렀으면 `true` |
| `isBookmarked` | 현재 로그인 유저가 북마크했으면 `true` |
| `content` | 목록 조회(`GET /api/posts`) 응답에는 포함되지 않음. 단건 조회(`GET /api/posts/:id`)에만 포함 |

#### 회원 (Member)

```json
{
  "id": 1,
  "name": "장찬욱",
  "email": "chanwook@khu.ac.kr",
  "generation": "13기",
  "role": "ADMIN",
  "bio": "경희대 컴퓨터공학과 3학년.",
  "github": "https://github.com/chanwook",
  "blog": "https://chanwook.kr",
  "linkedin": "https://linkedin.com/in/chanwook",
  "postCount": 12,
  "joinDate": "2026-03-03"
}
```

| 필드 | 가능한 값 |
|------|-----------|
| `role` | `"ADMIN"` \| `"MEMBER"` \| `"PENDING"` |

#### 댓글 (Comment)

```json
{
  "id": 1,
  "content": "댓글 내용",
  "author": {
    "id": 2,
    "name": "노희윤"
  },
  "likeCount": 2,
  "isLiked": false,
  "parentId": null,
  "replies": [],
  "createdAt": "2026-03-29"
}
```

| 필드 | 설명 |
|------|------|
| `parentId` | 일반 댓글이면 `null`, 대댓글이면 부모 댓글의 `id` |
| `replies` | 이 댓글에 달린 대댓글 목록 |

---

### 인증 API

#### 로그인

```
POST /api/auth/login
```

요청:
```json
{ "email": "chanwook@khu.ac.kr", "password": "비밀번호" }
```

응답:
```json
{
  "token": "eyJhbGci...",
  "user": {
    "id": 1,
    "name": "장찬욱",
    "role": "ADMIN"
  }
}
```

> 받은 `token`은 `localStorage.setItem("token", ...)` 으로 저장합니다.  
> 받은 `user`는 `localStorage.setItem("user", JSON.stringify(...))` 으로 저장합니다.  
> **세 명 모두 키 이름을 `"token"`, `"user"` 로 통일합니다.**

#### 회원가입

```
POST /api/auth/register
```

요청:
```json
{
  "name": "장찬욱",
  "email": "chanwook@khu.ac.kr",
  "password": "비밀번호",
  "generation": "13기"
}
```

응답:
```json
{ "message": "가입 신청이 완료되었습니다. 관리자 승인 후 로그인할 수 있습니다." }
```

#### 이메일 중복 확인

```
GET /api/auth/check-email?email=chanwook@khu.ac.kr
```

응답:
```json
{ "available": true }
```

#### 로그아웃

별도 API 없음. 클라이언트에서 아래 두 가지만 삭제하면 됩니다.

```js
localStorage.removeItem("token")
localStorage.removeItem("user")
```

---

### 공개 통계 API

MainPage 히어로 섹션의 숫자 (멤버 수, 게시글 수, 활동 기수)에 사용합니다.  
로그인 없이도 접근 가능합니다.

```
GET /api/stats
```

응답:
```json
{
  "memberCount": 24,
  "postCount": 47,
  "activeGenerations": 3
}
```

---

### 게시글 API

#### 게시글 목록 조회

```
GET /api/posts?board=백엔드&category=CI/CD&generation=13기&authorId=1&keyword=Spring&page=1
```

파라미터는 모두 선택 사항입니다. 없으면 전체 조회입니다.

| 파라미터 | 설명 |
|----------|------|
| `board` | 대분류 필터 (`"AI"` \| `"백엔드"` \| `"해커톤"`) |
| `category` | 소분류 필터 (`"LLM"`, `"CI/CD"` 등) |
| `generation` | 기수 필터 (`"13기"` 등) |
| `authorId` | 작성자 ID 필터 (이름이 아니라 **ID**) |
| `keyword` | 제목 · 태그 검색어 |
| `page` | 페이지 번호 (기본값 1) |

응답:
```json
{
  "posts": [ /* Post 목록 (content 필드 없음, preview만 포함) */ ],
  "totalCount": 47,
  "totalPages": 5,
  "currentPage": 1
}
```

#### 게시글 단건 조회

```
GET /api/posts/:id
```

응답: Post 전체 (위의 게시글 구조 그대로)

#### 게시글 작성

```
POST /api/posts
```

요청:
```json
{
  "title": "제목",
  "content": "마크다운 본문",
  "board": "백엔드",
  "category": "CI/CD",
  "tags": ["Spring Boot"],
  "generation": "13기",
  "status": "draft"
}
```

응답: 생성된 Post 전체

#### 게시글 수정

```
PUT /api/posts/:id
```

요청: 작성과 동일 (수정할 필드만 보내도 됨)  
응답: 수정된 Post 전체

#### 게시글 삭제

```
DELETE /api/posts/:id
```

응답:
```json
{ "message": "삭제되었습니다." }
```

#### 좋아요 토글

```
POST /api/posts/:id/like
```

응답:
```json
{ "isLiked": true, "likeCount": 15 }
```

#### 북마크 토글

```
POST /api/posts/:id/bookmark
```

응답:
```json
{ "isBookmarked": true, "bookmarkCount": 8 }
```

---

### 댓글 API

#### 댓글 목록 조회

```
GET /api/posts/:id/comments
```

응답:
```json
{ "comments": [ /* Comment 목록 */ ] }
```

#### 댓글 작성

```
POST /api/posts/:id/comments
```

요청:
```json
{
  "content": "댓글 내용",
  "parentId": null
}
```

응답: 생성된 Comment 전체

#### 댓글 좋아요 토글

```
POST /api/comments/:id/like
```

응답:
```json
{ "isLiked": true, "likeCount": 3 }
```

---

### 회원 API

#### 회원 목록 조회

```
GET /api/members?keyword=장찬욱
```

`keyword`는 선택 사항입니다. 이름 또는 이메일로 검색합니다 (어드민 회원 검색에 사용).

응답:
```json
{ "members": [ /* Member 목록 */ ] }
```

#### 회원 프로필 조회

```
GET /api/members/:id
```

응답: Member 전체

#### 특정 회원의 게시글 목록

```
GET /api/members/:id/posts?status=published
```

응답:
```json
{ "posts": [ /* Post 목록 */ ], "totalCount": 12 }
```

#### 내 북마크 목록

```
GET /api/members/:id/bookmarks
```

응답:
```json
{ "posts": [ /* 북마크한 Post 목록 */ ] }
```

---

### 어드민 API

#### 대시보드 통계

```
GET /api/admin/stats
```

응답:
```json
{
  "memberCount": 24,
  "pendingCount": 3,
  "postCount": 47,
  "monthlyPostCount": 12
}
```

#### 게시글 전체 조회 (어드민 전용)

일반 `GET /api/posts`와 다르게, 모든 회원의 draft 글까지 포함해서 조회합니다.

```
GET /api/admin/posts?status=draft&keyword=Spring&page=1
```

응답:
```json
{
  "posts": [ /* Post 목록 (draft 포함) */ ],
  "totalCount": 52,
  "totalPages": 6,
  "currentPage": 1
}
```

#### 가입 승인 대기 목록

```
GET /api/admin/pending
```

응답:
```json
{ "members": [ /* PENDING 상태 Member 목록 */ ] }
```

#### 가입 승인

```
POST /api/admin/members/:id/approve
```

응답:
```json
{ "message": "승인되었습니다." }
```

#### 가입 거절

```
POST /api/admin/members/:id/reject
```

응답:
```json
{ "message": "거절되었습니다." }
```

#### 회원 Role 변경

```
PATCH /api/admin/members/:id/role
```

요청:
```json
{ "role": "ADMIN" }
```

응답: 변경된 Member 전체

---

## 브랜치 전략

`blog` 브랜치가 이 팀의 기준 브랜치입니다. 작업은 아래 규칙을 따릅니다.

### 브랜치 명명 규칙

```
<type>/<작업-내용>
```

| type | 사용 시점 | 예시 |
|------|-----------|------|
| `feat` | 새 기능 구현 | `feat/detail-page-api` |
| `fix` | 버그 수정 | `fix/login-redirect-loop` |
| `refactor` | 기능 변경 없는 코드 정리 | `refactor/board-filter-logic` |
| `test` | 테스트 코드 추가 / 수정 | `test/write-page-validation` |
| `style` | CSS / 스타일 변경 | `style/header-mobile-layout` |
| `docs` | 문서 수정 | `docs/readme-update` |
| `chore` | 빌드 설정, 의존성 추가 등 | `chore/add-axios` |

### 플로우

```
blog (기준 브랜치)
  └─ feat/detail-page-api      ← 작업 브랜치
       └─ Pull Request → blog  ← 리뷰 후 머지
```

1. `blog` 브랜치에서 작업 브랜치를 분기
2. 작업 완료 후 `blog`로 Pull Request
3. 팀장(장찬욱) 코드 리뷰 후 머지
4. **`main`에 직접 푸시 금지** — `blog`에서 팀 작업 완료 후 루트 PR로 반영

### 커밋 메시지

```
<type>: <변경 내용 요약>

예)
feat: 게시글 목록 API 연동 (페이지네이션 포함)
fix: 로그인 후 홈 리디렉트 안 되는 버그 수정
```

---

## 개발 환경 설정

### 프론트엔드

```bash
cd blog/frontend
npm install
npm run dev      # localhost:3000
npm run build    # 프로덕션 빌드
```

개발 시 `/api` 요청은 `localhost:8080`으로 자동 프록시됩니다 (`vite.config.js` 설정).

### 백엔드 (Spring Boot + Amazon RDS PostgreSQL)

DB 연결은 GitHub Secrets에 등록된 환경 변수를 Spring이 읽는 방식으로 동작합니다.

**흐름**
```
GitHub Secrets (DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD)
  └─ CI/CD 워크플로우에서 환경 변수로 주입
       └─ app/src/main/resources/application.yml 에서 참조
```

`application.yml`은 아래처럼 `${}` 플레이스홀더로 환경 변수를 읽습니다:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

따라서 새로운 DB 관련 설정을 `application.yml`에 추가할 때도 값을 하드코딩하지 않고 `${변수명}` 형태로 작성하고, 해당 변수를 GitHub Secrets에 등록 요청(팀장)하면 됩니다.

#### 1. 애플리케이션 실행

```bash
# 프로젝트 루트에서
./gradlew :app:bootRun

# blog 모듈만 빌드
./gradlew :blog:build
```

#### 2. DB 스키마 관리

`application.yml`의 `ddl-auto: update` 설정으로 엔티티 변경 시 스키마가 자동 반영됩니다.  
단, **컬럼 삭제는 자동으로 반영되지 않으므로** 직접 쿼리가 필요합니다.

---

## 모듈 의존 규칙

루트 [README](../README.md)의 "모듈 간 통신" 섹션을 반드시 읽어주세요.

- 다른 팀 모듈의 기능이 필요하면 `contract/`에 Port 인터페이스 정의 후 팀 전체 리뷰
- `blog/` 내부 코드는 `blog/src/` 아래에서만 작업
- `app/`, `common/`, `contract/`는 임의 수정 금지 (팀장 확인 필요)
