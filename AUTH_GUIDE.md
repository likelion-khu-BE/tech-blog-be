# 인증/인가 사용 가이드

> 작성: 김우진 (인프라/인증 담당)
> 최종 수정: 2026-04-12

이 문서는 내가 구현한 인증/인가 시스템을 팀원들이 각자 모듈에서 쓸 때 필요한 내용만 정리한 거다.
모르겠으면 이 파일 읽으라고 하면 된다. 내부 구현은 몰라도 되고, 여기 있는 인터페이스만 쓰면 된다.

---

## 전체 그림

```
[클라이언트]
    │
    ├── POST /api/auth/signup     회원가입 (PENDING 상태, 로그인 불가)
    ├── POST /api/auth/login      → access token (응답 body) + refresh token (HttpOnly 쿠키)
    ├── GET  /api/whatever         Authorization: Bearer {access token}
    ├── POST /api/auth/refresh    → 새 access token + 새 refresh token (자동 rotation)
    └── POST /api/auth/logout     → refresh token 폐기 + 쿠키 삭제
```

- access token은 15분짜리. 만료되면 refresh 호출.
- refresh token은 쿠키로 자동 관리되니까 프론트가 직접 다룰 일 없다.
- 회원가입하면 PENDING이고, 내가 승인해야 ACTIVE → 로그인 가능.

---

## 팀 모듈에서 쓰는 법

### 1. 컨트롤러에서 현재 유저 받기 — `@CurrentUser`

```java
import com.study.common.security.CurrentUser;
import com.study.common.security.CustomUserDetails;

@GetMapping("/my-posts")
public List<PostDto> getMyPosts(@CurrentUser CustomUserDetails user) {
    Long userId = user.userId();
    UserRole role = user.role();
    // ...
}
```

- `@CurrentUser`를 파라미터에 붙이면 현재 로그인된 유저 정보가 들어온다.
- `userId()` → Long (DB PK), `role()` → `ADMIN` 또는 `MEMBER`
- 인증 안 된 요청이면 여기까지 오기 전에 401이 나간다. null 체크 필요 없다.

### 2. 메서드 레벨 권한 제어 — `@PreAuthorize`

```java
import org.springframework.security.access.prepost.PreAuthorize;

// ADMIN만 호출 가능
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public void deletePost(@PathVariable Long id) { ... }

// ADMIN, MEMBER 둘 다 가능
@PostMapping
@PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
public PostDto createPost(@RequestBody CreatePostRequest req) { ... }

// 인증만 되어 있으면 누구나 (기본 동작이라 생략해도 됨)
@GetMapping
public List<PostDto> list() { ... }
```

- `hasRole('ADMIN')` → ADMIN만
- `hasAnyRole('ADMIN', 'MEMBER')` → 둘 다
- 권한 없으면 403 Forbidden이 자동으로 나간다.

### 3. 서비스 레이어에서 현재 유저 조회 — `SecurityUtils`

```java
import com.study.common.security.SecurityUtils;

@Service
public class PostService {

    public Post createPost(CreatePostRequest req) {
        Long authorId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        // ...
    }
}
```

| 메서드 | 반환 | 설명 |
|---|---|---|
| `getCurrentUserId()` | `Long` | 현재 유저 PK. 미인증 시 예외 |
| `getCurrentUserRole()` | `UserRole` | ADMIN 또는 MEMBER. 미인증 시 예외 |
| `getCurrentUser()` | `CustomUserDetails` | 전체 정보. 미인증 시 null |
| `isAuthenticated()` | `boolean` | 인증 여부 |
| `isAdmin()` | `boolean` | ADMIN 여부 |

컨트롤러에서는 `@CurrentUser`, 서비스에서는 `SecurityUtils`. 이렇게 나눠 쓰면 된다.

---

## 역할 (Role)

| 역할 | 설명 |
|---|---|
| `MEMBER` | 일반 사용자. 가입 시 기본값 |
| `ADMIN` | 관리자. 유저 승인/반려, 관리 기능 접근 |

---

## 하지 말 것

1. **common 모듈의 security/entity/exception 패키지 직접 수정하지 마라.** 인증 관련 코드는 내가 관리한다. 필요한 게 있으면 말해.
2. **SecurityContextHolder를 직접 건드리지 마라.** `SecurityUtils`나 `@CurrentUser` 쓰면 된다.
3. **JWT를 직접 파싱하거나 생성하지 마라.** 토큰 관련은 전부 app 모듈의 AuthService가 담당.
4. **refresh token을 코드에서 직접 다루지 마라.** 쿠키로 자동 관리된다.
5. **비밀번호를 로그에 찍거나 DTO의 toString에 포함하지 마라.**

---

## 엔드포인트 정리

| Method | URL | 인증 필요 | 설명 |
|---|---|---|---|
| POST | `/api/auth/signup` | X | 회원가입 |
| POST | `/api/auth/login` | X | 로그인 → access token + refresh 쿠키 |
| POST | `/api/auth/refresh` | X (쿠키) | 토큰 갱신 |
| POST | `/api/auth/logout` | X (쿠키) | 로그아웃 + 쿠키 삭제 |
| 그 외 `/api/**` | | O | Bearer 토큰 필요 |

---

## 응답 코드

| 코드 | 의미 | 언제 나오나 |
|---|---|---|
| 401 | 인증 실패 | 토큰 없음, 만료, 변조 |
| 403 | 권한 부족 | MEMBER가 ADMIN 전용 API 호출 |
| 409 | 이메일 중복 | 이미 가입된 이메일로 signup |

---

## 궁금한 거

구현 관련 질문은 김우진한테.
`common/security/`, `app/config/`, `app/auth/` 코드를 보면 답이 있을 수도 있다.
