# MemberQueryService — 외부 BC가 작성자 정보 가져가기

> 외부 BC(blog/qna/sessionboard)가 응답에 작성자 정보(이름·프사)를 표시할 때 호출하는 빈.
> 같은 jar 내 Spring 빈 직접 의존 (HTTP X).

## 기본 사용법

`MemberQueryService` 빈을 주입받아 호출.

```java
@Service
@RequiredArgsConstructor
public class BlogPostService {
    private final MemberQueryService memberQueryService;

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(...);
        MemberSummaryDto author = memberQueryService.getById(post.getUserId())
            .orElseThrow(() -> new IllegalStateException("작성자 프로필 없음"));
        return PostResponse.of(post, author);
    }
}
```

## 인터페이스

```java
public interface MemberQueryService {
    Optional<MemberSummaryDto> getById(Long userId);
}

public record MemberSummaryDto(
    Long memberId,           // 프로필 페이지 라우팅용 (Member.id)
    String name,
    String profileImageUrl   // 없으면 null
) {}
```

## 응답 처리

| 상황 | 응답 | 호출부가 결정 |
|---|---|---|
| Member 있음 | `Optional.of(dto)` | 정상 사용 |
| Member 없음 | `Optional.empty()` | throw / 기본 표시 / skip 등 자체 정책 |

`Member 없음` = profile-init 미완료 가능성. `auth.User`는 있으나 프로필 등록 안 한 상태.

## 응답 활용

```json
{
  "id": 42,
  "title": "Spring 입문",
  "author": {
    "memberId": 1,
    "name": "임근엽",
    "profileImageUrl": "https://..."
  }
}
```

- `name`, `profileImageUrl` — 화면 표시
- `memberId` — 프로필 페이지 `/profile/members/{memberId}` 링크용. 도메인 의미 모르고 라우팅 토큰으로만 박기

## 이건 하면 안 됨

```java
// X — Repository 직접 의존 (BC 격리 위반)
@Autowired private MemberRepository memberRepository;
Member m = memberRepository.findByUserId(userId).orElseThrow(...);

// X — HTTP 호출 (같은 jar 안에서 불필요한 오버헤드)
restTemplate.getForObject("/api/profile/members/" + memberId, ...);
```

→ 항상 `MemberQueryService` 인터페이스를 통해 접근. profile 도메인 모델·Repository에 직접 의존 X.

## 식별자 단 분리

| 단 | 식별자 | 출처/용도 |
|---|---|---|
| **입력** | `userId` | `auth.User.id` — auth는 전 BC 공유 BC라 어디서나 안정적인 키 |
| **응답** | `memberId` | `Member.id` — profile 내부 정체성. 외부 BC는 라우팅 토큰으로만 사용 |

User ↔ Member는 1:1이지만 의미·수명·책임이 다름:
- User: 인증 식별자 (계정·비밀번호·승인 상태) — auth 소유, 전 BC 공유
- Member: 프로필 도메인 식별자 (이름·프사) — profile 내부

외부 BC는 `userId`만 알면 충분. `memberId`는 응답으로 받아 라우팅에만 사용.

## 다건 호출 (목록 화면)

현재 단건(`getById`)만 제공. 목록 화면에서 N명 작성자 표시는 호출부 책임.

```java
// 호출부에서 루프 — N+1 발생 가능
posts.forEach(p -> memberQueryService.getById(p.getUserId()));
```

비효율이 실제 문제로 드러나면 profile 팀에 요청 — 다건 메서드(`getByIds`) / 캐싱 / projection 추가 검토. *purely additive* 추가라 외부 BC 깨지지 않음.

## 왜 빈 직접 의존인가

같은 jar 모놀리식 + BC 격리 환경의 표준 통신 방법:

- **성능**: 같은 프로세스 내라 HTTP 오버헤드 불필요 (메서드 호출 μs vs HTTP ms)
- **컴파일 타임 안전성**: 인터페이스 시그니처 변경 시 외부 BC 즉시 컴파일 오류
- **트랜잭션 일관성**: Spring `@Transactional` 자동 전파 → 외부 BC 트랜잭션과 같은 컨텍스트
- **분산 전환 옵션**: 인터페이스를 추후 HTTP 어댑터로 갈아끼우면 됨 (Adapter 패턴)
