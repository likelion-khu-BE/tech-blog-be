# 블로그팀 설계 문서 (v3)

> **v3 변경:** 전체 API prefix `/api/blog/` 적용. CORS 설계 섹션 추가.

---

## 1. 팀원별 기능 목록

### 장찬욱 (팀장)

#### Header 컴포넌트
- 로그인 상태에 따라 로그인/로그아웃 버튼 토글
- 로그인된 사용자 아바타 및 이름 표시
- ADMIN일 때 어드민 메뉴 노출
- 설정 링크 노출
- 로그아웃 시 localStorage 제거 + 홈 리디렉트

#### AdminPage (`/admin`)
- `GET /api/blog/admin/stats` → 대시보드 통계
- `GET /api/blog/admin/pending` → 승인 대기 목록
- `POST /api/blog/admin/members/:id/approve`
- `POST /api/blog/admin/members/:id/reject`
- `PATCH /api/blog/admin/members/:id/role`
- `GET /api/blog/members?keyword=` → 전체 회원 검색
- `GET /api/blog/admin/posts` → draft 포함 게시글 조회
- `DELETE /api/blog/admin/posts/:id` → 강제 삭제
- ADMIN 아니면 접근 차단 (라우터 가드)

#### DetailPage (`/post/:id`)
- `GET /api/blog/posts/:id`
- `GET /api/blog/posts/:id/comments`
- `POST /api/blog/posts/:id/like`
- `POST /api/blog/posts/:id/bookmark`
- `POST /api/blog/posts/:id/comments` (댓글 / 대댓글 parentId 포함)
- `POST /api/blog/comments/:id/like`
- `PUT /api/blog/comments/:id`
- `DELETE /api/blog/comments/:id`
- `DELETE /api/blog/posts/:id`
- 본인 글/댓글일 때만 수정/삭제 버튼 노출
- Draft 배너 본인에게만 표시
- 링크 클립보드 복사
- Repost → `/write?repostFrom=:id` 이동

---

### 노희윤

#### MainPage (`/`)
- `GET /api/blog/posts` → 최신 게시글
- `GET /api/blog/members` → 멤버 목록
- board / generation / keyword 필터
- 더 보기 → BoardPage 이동

#### BoardPage (`/board`)
- `GET /api/blog/posts` (board / category / generation / authorId / keyword / page)
- 필터 상태 관리
- 페이지네이션
- 로그인 시만 글쓰기 버튼 노출

---

### 김주연

#### WritePage / EditPage (`/write`, `/edit/:id`)
- `GET /api/blog/posts/:id` (EditPage 진입 시 기존 데이터 로드)
- `GET /api/blog/posts/:repostFrom` (Repost 진입 시)
- `POST /api/blog/posts` (status: draft / published)
- `PUT /api/blog/posts/:id`
- 마크다운 에디터 + 라이브 프리뷰
- 태그 chip 입력/삭제
- 비로그인 접근 차단

---

## 2. DB 스키마

### users

| 컬럼 | 타입 / 설명 |
|------|------------|
| id | BIGINT PK |
| email | VARCHAR(100) UNIQUE NOT NULL |
| password | VARCHAR(255) NOT NULL |
| name | VARCHAR(50) NOT NULL |
| generation | VARCHAR(10) NOT NULL — "13기" 형태 |
| role | VARCHAR(20) NOT NULL — PENDING, MEMBER, ADMIN |
| created_at | TIMESTAMP NOT NULL |

### posts

| 컬럼 | 타입 / 설명 |
|------|------------|
| id | BIGINT PK |
| user_id | BIGINT FK → users.id |
| title | VARCHAR(255) NOT NULL |
| content | TEXT NOT NULL |
| board | VARCHAR(20) NOT NULL — AI, 백엔드, 해커톤 |
| category | VARCHAR(20) NOT NULL — LLM, CI/CD 등 |
| status | VARCHAR(20) NOT NULL — DRAFT, PUBLISHED |
| tags | VARCHAR(255) — 콤마 구분 |
| generation | VARCHAR(10) NOT NULL |
| created_at | TIMESTAMP NOT NULL |
| updated_at | TIMESTAMP NOT NULL |

### comments

| 컬럼 | 타입 / 설명 |
|------|------------|
| id | BIGINT PK |
| post_id | BIGINT FK → posts.id |
| user_id | BIGINT FK → users.id |
| parent_id | BIGINT FK → comments.id — NULL=댓글, 있으면=대댓글 |
| content | TEXT NOT NULL |
| created_at | TIMESTAMP NOT NULL |

