# API 응답 형식 컨벤션

## 성공 응답

별도 래퍼 없이, **데이터를 그대로 반환**한다.

```java
// 단일 조회
@GetMapping("/posts/{id}")
public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
    return ResponseEntity.ok(postService.getPost(id));
}

// 목록 조회
@GetMapping("/posts")
public ResponseEntity<List<PostResponse>> getPosts() {
    return ResponseEntity.ok(postService.getPosts());
}

// 생성 — 201 Created
@PostMapping("/posts")
public ResponseEntity<PostResponse> createPost(...) {
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
}

// 삭제 — 204 No Content
@DeleteMapping("/posts/{id}")
public ResponseEntity<Void> deletePost(@PathVariable Long id) {
    postService.deletePost(id);
    return ResponseEntity.noContent().build();
}
```

왜 래퍼를 안 쓰나? `{ "status": 200, "data": { ... } }` 같은 래퍼는 HTTP 상태 코드와 중복이다. 프론트엔드에서 `response.status`로 이미 구분할 수 있는 걸 body에 또 넣을 필요 없다.

## 에러 응답

모든 에러는 **같은 형식**으로 내려간다.

```json
{
  "status": 401,
  "message": "이메일 또는 비밀번호가 올바르지 않습니다"
}
```

- `status` — HTTP 상태 코드 (숫자)
- `message` — 사용자에게 보여줄 수 있는 한국어 메시지

## HTTP 상태 코드

| 코드 | 언제 |
|------|------|
| `200 OK` | 조회, 수정 성공 |
| `201 Created` | 새 리소스 생성 |
| `204 No Content` | 삭제 성공 (body 없음) |
| `400 Bad Request` | 요청 값 검증 실패 (`@Valid`) |
| `401 Unauthorized` | 인증 실패 (토큰 없음/만료/잘못됨) |
| `403 Forbidden` | 인증은 됐는데 권한 없음 |
| `404 Not Found` | 리소스 없음 |
| `409 Conflict` | 중복 (이미 존재하는 이메일 등) |

헷갈리는 거: **401 vs 403**
- 401 = "너 누군지 모르겠어" (로그인 안 했거나 토큰 만료)
- 403 = "누군진 아는데 권한이 없어" (MEMBER가 ADMIN 전용 API 호출)
