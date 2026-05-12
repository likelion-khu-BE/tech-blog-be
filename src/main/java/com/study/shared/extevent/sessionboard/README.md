# sessionboard 통합 이벤트

sessionboard BC가 발행하는 도메인 사건 record.

| record | 발행 시점 | 시그니처 |
|---|---|---|
| `SessionEventPostCreated` | EventPost 저장 직후 | `(userId, postId)` |
| `SessionEventPostDeleted` | EventPost 삭제 직후 | `(userId, postId)` |
| `SessionEventCommentCreated` | EventPostComment 저장 직후 | `(userId, commentId)` |
| `SessionEventCommentDeleted` | EventPostComment 삭제 직후 | `(userId, commentId)` |
| `SessionEventPostLiked` | 좋아요 켜질 때 | `(likerId, postId, postOwnerId)` |
| `SessionEventPostUnliked` | 좋아요 꺼질 때 | `(likerId, postId, postOwnerId)` |
| `SessionSpeakerRegistered` | 발표자 등록 시 | `(userId, sessionId)` |
| `SessionSpeakerUnregistered` | 발표자 등록 취소 시 | `(userId, sessionId)` |

작성 규칙: [docs/conventions/통합-이벤트.md](../../../../../../../../docs/conventions/통합-이벤트.md)
