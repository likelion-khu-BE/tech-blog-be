# S3 이미지 업로드 가이드

## 업로드 흐름

이미지 업로드는 **Presigned PUT URL** 방식을 사용한다. 서버가 파일을 중계하지 않고 클라이언트가 S3에 직접 업로드한다.

```
클라이언트                   백엔드                        S3
    │                          │                            │
    │  ① GET /presigned-urls   │                            │
    │─────────────────────────▶│                            │
    │                          │  (Rate Limit + 파일명 검증) │
    │                          │  (presigned URL 서명 생성)  │
    │  ② presignedUrl + key    │                            │
    │◀─────────────────────────│                            │
    │                                                       │
    │  ③ PUT presignedUrl (파일 직접 업로드)                 │
    │───────────────────────────────────────────────────────▶
    │                                                       │
    │  ④ POST /posts (key 포함)│                            │
    │─────────────────────────▶│                            │
    │                          │  (HeadObject 검증)         │
    │                          │  (key → DB 저장)           │
```

---

## 공통 레이어 파일 목록

도메인 팀은 아래 파일을 **직접 수정하지 않는다.** `DomainS3Client`만 사용한다.

| 파일 | 역할 |
|---|---|
| `shared/s3/DomainS3Client.java` | **팀이 직접 사용하는 진입점.** 버킷 고정 + Rate Limit + 검증 내장 |
| `shared/s3/S3Service.java` | AWS SDK 래퍼. presigned URL 생성, HeadObject, 삭제, URL 조회 |
| `shared/s3/S3UploadValidator.java` | 파일명(확장자·수) 검증 + 업로드 후 Content-Type·크기 검증 |
| `shared/s3/S3Properties.java` | `application.yml` S3 설정값 바인딩 |
| `shared/s3/S3ErrorCode.java` | S3 공통 에러 코드 (6종) |
| `shared/s3/S3Exception.java` | S3 공통 예외. `GlobalExceptionHandler`가 HTTP 응답으로 변환 |
| `shared/s3/PresignedUrlResponse.java` | presigned URL 발급 응답 DTO (`presignedUrl`, `key`) |
| `shared/ratelimit/RateLimitService.java` | 유저당 분당 5회 제한 (Bucket4j 인메모리) |
| `shared/config/S3Config.java` | `S3Client`, `S3Presigner` 빈 등록 |
| `shared/config/AsyncConfig.java` | `@EnableAsync` + `@EnableScheduling` 활성화 |

---

## DomainS3Client — 메서드 레퍼런스

| 메서드 | 호출 시점 | 내부 동작 |
|---|---|---|
| `issuePresignedUrls(userId, filenames)` | presigned URL 엔드포인트 | Rate Limit → 파일명 검증 → URL 발급 |
| `validateAndGetUrl(key)` | 게시글·자료 **저장** 직전 | HeadObject → Content-Type·크기 검증 → URL 반환. 검증 실패 시 S3 파일 자동 삭제 |
| `getFileUrl(key)` | 조회 응답 DTO 조립 | DB에 저장된 key → 공개 URL 변환 (검증 없음) |
| `delete(key)` | 게시글·자료 **삭제** 시 | S3 파일 삭제. 키 없어도 예외 없음 |

---

## 버킷 구성

버킷은 도메인별로 분리되어 있으며 **Public 읽기 버킷**이다 (조회 시 presigned GET URL 불필요).

