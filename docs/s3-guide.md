# S3 이미지 업로드 가이드

## 개요

이미지 업로드는 **Presigned PUT URL** 방식을 사용한다. 서버가 파일을 중계하지 않고, 클라이언트가 S3에 직접 업로드한다.

**왜 Presigned URL인가?**
- 서버 메모리·대역폭을 소비하지 않음
- 클라이언트 → S3 직접 전송이라 속도가 빠름
- 서버는 `key`만 받아서 DB에 저장하면 됨

---

## 생성된 파일 목록 및 역할

| 파일 | 위치 | 역할 |
|---|---|---|
| `S3Config` | `shared/config/S3Config.java` | `S3Client`, `S3Presigner` 빈 등록. `S3Properties` 활성화 |
| `S3Properties` | `shared/s3/S3Properties.java` | `application.yml`의 S3 설정값을 타입 안전하게 바인딩 |
| `S3Service` | `shared/s3/S3Service.java` | presigned URL 생성, 공개 URL 조회, HeadObject 검증, 파일 삭제 |
| `S3UploadValidator` | `shared/s3/S3UploadValidator.java` | 파일명(확장자·수) 검증 + 업로드 후 HeadObject(Content-Type·크기) 검증 |
| `S3ErrorCode` | `shared/s3/S3ErrorCode.java` | S3 관련 공통 에러 코드 enum |
| `S3Exception` | `shared/s3/S3Exception.java` | S3ErrorCode를 wrapping하는 공통 예외 |
| `PresignedUrlResponse` | `shared/s3/PresignedUrlResponse.java` | presigned URL 발급 응답 DTO (`presignedUrl`, `key`) |
| `RateLimitService` | `shared/ratelimit/RateLimitService.java` | 유저별 API 요청 빈도 제한 (Bucket4j 인메모리) |
| `AsyncConfig` | `shared/config/AsyncConfig.java` | `@EnableAsync` + `@EnableScheduling` 활성화 |

### S3Config

`S3Client`(실제 S3 작업)와 `S3Presigner`(서명 URL 생성) 두 빈을 등록한다. `@EnableConfigurationProperties(S3Properties.class)`로 `S3Properties` 레코드를 활성화한다.

### S3Properties

```java
@ConfigurationProperties(prefix = "spring.cloud.aws.s3")
public record S3Properties(BucketProperties bucket, long presignedUrlExpiration) {
  public record BucketProperties(String event, String session) {}
}
```

`application.yml`의 `spring.cloud.aws.s3.bucket.event`, `spring.cloud.aws.s3.bucket.session` 값을 바인딩한다.  
**새 버킷을 추가할 때는 `BucketProperties` 레코드에 필드를 추가한다.**

### S3Service

| 메서드 | 설명 |
|---|---|
| `generatePresignedPutUrls(bucket, filenames)` | 파일명 목록 → `List<PresignedUrlResponse>` 일괄 발급 |
| `generatePresignedPutUrl(bucket, key, contentType, expirySeconds)` | 단건 발급. `contentType`을 고정하면 AWS가 헤더를 검증 |
| `getFileUrl(bucket, key)` | `key` → `https://{bucket}.s3.{region}.amazonaws.com/{key}` |
| `headObject(bucket, key)` | `HeadObjectResponse` 반환. 키 없으면 `NoSuchKeyException` |
| `delete(bucket, key)` | S3 파일 삭제. 키가 없어도 예외 없음 |
| `generateKey(filename)` | `images/{UUID}.{ext}` 형식 key 생성 |

### RateLimitService

```java
boolean allowed = rateLimitService.tryConsume(userId);
// true  → 요청 허용
// false → 429 Too Many Requests 반환
```

기본값: **1분에 5회**. presigned URL 발급 엔드포인트에 반드시 적용한다.  
서버 재시작 시 버킷이 초기화되며, Redis 전환 가이드는 클래스 Javadoc 참고.

---

## 버킷 구성

버킷은 도메인별로 분리되어 있으며 **Public 읽기 버킷**이다 (조회 시 presigned GET URL 불필요).

