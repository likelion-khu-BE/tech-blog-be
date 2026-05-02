# S3 이미지 업로드 가이드

## 개요

이미지 업로드는 **Presigned PUT URL** 방식을 사용한다. 서버가 파일을 중계하지 않고, 클라이언트가 S3에 직접 업로드한다.

```
클라이언트                백엔드                     S3
    │                      │                          │
    │  ① presigned URL 요청 │                          │
    │──────────────────────▶│                          │
    │                      │  (S3Presigner로 서명 생성) │
    │  ② presignedUrl + key │                          │
    │◀──────────────────────│                          │
    │                                                  │
    │  ③ PUT presignedUrl (이미지 파일 직접 업로드)       │
    │──────────────────────────────────────────────────▶│
    │                                                  │
    │  ④ 게시글 저장 요청 (key 포함)                      │
    │──────────────────────▶│                          │
    │                      │  (key를 DB에 저장)         │
```

**왜 Presigned URL인가?**
- 서버 메모리/대역폭을 소비하지 않음
- 클라이언트 → S3 직접 전송이라 속도가 빠름
- 서버는 key만 받아서 DB에 저장하면 됨

---

## 버킷 & 엔드포인트 구성

버킷은 도메인별로 분리되어 있으며, **모두 Public 버킷**이다 (조회 시 presigned GET URL 불필요).  
presigned URL 발급 엔드포인트도 도메인별로 분리되어 있어, 클라이언트가 버킷 이름을 알 필요가 없다.

| 엔드포인트 | 버킷 환경변수 | application.yml key |
|---|---|---|
| `GET /api/events/presigned-urls` | `S3_BUCKET_EVENT` | `s3.bucket.event` |
| `GET /api/sessions/presigned-urls` | `S3_BUCKET_SESSION` | `s3.bucket.session` |
| `GET /api/{domain}/presigned-urls` | `S3_BUCKET_{DOMAIN}` | `s3.bucket.{domain}` |

새 도메인에 버킷이 필요하면 위 패턴으로 환경변수와 `application.yml` 항목을 추가하고, 해당 도메인에 컨트롤러를 추가한다.

---

## 백엔드 사용법

### 1. 버킷 이름 주입

각 도메인 서비스(또는 컨트롤러)는 자신이 사용할 버킷 이름만 `@Value`로 주입한다. **S3Service에 버킷 이름을 하드코딩하지 않는다.**

```java
@Service
@RequiredArgsConstructor
public class YourDomainService {

    private final S3Service s3Service;

    @Value("${s3.bucket.your-domain}")
    private String bucket;
}
```

### 2. presigned PUT URL 발급

게시글 저장 직전에 발급한다. 업로드 후 `key`를 DB에 저장한다.

```java
// filenames: 클라이언트가 올릴 파일명 목록 (예: ["photo.jpg", "diagram.png"])
List<PresignedUrlResponse> urls = s3Service.generatePresignedPutUrls(bucket, filenames);

// PresignedUrlResponse(String presignedUrl, String key)
// presignedUrl → 클라이언트가 PUT 요청에 쓸 서명된 URL (만료 있음, 캐시 금지)
// key          → DB에 저장할 경로 (예: "images/550e8400-e29b-41d4-a716-446655440000.jpg")
```

### 3. 조회 시 URL 변환

DB에 저장된 `key`를 응답 DTO로 조립할 때 전체 URL로 변환한다.

```java
// key → "https://{bucket}.s3.{region}.amazonaws.com/{key}"
String url = s3Service.getFileUrl(bucket, key);
```

### 4. 삭제

게시글 삭제 시 연결된 이미지도 함께 삭제한다.

```java
// 연결된 이미지 목록을 순회하며 S3 파일 삭제
images.forEach(image -> s3Service.delete(bucket, image.getKey()));
```

---

## S3Service 메서드 레퍼런스

> 위치: `src/main/java/com/study/s3/application/S3Service.java`

| 메서드 | 설명 |
|---|---|
| `generatePresignedPutUrls(bucket, filenames)` | 파일명 목록으로 presigned PUT URL + key 발급 |
| `getFileUrl(bucket, key)` | key → Public URL 변환 (조회 응답 DTO 조립용) |
| `delete(bucket, key)` | S3 파일 삭제 |

**key 생성 규칙**: `images/{UUID}.{확장자}` — 확장자가 없으면 UUID만 사용

---

## 프론트엔드 연동

### Step 1 — presigned URL 요청

게시글 등록/임시저장 버튼 클릭 시 업로드할 파일 목록으로 presigned URL을 요청한다.  
엔드포인트는 도메인마다 다르며, 버킷은 서버에서 자동으로 결정된다.

