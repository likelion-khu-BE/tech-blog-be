# qna 통합 이벤트

| 이벤트 | consumer | publish 위치 |
|---|---|---|
| `QnaQuestionCreated` | profile · `ActivityEventListener` | 질문 작성 시 |
| `QnaAnswerCreated` | profile · `ActivityEventListener` | 답변 작성 시 |
| `QnaAnswerAccepted` | profile · `ActivityEventListener` | 답변 채택 시 (채택된 답변 작성자가 점수 받음) |

작성 규칙: [docs/conventions/통합-이벤트.md](../../../../../../../../docs/conventions/통합-이벤트.md)