### post_likes

| 컬럼 | 타입 / 설명 |
|------|------------|
| post_id | BIGINT FK → posts.id |
| user_id | BIGINT FK → users.id |
| PK | (post_id, user_id) |

### post_bookmarks

| 컬럼 | 타입 / 설명 |
|------|------------|
| post_id | BIGINT FK → posts.id |
| user_id | BIGINT FK → users.id |
| PK | (post_id, user_id) |

### comment_likes

| 컬럼 | 타입 / 설명 |
|------|------------|
| comment_id | BIGINT FK → comments.id |
| user_id | BIGINT FK → users.id |
| PK | (comment_id, user_id) |

---

## 3. 최종 API 목록

### 게시글

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/blog/posts` | 게시글 목록 (필터/검색/페이지) |
| GET | `/api/blog/posts/:id` | 게시글 단건 조회 |
| POST | `/api/blog/posts` | 게시글 작성 |
| PUT | `/api/blog/posts/:id` | 게시글 수정 |
| DELETE | `/api/blog/posts/:id` | 게시글 삭제 (본인) |
| POST | `/api/blog/posts/:id/like` | 좋아요 토글 |
| POST | `/api/blog/posts/:id/bookmark` | 북마크 토글 |

### 댓글

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/blog/posts/:id/comments` | 댓글 목록 조회 |
| POST | `/api/blog/posts/:id/comments` | 댓글 / 대댓글 작성 |
| PUT | `/api/blog/comments/:id` | 댓글 수정 |
| DELETE | `/api/blog/comments/:id` | 댓글 삭제 |
| POST | `/api/blog/comments/:id/like` | 댓글 좋아요 토글 |

### 회원

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/blog/members` | 회원 목록 (검색 포함) |

### 어드민

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/blog/admin/stats` | 대시보드 통계 |
| GET | `/api/blog/admin/pending` | 가입 승인 대기 목록 |
| GET | `/api/blog/admin/posts` | 전체 게시글 (draft 포함) |
| DELETE | `/api/blog/admin/posts/:id` | 게시글 강제 삭제 |
| POST | `/api/blog/admin/members/:id/approve` | 가입 승인 |
| POST | `/api/blog/admin/members/:id/reject` | 가입 거절 |
| PATCH | `/api/blog/admin/members/:id/role` | Role 변경 |

---

## 4. 인증 설계 (profile팀 common/ 모듈)

> 블로그팀은 직접 구현하지 않고 `common/` 모듈에 의존. 개발 중에는 mock 인증 사용.

| 항목 | 내용 |
|------|------|
| 인증 방식 | JWT access token (body) + refresh token (HttpOnly 쿠키) |
| 비밀번호 | BCrypt |
| refresh token 저장 | DB (JPA) |
| 인가 ROLE | ADMIN, MEMBER, PENDING |
| common/ 위치 | JWT유틸, @CurrentUser 리졸버, 인증예외, User/RefreshToken 엔티티 |
| app/ 위치 | SecurityFilterChain, CORS, JwtAuthenticationFilter |
| Rotation | 재사용 감지 시 해당 사용자 전체 토큰 무효화 |

---

## 5. 버튼 흐름표

> **인증 컬럼:** `[공개]` = 비로그인 허용 / `[MEMBER]` = JWT 필요 / `[ADMIN]` = ADMIN Role 필요 / `[본인]` = 본인 리소스만

### 장찬욱 — Header

| 버튼/액션 | React | API | Spring | 인증 |
|----------|-------|-----|--------|------|
| 페이지 진입 | localStorage 토큰 확인 | — | — | 공개 |
| 로그아웃 | localStorage 제거 + 홈 이동 | — | — | MEMBER |
| 어드민 메뉴 | ADMIN일 때만 노출 | — | — | ADMIN |
| 설정 링크 | SettingPage로 이동 | — | — | MEMBER |

### 장찬욱 — AdminPage

