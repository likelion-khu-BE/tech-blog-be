# Blog Module Integration Test Log

**실행일**: 2026-04-11  
**테스트 환경**: H2 in-memory DB (MODE=PostgreSQL, ddl-auto=create-drop)  
**테스트 프레임워크**: Spring Boot Test + MockMvc + JUnit 5  
**총 결과**: ✅ 63개 통과 / ❌ 0개 실패

---

## 테스트 데이터 설계

각 테스트는 `@Transactional` 어노테이션으로 감싸져 있어, 테스트 후 자동 롤백됩니다.  
`@BeforeEach`에서 다음과 같은 현실적인 운영 데이터를 삽입합니다.

| ID   | 제목 (요약)                          | 상태      | 게시판  | 기수  | 작성자     | 태그                        | 좋아요/북마크           |
|------|--------------------------------------|-----------|---------|-------|------------|-----------------------------|-------------------------|
| postA | Spring Boot CI/CD 파이프라인        | PUBLISHED | 백엔드  | 13기  | MOCK_USER  | Spring Boot, GitHub Actions, AWS EC2 | MOCK_USER 좋아요+북마크 |
| postB | ChatGPT API 번역 서비스             | PUBLISHED | AI      | 12기  | OTHER_USER | ChatGPT, Python             | 없음                    |
| postC | 13기 해커톤 48시간 도전기           | PUBLISHED | 해커톤  | 13기  | OTHER_USER | 없음                        | 없음                    |
| postD | Docker Compose 개발환경 (임시저장)  | DRAFT     | 백엔드  | 13기  | MOCK_USER  | 없음                        | 없음                    |
| postE | AWS EC2 CI/CD 심화편 (postA 재게시) | PUBLISHED | 백엔드  | 13기  | MOCK_USER  | Docker                      | 없음                    |

- **MOCK_USER_ID**: `00000000-0000-0000-0000-000000000001`
- **OTHER_USER_ID**: `00000000-0000-0000-0000-000000000002`

---

## 1. Post API (`PostController`) — 31개 테스트

### GET /api/blog/posts (목록 조회)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `getPosts_returnsOnlyPublished` | DRAFT 제외 PUBLISHED 4개만 반환 | ✅ PASS |
| `getPosts_filterByBoard_returnsMatchingPublishedOnly` | board=백엔드 필터 → PUBLISHED 2개 (postD DRAFT 제외) | ✅ PASS |
| `getPosts_filterByGeneration_returnsMatching` | generation=13기 필터 → PUBLISHED 3개 | ✅ PASS |
| `getPosts_filterByKeyword_searchesTitleAndContent` | keyword=CI/CD → 제목 매칭 2개 | ✅ PASS |
| `getPosts_filterByKeyword_contentMatch` | keyword=Blue-Green → 본문 매칭 1개 | ✅ PASS |
| `getPosts_filterByAuthorId_returnsAuthorPublishedPosts` | authorId=MOCK_USER → PUBLISHED 2개 | ✅ PASS |
| `getPosts_pagination_respectsSizeAndPageParams` | size=2, page=0/1 페이지네이션 검증 | ✅ PASS |
| `getPosts_combinedFilters_boardAndGeneration` | board=백엔드 + generation=13기 복합 필터 | ✅ PASS |

### GET /api/blog/posts/{id} (단건 조회)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `getPost_publishedPost_returnsFullDetails` | PUBLISHED 포스트 전체 필드 검증 (liked, bookmarked, likeCount, bookmarkCount, tags) | ✅ PASS |
| `getPost_repostedPost_includesRepostFromId` | 재게시 포스트의 repostFromId 포함 여부 | ✅ PASS |
| `getPost_noLikeOrBookmark_returnsFalseFlags` | 좋아요/북마크 없는 포스트 → liked=false, bookmarked=false | ✅ PASS |
| `getPost_ownDraft_returnsPost` | 본인 DRAFT 조회 → 200 OK | ✅ PASS |
| `getPost_othersDraft_returns403` | 타인 DRAFT 조회 → 403 Forbidden | ✅ PASS |
| `getPost_notFound_returns404` | 존재하지 않는 ID 조회 → 404 Not Found | ✅ PASS |

### POST /api/blog/posts (포스트 생성)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `createPost_publishedWithTags_returns201` | 태그 포함 PUBLISHED 포스트 생성 → 201, 필드 검증 | ✅ PASS |
| `createPost_asDraft_returns201WithDraftStatus` | DRAFT 상태로 생성 → 201, status=DRAFT | ✅ PASS |
| `createPost_withRepostFromId_returns201` | repostFromId 포함 재게시 생성 | ✅ PASS |
| `createPost_missingRequiredField_returns400` | 필수 필드(title) 누락 → 400 Bad Request | ✅ PASS |

