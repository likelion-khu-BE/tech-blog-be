# MemberQueryService — 외부 BC가 작성자 정보 가져가기

> 외부 BC(blog/qna/sessionboard)가 응답에 작성자 정보(이름·프사)를 표시할 때 호출하는 빈.
> 같은 jar 내 Spring 빈 직접 의존 (HTTP X).

## 기본 사용법

`MemberQueryService` 빈을 주입받아 한 줄 호출:

```java
@Service
@RequiredArgsConstructor
public class BlogPostService {
    private final MemberQueryService memberQueryService;

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(...);
        MemberSummaryDto author = memberQueryService.getById(post.getUserId());
        return PostResponse.of(post, author);
    }
}
```

응답 JSON 예:
```json
{
  "id": 42, "title": "Spring 입문",
  "author": { "memberId": 1, "name": "임근엽", "profileImageUrl": "https://..." }
}
```

## 시그니처

```java
public class MemberQueryService {
    public MemberSummaryDto getById(Long userId);   // throws IllegalStateException
}

public record MemberSummaryDto(
    Long memberId,           // 프로필 페이지 라우팅용 (Member.id)
    String name,
    String profileImageUrl   // 없으면 null
) {}
```

## 응답 처리

| 상황 | 응답 |
|---|---|
| Member 있음 | `MemberSummaryDto` 반환 |
| Member 없음 | `IllegalStateException` throw — **시스템 불일치** (profile-init 강제 + ACTIVE 사용자 = Member 보장이라 발생하면 안 되는 상황) |

호출부는 throw를 catch하지 말고 흘려보내면 `GlobalExceptionHandler`가 일관 처리.

## DTO 활용

- `name`, `profileImageUrl` — 화면 표시
- `memberId` — 프로필 페이지 `/profile/members/{memberId}` 링크 박을 때만 사용 (도메인 의미 모르고 라우팅 토큰으로만)

## 식별자 단 분리

| 단 | 식별자 | 출처 |
|---|---|---|
| **입력** | `userId` | `auth.User.id` — 모든 BC 공유 키 (`@CurrentUser`로 받은 값 그대로) |
| **응답** | `memberId` | `Member.id` — profile 내부 정체성, 외부 BC는 라우팅 토큰으로만 사용 |

외부 BC는 `userId`만 알면 충분. `memberId`는 응답으로 받아 라우팅에만 사용.

## 이건 하면 안 됨

```java
// X — Repository 직접 의존 (BC 격리 위반)
@Autowired private MemberRepository memberRepository;
Member m = memberRepository.findByUserId(userId).orElseThrow(...);

// X — HTTP 호출 (같은 jar 안에서 불필요한 오버헤드)
restTemplate.getForObject("/api/profile/members/" + memberId, ...);
```

→ 항상 `MemberQueryService`를 통해 접근.

## 다건 호출 (목록 화면)

현재 단건만 제공. 목록 화면에서 N명 작성자 표시는 호출부 책임:

```java
// 호출부에서 루프 — N+1 발생 가능
posts.forEach(p -> memberQueryService.getById(p.getUserId()));
```

비효율이 실제 문제로 드러나면 profile 팀에 요청 — `getByIds` / 캐싱 / projection 추가 검토. *purely additive*라 외부 BC 깨지지 않음.

## 왜 빈 직접 의존인가

- **성능**: 같은 프로세스라 HTTP 오버헤드 불필요 (메서드 호출 μs vs HTTP ms)
- **컴파일 타임 안전성**: 시그니처 변경 시 외부 BC 즉시 컴파일 오류
- **트랜잭션 일관성**: Spring `@Transactional` 자동 전파 → 외부 BC 트랜잭션과 같은 컨텍스트
- **분산 전환 옵션**: 미래 분산화 시 Extract Interface로 cheap하게 추출 가능