| 버튼/액션 | React | API | Spring | 인증 |
|----------|-------|-----|--------|------|
| 페이지 진입 | ADMIN 아니면 차단 | `GET /api/blog/admin/stats` | `AdminService.getStats()` | ADMIN |
| 승인 대기 목록 | 자동 로드 | `GET /api/blog/admin/pending` | `AdminService.getPending()` | ADMIN |
| 승인 버튼 | 행 상태 변경 | `POST /api/blog/admin/members/:id/approve` | `AdminService.approve()` | ADMIN |
| 거절 버튼 | 행 제거 | `POST /api/blog/admin/members/:id/reject` | `AdminService.reject()` | ADMIN |
| Role 변경 | 뱃지 변경 | `PATCH /api/blog/admin/members/:id/role` | `AdminService.changeRole()` | ADMIN |
| 회원 검색 | 검색어 상태 | `GET /api/blog/members?keyword=` | `UserService.getMembers()` | ADMIN |
| 게시글 목록 | 자동 로드 | `GET /api/blog/admin/posts` | `AdminService.getPosts()` | ADMIN |
| 게시글 강제 삭제 | 확인 모달 | `DELETE /api/blog/admin/posts/:id` | `AdminService.forceDelete()` | ADMIN |

### 장찬욱 — DetailPage

| 버튼/액션 | React | API | Spring | 인증 |
|----------|-------|-----|--------|------|
| 페이지 진입 | 자동 로드 | `GET /api/blog/posts/:id` | `PostService.getPost()` | 공개 |
| 댓글 목록 | 자동 로드 | `GET /api/blog/posts/:id/comments` | `CommentService.getComments()` | 공개 |
| 좋아요 | 카운트 토글 | `POST /api/blog/posts/:id/like` | `PostService.toggleLike()` | MEMBER |
| 북마크 | 상태 토글 | `POST /api/blog/posts/:id/bookmark` | `PostService.toggleBookmark()` | MEMBER |
| 댓글 작성 | 입력값 전송 | `POST /api/blog/posts/:id/comments` | `CommentService.createComment()` | MEMBER |
| 대댓글 작성 | 입력값 전송 | `POST /api/blog/posts/:id/comments` | `CommentService.createComment()` | MEMBER |
| 댓글 좋아요 | 카운트 토글 | `POST /api/blog/comments/:id/like` | `CommentService.toggleLike()` | MEMBER |
| 댓글 수정 | 수정 폼 표시 | `PUT /api/blog/comments/:id` | `CommentService.updateComment()` | 본인 |
| 댓글 삭제 | 확인 모달 | `DELETE /api/blog/comments/:id` | `CommentService.deleteComment()` | 본인 |
| 게시글 삭제 | 확인 모달 | `DELETE /api/blog/posts/:id` | `PostService.deletePost()` | 본인 |
| 수정 버튼 | EditPage로 이동 | — | — | 본인 |
| 링크 공유 | 클립보드 복사 | — | — | 공개 |
| Repost | `/write?repostFrom=` 이동 | — | — | MEMBER |
| 목록으로 | BoardPage로 이동 | — | — | 공개 |

### 노희윤 — MainPage

| 버튼/액션 | React | API | Spring | 인증 |
|----------|-------|-----|--------|------|
| 페이지 진입 | 자동 로드 | `GET /api/blog/posts` | `PostService.getPosts()` | 공개 |
| 페이지 진입 | 자동 로드 | `GET /api/blog/members` | `UserService.getMembers()` | 공개 |
| 탭 전환 | 상태 변경 | `GET /api/blog/posts?board=` | `PostService.getPosts()` | 공개 |
| 기수/태그 필터 | 상태 변경 | `GET /api/blog/posts?generation=&keyword=` | `PostService.getPosts()` | 공개 |
| 더 보기 | BoardPage로 이동 | — | — | 공개 |

### 노희윤 — BoardPage

| 버튼/액션 | React | API | Spring | 인증 |
|----------|-------|-----|--------|------|
| 페이지 진입 | 자동 로드 | `GET /api/blog/posts?page=1` | `PostService.getPosts()` | 공개 |
| 탭 전환 | 상태 변경 | `GET /api/blog/posts?board=` | `PostService.getPosts()` | 공개 |
| 카테고리 필터 | 상태 변경 | `GET /api/blog/posts?category=` | `PostService.getPosts()` | 공개 |
| 기수/저자 필터 | 상태 변경 | `GET /api/blog/posts?generation=&authorId=` | `PostService.getPosts()` | 공개 |
| 검색 | Enter 상태 저장 | `GET /api/blog/posts?keyword=` | `PostService.searchPosts()` | 공개 |
| 페이지네이션 | 페이지 번호 변경 | `GET /api/blog/posts?page=` | `PostService.getPosts()` | 공개 |
| 게시글 클릭 | DetailPage로 이동 | — | — | 공개 |
| 글쓰기 버튼 | WritePage로 이동 | — | — | MEMBER |