| 엔드포인트 예시 | 환경변수 | `S3Properties` 접근 |
|---|---|---|
| `GET /api/events/presigned-urls` | `S3_BUCKET_EVENT` | `s3Properties.bucket().event()` |
| `GET /api/sessions/presigned-urls` | `S3_BUCKET_SESSION` | `s3Properties.bucket().session()` |
| `GET /api/{domain}/presigned-urls` | `S3_BUCKET_{DOMAIN}` | 신규 필드 추가 필요 |

---

## 백엔드 구현 가이드

### 1. 의존성 주입

```java
@Service
@RequiredArgsConstructor
public class YourDomainService {

    private final S3Service s3Service;
    private final S3Properties s3Properties;
    private final S3UploadValidator s3UploadValidator;
    private final RateLimitService rateLimitService;

    private String bucket() {
        return s3Properties.bucket().yourDomain(); // 자신의 도메인 버킷만 사용
    }
}
```

### 2. presigned URL 발급 컨트롤러

**presigned URL 엔드포인트에는 반드시 Rate Limit + 파일명 검증을 함께 적용한다.**

```java
@RestController
@RequestMapping("/api/your-domain")
@RequiredArgsConstructor
public class YourPresignedUrlController {

    private final S3Service s3Service;
    private final S3Properties s3Properties;
    private final S3UploadValidator s3UploadValidator;
    private final RateLimitService rateLimitService;

    @GetMapping("/presigned-urls")
    public List<PresignedUrlResponse> getPresignedUrls(
            @RequestParam List<String> filenames,
            @AuthenticationPrincipal Long userId) {

        // ① Rate Limit: 유저당 분당 5회
        if (!rateLimitService.tryConsume(userId)) {
            throw new S3Exception(S3ErrorCode.UPLOAD_RATE_LIMITED);
        }

        // ② 파일명 검증: 허용 확장자·최대 파일 수 (application.yml upload 설정 기준)
        s3UploadValidator.validateFilenames(filenames);

        String bucket = s3Properties.bucket().yourDomain();
        return s3Service.generatePresignedPutUrls(bucket, filenames);
    }
}
```

### 3. 게시글/자료 저장 — HeadObject 검증 후 key를 DB에 저장

클라이언트로부터 `imageKeys` 목록을 받을 때 **반드시 HeadObject 검증 후** DB에 저장한다.  
검증 실패 시 해당 S3 파일은 `S3UploadValidator` 내부에서 자동 삭제된다.

```java
@Transactional
public void createPost(Long userId, CreatePostRequest request) {
    String bucket = bucket();

    List<YourImage> images = new ArrayList<>();
    for (int i = 0; i < request.imageKeys().size(); i++) {
        String key = request.imageKeys().get(i);

        // Content-Type(image/*), 파일 크기(≤ maxFileSizeBytes) 이중 검증
        // 검증 실패 시 S3Exception 발생 + S3 파일 즉시 삭제
        s3UploadValidator.validateUpload(bucket, key);

        String url = s3Service.getFileUrl(bucket, key);
        images.add(YourImage.of(entity, url, key, i));
    }
    imageRepository.saveAll(images);
}
```

### 4. 조회 응답 — key → URL 변환

DB에 `key`를 저장하고, 응답 DTO 조립 시 URL로 변환한다. 버킷이 Public이므로 presigned GET URL은 불필요하다.

```java
String url = s3Service.getFileUrl(bucket, image.getImageKey());
// → "https://{bucket}.s3.{region}.amazonaws.com/{key}"
```

### 5. 삭제 — S3와 DB 함께 처리

게시글·자료 삭제 시 연결된 S3 파일도 반드시 함께 삭제한다.

```java
@Transactional
public void deletePost(Long postId) {
    YourPost post = postRepository.findById(postId).orElseThrow(...);
    List<YourImage> images = imageRepository.findByPost(post);

    // S3 파일 먼저 삭제 → DB 삭제 순서
    images.forEach(img -> s3Service.delete(bucket(), img.getImageKey()));
    imageRepository.deleteAll(images);
    postRepository.delete(post);
}
```

---

## 팀별 적용 방법

### sessionboard 팀

- 버킷: `s3Properties.bucket().session()`
- presigned URL 엔드포인트: `GET /api/sessions/presigned-urls`
- 참고 파일: `EventPresignedUrlController`, `EventPostService`

```java
// EventPresignedUrlController — 이미 구현된 패턴 참고
String bucket = s3Properties.bucket().session(); // session 버킷 사용
```

