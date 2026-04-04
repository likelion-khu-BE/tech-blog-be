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

#### 1. `.env` 파일 생성

프로젝트 **루트**에 `.env` 파일을 만들고 DB 접속 정보를 입력합니다.  
(`.env`는 `.gitignore`에 포함되어 있어 레포에 올라가지 않습니다.)

```bash
# 프로젝트 루트에서
cp .env.example .env
```

`.env` 파일을 열고 아래 값을 채웁니다:

```dotenv
DB_HOST=<RDS 엔드포인트>      # ex) mydb.xxxx.ap-northeast-2.rds.amazonaws.com
DB_PORT=5432
DB_NAME=<데이터베이스 이름>
DB_USERNAME=<DB 사용자명>
DB_PASSWORD=<DB 비밀번호>
```

> RDS 접속 정보는 팀장(장찬욱)에게 문의하세요.

#### 2. RDS 보안 그룹 확인

AWS 콘솔에서 해당 RDS 인스턴스의 **보안 그룹 인바운드 규칙**에 본인 IP(또는 개발 서버 IP)가 허용되어 있어야 합니다.
- 포트: `5432` (PostgreSQL)
- 접속이 안 될 경우 팀장에게 IP 추가 요청

#### 3. 애플리케이션 실행

```bash
# 프로젝트 루트에서
./gradlew :app:bootRun

# blog 모듈만 빌드
./gradlew :blog:build
```

#### 4. DB 스키마 관리

`application.yml`의 `ddl-auto: update` 설정으로 엔티티 변경 시 스키마가 자동 반영됩니다.  
단, **컬럼 삭제는 자동으로 반영되지 않으므로** 직접 쿼리가 필요합니다.

---

## 모듈 의존 규칙

루트 [README](../README.md)의 "모듈 간 통신" 섹션을 반드시 읽어주세요.

- 다른 팀 모듈의 기능이 필요하면 `contract/`에 Port 인터페이스 정의 후 팀 전체 리뷰
- `blog/` 내부 코드는 `blog/src/` 아래에서만 작업
- `app/`, `common/`, `contract/`는 임의 수정 금지 (팀장 확인 필요)