| 도메인 | 환경변수 | `S3Properties` 필드 |
|---|---|---|
| sessionboard 이벤트 | `S3_BUCKET_EVENT` | `bucket().event()` |
| sessionboard 세션 | `S3_BUCKET_SESSION` | `bucket().session()` |
| 신규 도메인 | `S3_BUCKET_{DOMAIN}` | 필드 추가 필요 → [버킷 추가 절차](#새-도메인-버킷-추가-절차) 참고 |

---

## 백엔드 구현 — 3단계

### Step 1. Config에 DomainS3Client 빈 등록 (팀당 1회)

자신의 도메인 패키지 안에 Config 클래스를 만들고 `DomainS3Client` 빈을 등록한다.  
버킷 이름은 이 시점에 고정되어 이후 오사용이 불가능하다.

```java
// com.study.{domain}.config.{Domain}S3Config
@Configuration
public class EventS3Config {

    @Bean
    public DomainS3Client eventS3Client(
            S3Service s3Service,
            S3UploadValidator validator,
            RateLimitService rateLimitService,
            S3Properties props) {
        return new DomainS3Client(
                props.bucket().event(), s3Service, validator, rateLimitService);
    }
}
```

> 버킷이 2개 이상인 도메인은 빈을 여러 개 등록하고 `@Qualifier`로 구분한다.

### Step 2. presigned URL 컨트롤러

`issuePresignedUrls` 한 줄로 Rate Limit + 파일명 검증 + URL 발급이 보장된다.

```java
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventPresignedUrlController {

    private final DomainS3Client eventS3Client;

    @GetMapping("/presigned-urls")
    public List<PresignedUrlResponse> getPresignedUrls(
            @RequestParam List<String> filenames,
            @AuthenticationPrincipal Long userId) {

        return eventS3Client.issuePresignedUrls(userId, filenames);
    }
}
```

**에러 응답 (자동 처리)**

| 상황 | HTTP |
|---|---|
| 분당 5회 초과 | `429 Too Many Requests` |
| 허용되지 않는 확장자 | `400 Bad Request` |
| 파일 수 초과 (기본 10개) | `400 Bad Request` |

### Step 3. 서비스 레이어

#### 저장

클라이언트로부터 받은 `key`는 반드시 `validateAndGetUrl`로 검증 후 DB에 저장한다.  
검증 실패 시 S3 파일이 자동 삭제되고 `S3Exception`이 발생한다.

```java
@Transactional
public void createPost(CreatePostRequest request) {
    List<YourImage> images = new ArrayList<>();
    for (int i = 0; i < request.imageKeys().size(); i++) {
        String key = request.imageKeys().get(i);
        String url = eventS3Client.validateAndGetUrl(key); // 검증 + URL 변환
        images.add(YourImage.of(post, url, key, i));
    }
    imageRepository.saveAll(images);
}
```

**검증 실패 시 에러 응답 (자동 처리)**

| 상황 | HTTP |
|---|---|
| S3에 파일 없음 (업로드 안 됨) | `400 Bad Request` |
| 이미지가 아닌 파일 | `400 Bad Request` |
| 파일 크기 초과 (기본 10MB) | `413 Payload Too Large` |

#### 조회

DB에 저장된 `key`를 응답 DTO 조립 시 URL로 변환한다.

```java
String url = eventS3Client.getFileUrl(image.getImageKey());
```

#### 삭제

게시글·자료 삭제 시 S3 파일을 DB 삭제 전에 먼저 삭제한다.

```java
@Transactional
public void deletePost(Long postId) {
    List<YourImage> images = imageRepository.findByPostId(postId);

    images.forEach(img -> eventS3Client.delete(img.getImageKey())); // S3 먼저
    imageRepository.deleteAll(images);
    postRepository.deleteById(postId);
}
```

---

## 팀별 적용 체크리스트

### sessionboard 팀

- [ ] `EventS3Config` — `props.bucket().event()` 로 빈 등록
- [ ] `SessionS3Config` — `props.bucket().session()` 로 빈 등록
- [ ] presigned URL 컨트롤러에서 `issuePresignedUrls` 사용
- [ ] 저장 서비스에서 `validateAndGetUrl` 사용
- [ ] 삭제 서비스에서 `delete` 사용

### blog 팀

- [ ] [버킷 추가 절차](#새-도메인-버킷-추가-절차)에 따라 `blog` 버킷 추가
- [ ] `BlogS3Config` — `props.bucket().blog()` 로 빈 등록
- [ ] 나머지는 위와 동일

### qna 팀

- [ ] [버킷 추가 절차](#새-도메인-버킷-추가-절차)에 따라 `qna` 버킷 추가
- [ ] `QnaS3Config` — `props.bucket().qna()` 로 빈 등록
- [ ] 나머지는 위와 동일

### profile 팀

프로필 이미지는 단건 업로드다. `issuePresignedUrls` 대신 단건 메서드를 직접 사용한다.

- [ ] [버킷 추가 절차](#새-도메인-버킷-추가-절차)에 따라 `profile` 버킷 추가
- [ ] `ProfileS3Config` 빈 등록
- [ ] presigned URL 발급은 `S3Service.generatePresignedPutUrl`로 Content-Type 고정

```java
// Content-Type을 고정하면 AWS가 업로드 시 헤더를 강제 검증
String key = s3Service.generateKey("profile.jpg");
String presignedUrl = s3Service.generatePresignedPutUrl(
        bucket, key, "image/jpeg", s3Properties.presignedUrlExpiration());
```

---

## 새 도메인 버킷 추가 절차

**1. `S3Properties.BucketProperties`에 필드 추가**

```java
// shared/s3/S3Properties.java
public record BucketProperties(String event, String session, String blog) {}
//                                                                  ^^^^^^ 추가
```

**2. `application.yml`에 항목 추가**

```yaml
spring:
  cloud:
    aws:
      s3:
        bucket:
          blog: ${S3_BUCKET_BLOG}
```

**3. `.env`에 값 추가**

```sh
S3_BUCKET_BLOG=my-blog-bucket-name
```

**4. 도메인 Config에서 빈 등록**

```java
return new DomainS3Client(props.bucket().blog(), s3Service, validator, rateLimitService);
```

---

## 업로드 제약 기본값

`application.yml`에서 설정하며 환경변수로 오버라이드 가능하다.

| 항목 | 기본값 | 환경변수 |
|---|---|---|
| presigned URL 만료 | 180초 (3분) | `S3_PRESIGNED_URL_EXPIRATION` |
| 최대 파일 크기 | 10MB | — |
| 허용 확장자 | jpg, jpeg, png, gif, webp | — |
| 요청당 최대 파일 수 | 10개 | — |
| Rate Limit | 유저당 분당 5회 | — |

---

## 프론트엔드 연동

### Step 1 — presigned URL 요청

```
GET /api/{domain}/presigned-urls?filenames=photo.jpg&filenames=diagram.png
Authorization: Bearer {accessToken}
```

**Response 200 OK**
```json
[
  {
    "presignedUrl": "https://{bucket}.s3.ap-northeast-2.amazonaws.com/images/uuid.jpg?X-Amz-Signature=...",
    "key": "images/550e8400-e29b-41d4-a716-446655440000.jpg"
  }
]
```

### Step 2 — S3 직접 업로드 (PUT)

`presignedUrl`에 `Authorization` 헤더를 포함하면 S3 서명 불일치로 **403**이 발생한다.

```ts
await fetch(presignedUrl, {
  method: 'PUT',
  body: file,
  headers: { 'Content-Type': file.type },
})
```

### Step 3 — key 포함해서 저장 요청

```ts
const keys = presignedUrlResponses.map((r) => r.key)
await api.post('/your-domain/posts', { ...otherFields, imageKeys: keys })
```

### TypeScript 유틸

```ts
// src/lib/s3.ts
export interface PresignedUrlResponse {
  presignedUrl: string
  key: string
}

export async function uploadImages(
  files: File[],
  endpoint: string, // 예: '/api/events/presigned-urls'
): Promise<string[]> {
  const { data } = await api.get<PresignedUrlResponse[]>(endpoint, {
    params: { filenames: files.map((f) => f.name) },
  })

  await Promise.all(
    data.map(({ presignedUrl }, i) =>
      fetch(presignedUrl, {
        method: 'PUT',
        body: files[i],
        headers: { 'Content-Type': files[i].type },
      }),
    ),
  )

  return data.map((r) => r.key)
}
```

---

## .env 설정

```sh
AWS_ACCESS_KEY=...
AWS_SECRET_KEY=...
AWS_REGION=ap-northeast-2

S3_BUCKET_EVENT=...        # sessionboard 이벤트 게시글 이미지
S3_BUCKET_SESSION=...      # sessionboard 세션보드 이미지
# S3_BUCKET_BLOG=...       # blog 팀 추가 시
# S3_BUCKET_QNA=...        # qna 팀 추가 시
# S3_BUCKET_PROFILE=...    # profile 팀 추가 시

S3_PRESIGNED_URL_EXPIRATION=180  # 미설정 시 application.yml 기본값(180) 사용
```

---

## 주의사항

**presigned URL 캐시 금지**  
presigned URL은 만료 시간이 있다. 클라이언트에서 캐시하면 만료 후 업로드 실패.

**고아 파일**  
업로드 후 저장 요청 실패 시 S3에 파일이 남는다.  
현재 별도 정리 정책 없음. 추후 S3 Lifecycle 정책(`images/` 경로 7일 후 자동 삭제) 적용 예정.  
PENDING 상태 도입 시 각 도메인 팀에서 배치로 정리한다.

**삭제 순서**  
S3 삭제 → DB 삭제 순서를 지킨다.  
S3 삭제가 실패해도 재시도가 가능하지만, DB 삭제 후 S3 삭제가 실패하면 고아 파일이 된다.

**S3와 DB의 트랜잭션 일관성**  
S3는 DB 트랜잭션에 참여하지 않는다. DB 롤백이 발생해도 S3 파일은 롤백되지 않는다.  
이는 분산 시스템의 구조적 한계이며, 현재 프로젝트 수준에서는 감수한다.