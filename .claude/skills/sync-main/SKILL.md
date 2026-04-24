---
name: sync-main
description: dev를 main에 FF 승격하고 SemVer annotated tag + GitHub Release를 1:1로 생성하는 Tom 전용 릴리즈 매니저. 변경 요약과 증분 제안을 보여주고 Tom 컨펌만 받으면 나머지는 전자동. TRIGGER when - "sync-main", "dev → main", "릴리즈 찍기", "메인 반영", "버전 올리기" 같은 발화. DO NOT TRIGGER when - 일반 feat → dev PR 작업, main == dev 확인만, 태그 조회.
---

# sync-main

Tom 혼자 쓰는 릴리즈 도구. 단조로운 흐름 + 깔끔한 컨펌.

## 흐름

### 1. 점검 (조용히)
```bash
git status --porcelain                          # clean인가
git fetch origin main dev --tags
git merge-base --is-ancestor origin/main origin/dev   # FF 가능한가
git describe --tags --abbrev=0 origin/main 2>/dev/null  # 마지막 태그
```

점검 실패면 Tom에게 원인만 한 줄로 알리고 중단.

### 2. 변경 요약 + 제안 + 컨펌

**한 화면으로 정리해서 보여주고 컨펌만 받기.**

포맷:
```
마지막 태그: v0.1.0 (없으면: 첫 릴리즈)
새 커밋 N개 (main..dev):
  - feat: … (2)
  - fix: … (1)
  - chore/docs: … (2)

제안: v0.2.0 (MINOR)  ← 왜: feat 있음

태그 메시지 초안:
  v0.2.0 — {한 줄 요약}

  - {주요 PR/기능 bullet}
  - …

이걸로 갑니다? (y / v0.1.1 같은 직접지정 / 메시지수정 / 취소)
```

**증분 규칙**: `BREAKING`/`!:` → MAJOR, `feat:` → MINOR, `fix:`/`perf:` → PATCH, 나머지만 있으면 PATCH. 여러 개 섞이면 큰 거 이김.

### 3. 컨펌 받으면 전자동 실행

```bash
git checkout main
git pull --ff-only origin main
git merge --ff-only origin/dev
git push origin main
git tag -a "$VERSION" -m "$MSG"
git push origin "$VERSION"

# 태그 = 릴리즈 1:1. 태그 메시지를 릴리즈 본문으로.
TAG_BODY=$(git tag -l --format='%(contents)' "$VERSION")
gh release create "$VERSION" \
  --title "$VERSION" \
  --notes "$TAG_BODY" \
  --verify-tag
```

### 4. 끝나면 한 줄 리포트

```
✅ v0.2.0 완료
   main: <hash> (FF) · tag · release: <url>
```

## 안 하는 것

- 컨펌 전 실행
- FF 불가 시 force push (중단 + 원인만 한 줄)
- 태그 수정/삭제 (한 번 찍으면 끝, 다음 태그로 정정)
- 릴리즈와 태그 분리 (항상 1:1)

## 에러 대응

- **FF 불가**: "main에 dev에 없는 커밋 N개 있음. 원인 보고 결정." — 해당 커밋 해시/메시지만 보여주고 Tom 판단 대기
- **태그 중복**: 다른 버전 재입력 받음
- **gh release create 실패**: 태그는 이미 푸시됐으니 재시도만 하면 됨

## 레포 전제

- 브랜치: `feat/* → dev → main` (2단), main은 FF-only
- main push는 Tom만, bypass 권한으로 가능
- 태그 = annotated, 릴리즈 = GitHub Release, **항상 1:1 매핑**
