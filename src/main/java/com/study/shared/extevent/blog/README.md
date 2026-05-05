# blog 통합 이벤트

| 이벤트 | consumer | publish 위치 |
|---|---|---|
| `BlogPostCreated` | profile · `ActivityEventListener` | `PostService.createPost` (글 작성 시) |

작성 규칙: [docs/conventions/통합-이벤트.md](../../../../../../../../docs/conventions/통합-이벤트.md)