### 김주연 — WritePage / EditPage

| 버튼/액션 | React | API | Spring | 인증 |
|----------|-------|-----|--------|------|
| 비로그인 진입 | LoginPage 리디렉트 | — | — | MEMBER |
| EditPage 진입 | 기존 데이터 로드 | `GET /api/blog/posts/:id` | `PostService.getPost()` | MEMBER |
| Repost 진입 | 원글 데이터 로드 | `GET /api/blog/posts/:repostFrom` | `PostService.getPost()` | MEMBER |
| 태그 입력/삭제 | 태그 배열 상태 관리 | — | — | MEMBER |
| Draft 저장 | 입력값 검증 | `POST /api/blog/posts (draft)` | `PostService.createPost()` | MEMBER |
| 발행 | 입력값 검증 | `POST /api/blog/posts (published)` | `PostService.createPost()` | MEMBER |
| 수정 저장 | 입력값 검증 | `PUT /api/blog/posts/:id` | `PostService.updatePost()` | 본인 |
| 취소 | BoardPage로 이동 | — | — | MEMBER |

---

## 6. 인증 설계 근거 (이슈 대응)

| 설계 항목 | 대응 이슈 | 근거 |
|----------|----------|------|
| access token → body | 이슈 1: localStorage XSS | 만료 시간이 짧아 탈취 피해 범위 제한. 수명이 짧은 토큰은 localStorage 트레이드오프 허용 가능. |
| refresh token → HttpOnly 쿠키 | 이슈 1: 저장/전달 보안 | HttpOnly=JS접근 차단(XSS), Secure=HTTPS만 전송, SameSite=Strict=타 도메인 차단(CSRF). 수명이 길어 쿠키 필수. |
| Rotation + 전체 무효화 | 이슈 2: refresh 탈취 대응 | 사용된 토큰 재사용 → DB에서 감지. 부분 무효화 시 공격자가 다른 토큰으로 계속 접근 가능하므로 전체 무효화. |
| BCrypt | 이슈 1: 비밀번호 평문 금지 | 단방향 해시 + 자동 salt. 레인보우 테이블 공격 방어. |
| ROLE: ADMIN/MEMBER/PENDING | 이슈 3: 401 vs 403 | 401=인증 없음(토큰 없음/만료), 403=인증됨 but 권한 없음. PENDING=가입 완료 but 승인 전. |
| common/ vs app/ 분리 | 이슈 3: FilterChain 구성 | JWT유틸/엔티티는 common/에서 전 팀 공유. 필터 체인은 app/에만 등록. 각 팀이 중복 구현 불필요. |

---

## 7. 인증 처리 레이어

> **백엔드 = 진짜 방어선** (없으면 데이터 탈취 가능). **프론트 = UX 방어선** (없으면 화면 깜빡임/지연). 둘 다 필요.

### 백엔드 (Spring Security + Controller-Service-Repository)

| 레이어 | 역할 | 해당 기능 | 처리 방식 |
|--------|------|----------|----------|
| Security Filter (진입 전) | JWT 유효성 검사, 401/403 반환 | 모든 인증 필요 API | JwtAuthenticationFilter에서 토큰 없음/만료 → 401, ROLE 불일치 → 403 |
| Controller | @CurrentUser로 현재 유저 주입 | 모든 인증 필요 API | @CurrentUser 어노테이션으로 현재 로그인 유저 꺼내기 (common/ 모듈 제공) |
| Service | 본인 여부 검증, 권한 예외 처리 | 댓글/게시글 수정·삭제 (본인), 어드민 기능 (ADMIN) | `post.getUserId() == currentUser.getId()`, 불일치 시 403 예외 throw |
| Repository | 인증 처리 없음 | — | 데이터 접근만 담당 |

### 프론트엔드 (React — UX 보호)

