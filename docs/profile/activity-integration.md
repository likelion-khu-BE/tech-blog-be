1# Activity 이벤트 — 외부 BC가 활동 사실 알리기

> blog/qna/sessionboard에서 발생한 활동(글 작성, 댓글, 답변 채택 등)을 profile이 받아 활동 이력·점수에 반영하기 위한 통신 방법.
> 같은 jar 내 Spring 도메인 이벤트(`ApplicationEventPublisher`) — profile 함수 직접 호출 X.

## 기본 사용법

자기 도메인 사실을 이벤트로 발행. profile은 알아서 구독해서 활동 기록·점수 부여.

```java
@Service
@RequiredArgsConstructor
public class BlogPostService {
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long createPost(Long userId, CreatePostRequest req) {
        Post saved = postRepository.save(...);

        eventPublisher.publishEvent(
            new BlogPostCreated(userId, saved.getId())
        );
        return saved.getId();
    }
}
```

블로그는 *자기 사실*만 발표. 누가 듣는지(profile / 미래의 알림 모듈 / etc.) 알 필요 없음.

## 이벤트 카탈로그

`com.study.shared.event` 패키지. 행위별로 record 1개씩.

| 이벤트 | 발행자 | 매핑 entity | 의미 |
|---|---|---|---|
| `BlogPostCreated` | blog | `Post` | 블로그 글 발행 |
| `BlogCommentCreated` | blog | `Comment` | 블로그 댓글 작성 |
| `QnaQuestionCreated` | qna | `Question` | 질문 등록 |
| `QnaAnswerCreated` | qna | `Answer` | 답변 작성 |
| `QnaAnswerAccepted` | qna | `Answer` | 답변 채택됨 (`userId` = 채택된 답변자) |
| `SessionSpeakerRegistered` | sessionboard | `SessionSpeaker` | 발표자 등록 |
| `SessionNoteCreated` | sessionboard | `SessionNote` | 발표 자료/노트 작성 |
| `EventPostCreated` | sessionboard | `EventPost` | 행사 게시글 작성 |
| `EventPostCommentCreated` | sessionboard | `EventPostComment` | 행사 게시글 댓글 |

> 좋아요·북마크·자료 업로드·회고 등 추가 활동은 점수 정책 결정 후 *purely additive* 방식으로 추가 가능.

각 이벤트 record 형식:
```java
package com.study.shared.event;

public record BlogPostCreated(Long userId, Long postId) {}
public record BlogCommentCreated(Long userId, Long commentId) {}
public record QnaQuestionCreated(Long userId, Long questionId) {}
public record QnaAnswerCreated(Long userId, Long answerId) {}
public record QnaAnswerAccepted(Long userId, Long answerId) {}
public record SessionSpeakerRegistered(Long userId, Long sessionId) {}
public record SessionNoteCreated(Long userId, Long noteId) {}
public record EventPostCreated(Long userId, Long postId) {}
public record EventPostCommentCreated(Long userId, Long commentId) {}
```

## 이벤트 필드 규칙

- **`userId`** (필수): 활동 주체. `auth.User.id` — auth가 모든 BC 공유 BC라 어디서나 안정적인 키
- **`referenceId`** (필수): 활동 대상 ID (예: `postId`, `commentId`, `answerId`). profile이 활동 카드 클릭 시 라우팅용으로 저장

> 점수는 박지 말 것 — profile 정책. 외부 BC는 *사실*만 발행.

## 트랜잭션 / 에러 처리

Spring 기본 `@EventListener` 사용 — 별도 어노테이션 불필요.

| 항목 | 동작 |
|---|---|
| 발행 시점 | 트랜잭션 안 (sync 호출, 같은 스레드) |
| 트랜잭션 | publisher와 같은 트랜잭션 |
| 에러 시 | listener 실패 → publisher도 롤백 |

→ 글 저장 + 활동 기록이 한 단위. "글은 저장됐는데 활동 누락" 같은 깨진 상태 안 생김.

## 이건 하면 안 됨

```java
// X — profile 도메인 직접 의존 (BC 격리 위반, ActivityRecorder는 폐기됨)
@Autowired private ActivityRecorder activityRecorder;
activityRecorder.record(userId, ActivityType.blog_post, postId);

// X — 점수를 외부 BC가 결정 (점수 정책은 profile 책임)
eventPublisher.publishEvent(new BlogPostCreated(userId, postId, 10));  // score 박지 마세요

// X — async (@Async) 사용 (트랜잭션 분리 → 정합성 깨짐)
```

→ 자기 도메인 사실만 발행. 점수·활동 저장은 profile이 알아서.

## 새 행위 추가 절차

예: "글 좋아요 = 5점" 추가 시.

**외부 BC (블로그) 쪽**:
1. `com.study.shared.event.BlogPostLiked` record 정의 (또는 기존 거 재사용)
2. 자기 서비스에서 `eventPublisher.publishEvent(new BlogPostLiked(userId, postId))` 한 줄 추가

**profile 쪽**:
1. `ActivityType.blog_like` enum 값 추가
2. `ActivityEventListener`에 `@EventListener` 메서드 추가
3. 점수 매핑 (5점) 추가
4. 테스트

→ 외부 BC 부담은 1~2줄. 점수 정책 변경은 profile 안에서만.

## 왜 이벤트 발행인가

- **약결합**: 외부 BC는 profile 존재 모름. 자기 도메인 사실만 발행
- **다중 listener 확장**: 미래에 알림/통계 모듈 추가돼도 외부 BC 코드 0줄 변경
- **점수 정책 격리**: 외부 BC는 점수 모름. 정책 변경은 profile 안에서만
- **트랜잭션 일관성**: Spring `@EventListener` 기본 동작이 같은 트랜잭션 → 정합성 보장

이 결정에 도달한 사고 과정: [`Lim/통신방안.md`](Lim/통신방안.md)