### blog 팀

- 버킷: `S3Properties.BucketProperties`에 `blog` 필드 추가 필요
- `application.yml`에 `spring.cloud.aws.s3.bucket.blog: ${S3_BUCKET_BLOG}` 추가
- `.env`에 `S3_BUCKET_BLOG=...` 추가
- presigned URL 엔드포인트: `GET /api/posts/presigned-urls` (신규 구현)

```java
// S3Properties.BucketProperties에 추가
public record BucketProperties(String event, String session, String blog) {}

// blog 서비스에서 사용
String bucket = s3Properties.bucket().blog();
```

### qna 팀

- blog 팀과 동일한 패턴으로 `qna` 필드 추가
- presigned URL 엔드포인트: `GET /api/questions/presigned-urls` (신규 구현)

### profile 팀

- 프로필 이미지는 단건 업로드이므로 `generatePresignedPutUrls` 대신 `generatePresignedPutUrl` 사용 가능
- Content-Type을 `image/jpeg` 또는 `image/png`로 고정하면 AWS 레벨에서 타입 강제 가능

```java
String key = s3Service.generateKey("profile.jpg");
String presignedUrl = s3Service.generatePresignedPutUrl(
        bucket, key, "image/jpeg", s3Properties.presignedUrlExpiration());
```

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

```ts
// presignedUrl에는 Authorization 헤더를 포함하지 않는다 (403 발생)
await fetch(presignedUrl, {
  method: 'PUT',
  body: file,
  headers: { 'Content-Type': file.type },
})
```

### Step 3 — key 포함해서 저장 요청

```ts
const keys = presignedUrlResponses.map((r) => r.key)
await api.post('/your-domain/resources', { ...otherFields, imageKeys: keys })
```

### 예시 유틸 (TypeScript)

```ts
// src/lib/s3.ts
export interface PresignedUrlResponse {
  presignedUrl: string
  key: string
}

export async function uploadImages(
  files: File[],
  endpoint: string,   // 예: '/api/events/presigned-urls'
): Promise<string[]> {
  const filenames = files.map((f) => f.name)

  const { data } = await api.get<PresignedUrlResponse[]>(endpoint, {
    params: { filenames },
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

S3_BUCKET_EVENT=...           # sessionboard 이벤트 게시글 이미지
S3_BUCKET_SESSION=...         # sessionboard 세션보드 이미지
# S3_BUCKET_BLOG=...          # blog 팀 추가 시
# S3_BUCKET_QNA=...           # qna 팀 추가 시

S3_PRESIGNED_URL_EXPIRATION=180   # 초 단위 (기본 3분, 미설정 시 application.yml 기본값 사용)
```

---

## 주의사항

**presigned URL 캐시 금지**
- 만료 시간은 `S3_PRESIGNED_URL_EXPIRATION` 값 (기본 180초 = 3분)
- 클라이언트에서 presigned URL을 캐시하면 만료 후 업로드 실패

**Rate Limit**
- presigned URL 발급 엔드포인트는 반드시 `RateLimitService.tryConsume(userId)`를 호출한다
- 기본 제한: 1분에 5회. 초과 시 429 반환

**업로드 타이밍**
- 게시글 저장 **직전**에 presigned URL을 발급하고 즉시 업로드한다
- 미리 발급해두면 만료 전에 못 올리는 상황이 생길 수 있음

**고아 파일 (Orphan Objects)**
- 업로드 후 게시글 저장이 실패하면 S3에 파일이 남는다
- 현재 별도 정리 정책 없음. 추후 S3 Lifecycle 정책(예: `images/` 경로 7일 후 자동 삭제) 적용 예정

**삭제 시 S3도 함께**
- 게시글·자료 삭제 시 연결된 S3 파일도 반드시 함께 삭제한다
- DB만 지우고 S3를 안 지우면 스토리지 비용 누수

**새 도메인 버킷 추가 절차**
1. `S3Properties.BucketProperties`에 필드 추가
2. `application.yml`에 `spring.cloud.aws.s3.bucket.{domain}: ${S3_BUCKET_{DOMAIN}}` 추가
3. `.env`에 `S3_BUCKET_{DOMAIN}=...` 추가
4. 도메인 컨트롤러에서 `s3Properties.bucket().{domain}()` 사용