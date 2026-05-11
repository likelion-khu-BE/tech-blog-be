# blog 통합 이벤트

블로그 BC가 발행하는 도메인 사건 record.

| record | 발행 시점 | 시그니처 |
|---|---|---|
| `BlogPostCreated` | 글 저장 직후 | `(userId, postId)` |
| `BlogPostDeleted` | 글 삭제 직후 | `(userId, postId)` |
| `BlogCommentCreated` | 댓글 저장 직후 | `(userId, commentId)` |
| `BlogCommentDeleted` | 댓글 삭제 직후 | `(userId, commentId)` |
| `BlogPostLiked` | 좋아요 켜질 때 | `(likerId, postId, postOwnerId)` |
| `BlogPostUnliked` | 좋아요 꺼질 때 | `(likerId, postId, postOwnerId)` |

작성 규칙: [docs/conventions/통합-이벤트.md](../../../../../../../../docs/conventions/통합-이벤트.md)