| 레이어 | 역할 | 해당 기능 | 처리 방식 |
|--------|------|----------|----------|
| Router (React Router) | 페이지 접근 차단 (UX 리디렉트) | AdminPage (ADMIN), WritePage/EditPage (MEMBER) | PrivateRoute 컴포넌트로 감싸서 토큰 없으면 /login 리디렉트, ADMIN 아니면 / 리디렉트. ※ 백엔드 없으면 URL 직접 입력 시 페이지 렌더링 후 뒤늦게 튕겨남 |
| API Interceptor (axios) | 토큰 헤더 자동 첨부, 401 핸들링 | MEMBER 이상 필요한 모든 API 호출 | 요청 시 Bearer 토큰 자동 첨부, 401 응답 → refresh 시도, refresh 실패 → 로그아웃 처리 |
| UI 컴포넌트 | 버튼/요소 조건부 렌더링 | 수정/삭제 버튼 (본인), 글쓰기 버튼 (MEMBER), 어드민 메뉴 (ADMIN), Draft 배너 (본인) | 로그인 여부/본인 여부/Role에 따라 버튼 노출 여부 결정. Router 차단과 별개로 UI 제어 |

---

## 8. CORS 설계

> CORS = 브라우저가 다른 Origin(도메인/포트)으로 요청 보낼 때 서버가 허용 여부를 알려주는 메커니즘. 서버끼리 통신엔 무관, 브라우저에서만 적용됨.

> **모놀리식 구조** — 모든 팀 API가 같은 포트에서 동작. `/api/blog/`, `/api/qna/` 등 URL로 팀 분기. CORS는 Origin만 뚫으면 전체 적용됨. profile팀이 app/ 모듈에서 통합 설정.

| 항목 | 값 | 설명 |
|------|----|------|
| 허용 Origin | `http://localhost:5173`, `https://chanwook.kr` | 요청을 보내는 프론트 주소. 브라우저는 요청 시 Origin 헤더를 자동으로 붙임. 서버가 이 목록에 없으면 브라우저가 응답을 차단. 개발(Vite 5173) + 배포(실제 도메인) 둘 다 등록 필요. ※ `allowCredentials=true` 면 `*` 사용 불가 — 명시적 URL 필수. |
| 허용 Method | `GET, POST, PUT, DELETE, PATCH, OPTIONS` | REST API에서 쓰는 전 메서드 허용. OPTIONS = Preflight 요청 메서드. **Preflight란:** 브라우저가 실제 요청 전에 "이 요청 보내도 돼?" 하고 먼저 OPTIONS로 물어보는 것. 서버가 OPTIONS를 허용해야 실제 요청이 날아감. OPTIONS 막히면 POST/PUT/DELETE 전부 실패. |
| 허용 Header | `Authorization, Content-Type, Accept` | Authorization = JWT access token 담는 헤더 (필수). Content-Type = JSON 요청 시 필요. `*` 로 전체 허용도 가능하나 `allowCredentials=true` 환경에서는 명시적으로 쓰는 게 브라우저 호환성상 더 안전. |
| allowCredentials | `true` | 현재 인증 구조에서 필수. refresh token이 HttpOnly 쿠키로 저장/전송됨. 쿠키는 credentials에 해당 — true 없으면 쿠키가 요청에 포함 안 됨. refresh token rotation이 동작하려면 반드시 true. |
| 설정 위치 | `app/ 모듈 SecurityFilterChain` | Spring Security 필터는 `@CrossOrigin`, `WebMvcConfigurer`보다 먼저 실행됨. Security 밖에서 CORS 설정하면 preflight(OPTIONS)가 JWT 검증 필터에서 401로 막힘. SecurityFilterChain 안에서 설정해야 preflight 통과 가능. app/ = common/ 부품들을 조립해 실제 실행되는 Spring Boot 앱. |

### SecurityFilterChain 역할

> HTTP 요청이 Controller 도달 전 거치는 검문소 체인

```
① CORS 필터 (preflight 처리)
→ ② JWT 검증 필터 → 토큰 없음/만료 → 401
→ ③ 권한 확인 필터 → ROLE 불일치 → 403
→ ④ Controller 도달 (비즈니스 로직 실행)
```

> CORS가 ①번이어야 하므로 Security 안에서 설정 필수.

---

## 9. 기술 스택

| 구분 | 기술 |
|------|------|
| 언어 (백엔드) | Java 21 |
| 프레임워크 | Spring Boot 3.5 |
| 빌드 | Gradle 멀티모듈 모놀리식 |
| 언어 (프론트) | React 19 + TypeScript |
| 스타일 | Tailwind CSS 4 |
| 3D | Three.js |
| 라우팅 | React Router 7 |
| 번들러 | Vite |