### PUT /api/blog/posts/{id} (포스트 수정)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `updatePost_ownPost_updatesFieldsAndTags` | 본인 포스트 수정, 태그 교체 | ✅ PASS |
| `updatePost_draftToPublished_changesStatus` | DRAFT → PUBLISHED 상태 변경 | ✅ PASS |
| `updatePost_othersPost_returns403` | 타인 포스트 수정 → 403 Forbidden | ✅ PASS |
| `updatePost_notFound_returns404` | 존재하지 않는 포스트 수정 → 404 Not Found | ✅ PASS |

### DELETE /api/blog/posts/{id} (포스트 삭제)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `deletePost_ownDraftWithNoDependents_returns204` | 본인 DRAFT (의존 데이터 없음) 삭제 → 204 No Content | ✅ PASS |
| `deletePost_othersPost_returns403` | 타인 포스트 삭제 → 403 Forbidden | ✅ PASS |
| `deletePost_notFound_returns404` | 존재하지 않는 포스트 삭제 → 404 Not Found | ✅ PASS |

> **알려진 한계**: 좋아요/북마크/댓글이 있는 포스트를 직접 삭제하면 FK 제약 위반 발생.  
> `PostService.deletePost()`가 관련 좋아요/북마크/댓글을 먼저 삭제하지 않는 설계상 제한. 향후 수정 필요.

### POST /api/blog/posts/{id}/like (좋아요 토글)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `toggleLike_noExistingLike_returnsLikedTrue` | 좋아요 없는 포스트 → liked=true | ✅ PASS |
| `toggleLike_existingLike_returnsLikedFalse` | 이미 좋아요한 포스트 → liked=false (취소) | ✅ PASS |
| `toggleLike_twice_backToLiked` | 좋아요 → 취소 → 좋아요 3회 토글 검증 | ✅ PASS |
| `likeCount_reflectsMultipleUsers` | 여러 사용자 좋아요 → likeCount 정확성 | ✅ PASS |

### POST /api/blog/posts/{id}/bookmark (북마크 토글)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `toggleBookmark_noExistingBookmark_returnsBookmarkedTrue` | 북마크 없는 포스트 → bookmarked=true | ✅ PASS |
| `toggleBookmark_existingBookmark_returnsBookmarkedFalse` | 이미 북마크한 포스트 → bookmarked=false (취소) | ✅ PASS |

---

## 2. Comment API (`CommentController`) — 18개 테스트

### 테스트 데이터 (댓글 트리)

```
root1 (MOCK_USER) ← MOCK_USER가 좋아요
  └── reply1 (OTHER_USER)
  └── reply2 (MOCK_USER)
root2 (OTHER_USER)
```

### GET /api/blog/posts/{postId}/comments (댓글 목록)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `getComments_returnsTreeStructure` | 루트 2개, root1에 대댓글 2개 트리 구조 | ✅ PASS |
| `getComments_rootCommentFields_includeCorrectData` | id, content, userId, likeCount, liked, parentId 검증 | ✅ PASS |
| `getComments_replyFields_includeParentId` | 대댓글의 parentId 포함 여부 | ✅ PASS |
| `getComments_emptyPost_returnsEmptyList` | 댓글 없는 포스트 → 빈 배열 | ✅ PASS |

### POST /api/blog/posts/{postId}/comments (댓글 생성)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `createComment_rootComment_returns201` | 루트 댓글 생성 → 201, parentId 없음 | ✅ PASS |
| `createComment_reply_returns201WithParentId` | 대댓글 생성 → 201, parentId 포함 | ✅ PASS |
| `createComment_invalidParentId_returns404` | 존재하지 않는 parentId → 404 Not Found | ✅ PASS |
| `createComment_blankContent_returns400` | 빈 content → 400 Bad Request | ✅ PASS |

### PUT /api/blog/comments/{id} (댓글 수정)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `updateComment_ownComment_returnsUpdated` | 본인 댓글 수정 → 수정된 content 반환 | ✅ PASS |
| `updateComment_othersComment_returns403` | 타인 댓글 수정 → 403 Forbidden | ✅ PASS |
| `updateComment_notFound_returns404` | 존재하지 않는 댓글 수정 → 404 Not Found | ✅ PASS |

### DELETE /api/blog/comments/{id} (댓글 삭제)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `deleteComment_ownLeafComment_returns204` | 본인 leaf 댓글 (대댓글/좋아요 없음) 삭제 → 204 No Content | ✅ PASS |
| `deleteComment_othersComment_returns403` | 타인 댓글 삭제 → 403 Forbidden | ✅ PASS |
| `deleteComment_notFound_returns404` | 존재하지 않는 댓글 삭제 → 404 Not Found | ✅ PASS |

> **알려진 한계**: 대댓글이 있거나 좋아요가 있는 댓글을 삭제하면 FK 제약 위반 발생.  
> `CommentService.deleteComment()`가 관련 데이터를 먼저 삭제하지 않는 설계상 제한. 향후 수정 필요.

