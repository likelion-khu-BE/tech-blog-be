# sessionboard 통합 이벤트

| 이벤트 | consumer | publish 위치 |
|---|---|---|
| `SessionEventPostCreated` | profile · `ActivityEventListener` | 행사 게시글 작성 시 (`EventPost` 엔티티 생성 시점) |
| `SessionSpeakerRegistered` | profile · `ActivityEventListener` | 발표자 등록 시 (`SessionSpeaker` 엔티티 생성 시점) |

작성 규칙: [docs/conventions/통합-이벤트.md](../../../../../../../../docs/conventions/통합-이벤트.md)