```
GET /api/events/presigned-urls?filenames={파일명1}&filenames={파일명2}
GET /api/sessions/presigned-urls?filenames={파일명1}&filenames={파일명2}
Authorization: Bearer {accessToken}
```

**Response `200 OK`**
```json
[
  {
    "presignedUrl": "https://{bucket}.s3.ap-northeast-2.amazonaws.com/images/uuid.jpg?X-Amz-Signature=...",
    "key": "images/550e8400-e29b-41d4-a716-446655440000.jpg"
  },
  {
    "presignedUrl": "https://...",
    "key": "images/6ba7b810-9dad-11d1-80b4-00c04fd430c8.png"
  }
]
```

### Step 2 — S3 직접 업로드

응답받은 `presignedUrl`로 파일을 **PUT**으로 직접 업로드한다.

```ts
// presignedUrl은 서명된 URL이므로 Authorization 헤더 없이 요청
await fetch(presignedUrl, {
  method: 'PUT',
  body: file,
  headers: { 'Content-Type': file.type },
})
```

> `Authorization` 헤더를 포함하면 S3 서명 불일치로 403이 발생한다.

### Step 3 — 리소스 저장 요청

업로드 완료 후 `key` 목록을 저장 요청에 포함한다.

```ts
const keys = presignedUrlResponses.map((r) => r.key)

// 각 도메인 API 명세 참고
await api.post('/your-domain/resources', {
  // ...기타 필드,
  imageKeys: keys,
})
```

### 예시 코드 (React + TypeScript)

**타입 정의**

```ts
// src/types/s3.ts
export interface PresignedUrlResponse {
  presignedUrl: string
  key: string
}
```

**API 유틸**

```ts
// src/lib/s3.ts
import axios from 'axios'
import type { PresignedUrlResponse } from '@/types/s3'

const api = axios.create({ baseURL: import.meta.env.VITE_API_URL })

export async function uploadImages(
  files: File[],
  presignedUrlEndpoint: string,
): Promise<string[]> {
  const filenames = files.map((f) => f.name)

  // ① presigned URL 요청 (bucket은 서버가 결정)
  const { data } = await api.get<PresignedUrlResponse[]>(presignedUrlEndpoint, {
    params: { filenames },
  })

  // ② S3 직접 업로드 (병렬, Authorization 헤더 제외)
  await Promise.all(
    data.map(({ presignedUrl }, i) =>
      fetch(presignedUrl, {
        method: 'PUT',
        body: files[i],
        headers: { 'Content-Type': files[i].type },
      }),
    ),
  )

  // ③ key 목록 반환 → 저장 요청에 포함
  return data.map((r) => r.key)
}
```

**컴포넌트에서 사용**

```tsx
// src/components/EventPostForm.tsx
import { useState } from 'react'
import { uploadImages } from '@/lib/s3'

export function EventPostForm() {
  const [files, setFiles] = useState<File[]>([])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    const imageKeys = files.length > 0
      ? await uploadImages(files, '/api/events/presigned-urls')
      : []

    await api.post('/session-board/{generationId}/event-posts', {
      title: '...',
      body: '...',
      imageKeys,
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="file"
        multiple
        accept="image/*"
        onChange={(e) => setFiles(Array.from(e.target.files ?? []))}
      />
      <button type="submit">등록</button>
    </form>
  )
}
```

**`vite.config.ts` 환경변수 설정**

```sh
# .env.local
VITE_API_URL=http://localhost:8080
```

---

## 주의사항

**presigned URL은 캐시하지 않는다**
- 기본 만료 시간은 **10분** (`S3_PRESIGNED_URL_EXPIRATION=600`)
- 조회 응답에 포함된 `images[].url`도 만료가 있으므로 클라이언트에서 장기 캐시 금지

**업로드 타이밍**
- 게시글 저장 **직전**에 presigned URL을 발급하고 즉시 업로드한다
- 미리 발급해두면 만료 전에 업로드 못 하는 상황이 생길 수 있음

**고아 파일**
- 업로드 후 게시글 저장이 실패하면 S3에 파일이 남는다
- 현재는 별도 정리 정책 없음 (추후 S3 Lifecycle 정책 적용 예정)

**DELETE 시 S3도 함께 삭제**
- 게시글/자료 삭제 시 연결된 S3 파일도 반드시 함께 삭제한다
- DB에서만 지우고 S3를 안 지우면 스토리지 비용 누수가 발생함

---

## .env 설정

```
AWS_ACCESS_KEY=...
AWS_SECRET_KEY=...
AWS_REGION=ap-northeast-2
S3_BUCKET_EVENT=...            # /api/events/presigned-urls
S3_BUCKET_SESSION=...          # /api/sessions/presigned-urls
# S3_BUCKET_{DOMAIN}=...       # 신규 도메인 추가 시
S3_PRESIGNED_URL_EXPIRATION=600
```
