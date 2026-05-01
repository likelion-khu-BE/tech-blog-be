# Activity 이벤트 — 외부 BC가 활동 사실 알리기

> blog/qna/sessionboard에서 사용자 활동(글·댓글·답변 채택 등) 발생 시 Spring 도메인 이벤트로 발행.
> profile이 자동 구독해서 활동 이력·점수에 반영.
> 같은 jar 내 `ApplicationEventPublisher` — profile 함수 직접 호출 X.

## 팀별 작업 가이드

자기 서비스에 `ApplicationEventPublisher` 주입 후, 활동 시점마다 한 줄 발행하면 끝.

### blog 팀

```java
import com.study.shared.event.BlogPostCreated;
import com.study.shared.event.BlogCommentCreated;

// 글 작성 후 (PostService.createPost 등)
eventPublisher.publishEvent(new BlogPostCreated(userId, post.getId()));

// 댓글 작성 후 (CommentService.createComment 등)
eventPublisher.publishEvent(new BlogCommentCreated(userId, comment.getId()));
```

### qna 팀

```java
import com.study.shared.event.QnaQuestionCreated;
import com.study.shared.event.QnaAnswerCreated;
import com.study.shared.event.QnaAnswerAccepted;

// 질문 등록 후
eventPublisher.publishEvent(new QnaQuestionCreated(userId, question.getId()));

// 답변 작성 후
eventPublisher.publishEvent(new QnaAnswerCreated(userId, answer.getId()));

// 답변 채택 시 — userId는 '채택된 답변자' (질문자 아님)
eventPublisher.publishEvent(new QnaAnswerAccepted(answer.getAuthorId(), answer.getId()));
```

### sessionboard 팀

```java
import com.study.shared.event.SessionSpeakerRegistered;
import com.study.shared.event.SessionNoteCreated;
import com.study.shared.event.EventPostCreated;
import com.study.shared.event.EventPostCommentCreated;

// 발표자 등록 후 (SessionSpeaker)
eventPublisher.publishEvent(new SessionSpeakerRegistered(userId, session.getId()));

// 발표 노트 작성 후 (SessionNote)
eventPublisher.publishEvent(new SessionNoteCreated(userId, note.getId()));

// 행사 게시글 작성 후 (EventPost)
eventPublisher.publishEvent(new EventPostCreated(userId, post.getId()));

// 행사 게시글 댓글 작성 후 (EventPostComment)
eventPublisher.publishEvent(new EventPostCommentCreated(userId, comment.getId()));
```

## 공통 주의사항

1. **`@Transactional` 메서드 안에서 발행** — 트랜잭션 자동 전파로 정합성 보장 (글 저장 실패 시 활동도 같이 롤백)
2. **`userId`는 활동 주체** — `@CurrentUser`로 받은 값 그대로
3. **점수는 박지 말 것** — profile 정책. 외부 BC는 *사실*만 발행

## 이건 하면 안 됨

```java
// X — profile 도메인 직접 의존 (BC 격리 위반)
activityRecorder.record(userId, ActivityType.blog_post, postId);

// X — async (@Async) 사용 → 트랜잭션 분리 → 정합성 깨짐
@Async @EventListener
void onBlogPostCreated(...) {}
```

## 트랜잭션 동작

Spring 기본 `@EventListener` 사용 — sync + 같은 트랜잭션 + listener 실패 시 publisher 롤백. "글은 저장됐는데 활동 누락" 같은 깨진 상태 안 생김.

## 왜 이벤트 발행인가

- 외부 BC는 profile 존재 모름 (자기 사실만 발행)
- 미래에 알림/통계 모듈 추가돼도 외부 BC 코드 0줄 변경
- 점수 정책 변경해도 외부 코드 영향 X