### POST /api/blog/comments/{id}/like (댓글 좋아요 토글)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `toggleCommentLike_noExistingLike_returnsLikedTrue` | 좋아요 없는 댓글 → liked=true | ✅ PASS |
| `toggleCommentLike_existingLike_returnsLikedFalse` | 이미 좋아요한 댓글 → liked=false (취소) | ✅ PASS |
| `toggleCommentLike_likeAndUnlike_likeCountChanges` | 좋아요 → 취소 2회 토글 | ✅ PASS |
| `toggleCommentLike_notFound_returns404` | 존재하지 않는 댓글 좋아요 → 404 Not Found | ✅ PASS |

---

## 3. Admin API (`AdminController`) — 14개 테스트

### 테스트 데이터

- 3개 PUBLISHED 포스트 (p1, p2, p3) + 1개 DRAFT 포스트 (p4)
- p1에 댓글 3개 (루트 2개 + 대댓글 1개)
- p1에 좋아요 1개, 태그 2개

### GET /api/blog/admin/stats (통계 조회)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `getStats_returnsCorrectCounts` | totalPosts=4, publishedPosts=3, draftPosts=1, totalComments=3 | ✅ PASS |
| `getStats_afterAddingDraft_incrementsDraftCount` | DRAFT 추가 후 draftPosts=2 증가 확인 | ✅ PASS |

### GET /api/blog/admin/posts (전체 포스트 관리 목록)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `getAllPosts_includesDrafts` | 어드민은 DRAFT 포함 전체 4개 조회 | ✅ PASS |
| `getAllPosts_pagination_defaultPage20` | 기본 size=20, 첫 페이지에 전체 포함 | ✅ PASS |
| `getAllPosts_customPageSize_paginatesCorrectly` | size=2 페이지네이션 → totalPages=2 | ✅ PASS |
| `getAllPosts_postFields_includeTagsAndLikeCount` | DRAFT가 createdAt 기준 최신 순 첫 번째 | ✅ PASS |

### PATCH /api/blog/admin/posts/{id}/status (상태 변경)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `changePostStatus_publishedToDraft_succeeds` | PUBLISHED → DRAFT 변경 후 DB 확인 | ✅ PASS |
| `changePostStatus_draftToPublished_succeeds` | DRAFT → PUBLISHED 변경 후 DB 확인 | ✅ PASS |
| `changePostStatus_notFound_returns404` | 존재하지 않는 포스트 → 404 Not Found | ✅ PASS |
| `changePostStatus_nullStatus_returns400` | status=null → 400 Bad Request | ✅ PASS |
| `changePostStatus_invalidStatusValue_returns400` | status="INVALID_STATUS" → 400 Bad Request | ✅ PASS |

### DELETE /api/blog/admin/posts/{id} (강제 삭제)

| 테스트명 | 설명 | 결과 |
|----------|------|------|
| `forceDeletePost_noDependents_returns204AndRemovesPost` | 의존 데이터 없는 포스트 삭제 → 204, DB에서 제거 확인 | ✅ PASS |
| `forceDeletePost_withTags_deleteTagsAndPost` | 태그 있는 포스트 삭제 → 포스트+태그 모두 제거 | ✅ PASS |
| `forceDeletePost_notFound_returns404` | 존재하지 않는 포스트 → 404 Not Found | ✅ PASS |

---

## 전체 요약

| 카테고리 | 테스트 수 | 통과 | 실패 |
|----------|-----------|------|------|
| Post API | 31 | 31 | 0 |
| Comment API | 18 | 18 | 0 |
| Admin API | 14 | 14 | 0 |
| **합계** | **63** | **63** | **0** |

---

## 알려진 설계 한계 (프로덕션 수정 권장)

1. **포스트 삭제 시 관련 데이터 미처리**  
   `PostService.deletePost()` 및 `AdminService.forceDeletePost()`가 `post_likes`, `post_bookmarks`, `comments`, `comment_likes`를 먼저 삭제하지 않음.  
   의존 데이터가 있는 포스트를 삭제하려면 FK 제약 위반 발생.  
   **권장 수정**: 각 repository에 `deleteAllByPostId()` 메서드 추가 후 삭제 순서 보장.

2. **댓글 삭제 시 관련 데이터 미처리**  
   `CommentService.deleteComment()`가 대댓글과 `comment_likes`를 먼저 삭제하지 않음.  
   대댓글이나 좋아요가 있는 댓글 삭제 시 FK 제약 위반 발생.

3. **`build.gradle` `-parameters` 플래그 없이 컴파일 시 동작 불가**  
   Spring Boot Plugin이 미적용된 상태에서 `@PathVariable`, `@RequestParam` 등 파라미터 이름 추론 실패.  
   `blog/build.gradle`에 `tasks.withType(JavaCompile).configureEach { options.compilerArgs += ['-parameters'] }` 추가로 해결.
