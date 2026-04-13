# ID 타입 컨벤션

엔티티 PK, 서비스 파라미터, DTO — **ID가 들어가는 곳은 전부 `Long`**.

```java
// 이렇게
public PostDto getPost(Long postId) { ... }
void deleteComment(Long commentId);

// 이거 안 됨
public PostDto getPost(String postId) { ... }    // String 안 됨
public PostDto getPost(Integer postId) { ... }   // Integer도 안 됨
```

왜? DB의 `BIGINT`와 1:1 매핑되고, 팀 전체가 타입을 통일해야 모듈 간 포트에서 혼란이 없다.
