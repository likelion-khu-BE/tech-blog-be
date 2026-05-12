# qna 통합 이벤트

QnA BC가 발행하는 도메인 사건 record.

> 시그니처 컬럼 순서 규칙: **actor → 부모(트리 깊이 순) → 자식(자체 ID)**

| record | 발행 시점 | 시그니처 |
|---|---|---|
| `QnaQuestionCreated` | 질문 저장 직후 | `(userId, questionId)` |
| `QnaQuestionDeleted` | 질문 삭제 직후 | `(userId, questionId)` |
| `QnaAnswerCreated` | 답변 저장 직후 | `(userId, questionId, answerId)` |
| `QnaAnswerDeleted` | 답변 삭제 직후 | `(userId, questionId, answerId)` |
| `QnaAnswerAccepted` | 답변 채택 시 | `(userId, questionId, answerId)` ※ userId = 답변 작성자 |
| `QnaAnswerUnaccepted` | 채택 취소 시 | `(userId, questionId, answerId)` ※ userId = 답변 작성자 |
| `QnaAnswerUpvoted` | Vote가 upvote 상태로 진입 (신규 upvote / downvote → upvote 전환) | `(voterId, questionId, answerId)` |
| `QnaAnswerDownvoted` | Vote가 downvote 상태로 진입 (신규 downvote / upvote → downvote 전환) | `(voterId, questionId, answerId)` |
| `QnaAnswerUpvoteWithdrawn` | Vote가 upvote 상태에서 탈출 (직접 취소 / upvote → downvote 전환) | `(voterId, questionId, answerId)` |
| `QnaAnswerDownvoteWithdrawn` | Vote가 downvote 상태에서 탈출 (직접 취소 / downvote → upvote 전환) | `(voterId, questionId, answerId)` |
| `QnaCommentCreated` | 댓글 저장 직후 | `(userId, questionId, answerId, commentId)` ※ 질문 댓글이면 answerId=null, 답변 댓글이면 그 답변의 id |
| `QnaCommentDeleted` | 댓글 삭제 직후 | `(userId, questionId, answerId, commentId)` ※ 동일 |

Answer 사건 시리즈는 시그니처 일관을 위해 모두 `questionId` 포함 (cascade 트리거 이벤트도 동일).

작성 규칙: [docs/conventions/통합-이벤트.md](../../../../../../../../docs/conventions/통합-이벤트.md)
