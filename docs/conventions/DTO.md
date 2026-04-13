# DTO 컨벤션

## record를 쓴다

DTO는 전부 Java `record`로 만든다. class로 만들지 않는다.

```java
// 이렇게
public record CreatePostRequest(
    @NotBlank String title,
    @NotBlank String content) {}

// 이거 안 됨
public class CreatePostRequest {
    private String title;
    private String content;
    // getter, setter, equals, hashCode... 왜 이걸 직접 써야 하나
}
```

왜? record는 불변이고, getter/equals/hashCode/toString을 자동 생성한다. DTO는 데이터를 옮기는 그릇인데 거기에 보일러플레이트가 20줄씩 붙을 이유가 없다.

## Request와 Response는 분리한다

같은 record를 요청/응답에 재사용하지 않는다.

```java
// Request — 클라이언트가 보내는 것 (입력 검증 포함)
public record CreatePostRequest(
    @NotBlank String title,
    @NotBlank String content) {}

// Response — 서버가 내려주는 것 (민감 정보 제외)
public record PostResponse(
    Long id,
    String title,
    String content,
    Instant createdAt) {}
```

왜? 요청에는 `id`가 없고, 응답에는 `password`가 없다. 역할이 다른 걸 하나로 합치면 나중에 "이 필드 요청에서만 쓰는 건지 응답에서만 쓰는 건지" 헷갈린다.

## 네이밍

```
{동작}{도메인}{Request|Response}
```

```java
CreatePostRequest       // 글 생성 요청
PostResponse            // 글 응답 (조회, 생성 후 반환 등 공통)
UpdatePostRequest       // 글 수정 요청
PostListResponse        // 글 목록 응답 (단건과 구조가 다를 때)
```

- Request 앞에는 동작을 붙인다: `Create`, `Update`
- Response는 동작을 안 붙여도 된다 — 조회든 생성이든 같은 형태면 `PostResponse` 하나로 충분

## 검증 어노테이션은 Request에만

```java
public record CreatePostRequest(
    @NotBlank String title,                                    // null, 빈 문자열 불허
    @NotBlank @Size(max = 5000) String content,                // 길이 제한
    @NotNull Long categoryId) {}                               // null 불허
```

컨트롤러에서 `@Valid`를 붙여야 검증이 작동한다:

```java
@PostMapping("/posts")
public ResponseEntity<PostResponse> createPost(
        @Valid @RequestBody CreatePostRequest request) { ... }
//       ^^^^^^ 이거 빠지면 검증 안 됨
```

## 민감 정보 마스킹

비밀번호처럼 로그에 찍히면 안 되는 필드가 있으면 `toString()`을 오버라이드한다.

```java
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password) {

    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=***]";
    }
}
```
