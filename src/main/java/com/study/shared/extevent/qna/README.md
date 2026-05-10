# qna 통합 이벤트

QnA BC가 발행하는 도메인 사건 record.

| record | 발행 시점 | 시그니처 |
|---|---|---|
| `QnaQuestionCreated` | 질문 저장 직후 | `(userId, questionId)` |
| `QnaQuestionDeleted` | 질문 삭제 직후 | `(userId, questionId)` |
| `QnaAnswerCreated` | 답변 저장 직후 | `(userId, answerId)` |
| `QnaAnswerDeleted` | 답변 삭제 직후 | `(userId, answerId)` |
| `QnaAnswerAccepted` | 답변 채택 시 | `(userId, answerId)` ※ userId = 답변 작성자 |
| `QnaAnswerUnaccepted` | 채택 취소 시 | `(userId, answerId)` ※ userId = 답변 작성자 |
| `QnaCommentCreated` | 댓글 저장 직후 | `(userId, commentId)` |
| `QnaCommentDeleted` | 댓글 삭제 직후 | `(userId, commentId)` |

작성 규칙: [docs/conventions/통합-이벤트.md](../../../../../../../../docs/conventions/통합-이벤트.md)
