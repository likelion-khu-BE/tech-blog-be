# save-learning

Claude와 대화하다 배운 내용을 `learned/`에 저장하고 PR로 올리는 스킬.

## 언제 쓰나

팀원이 아래 중 하나를 말할 때:
- "이거 저장해줘", "배운 내용 저장", "learned에 추가해줘"
- "/save-learning"

---

## 흐름

### 1. 내용 파악 + 초안 생성

현재 대화에서 배운 주제를 파악하고 MD 초안을 만든다.

**파일 품질 기준 (타협 없음):**
- 전문 백엔드 엔지니어 수준의 기술적 깊이
- 용어는 정확하게 — 추상적 비유보다 실제 동작 원리
- 이 프로젝트에서 실제 발생한 버그/이슈와 연결 (있으면)
- 면접에서 써먹을 수 있는 수준의 정리
- 파일 구조: 제목 → 핵심 개념 → 동작 원리 → 함정/주의사항 → 실무 적용

**파일명 규칙:** `{주제-kebab-case}.md` (한글 가능)

### 2. 확인

초안을 보여주고 한 번만 묻는다:

```
[저장할 내용]
파일명: learned/{파일명}.md

{MD 내용 미리보기}

저장할까요? (y / 수정사항 말씀해주세요)
```

수정 요청 시 반영 후 재확인. y면 바로 실행.

### 3. 실행 (자동)

```bash
# 현재 브랜치 저장
ORIGIN_BRANCH=$(git branch --show-current)

# learn 브랜치 생성 (origin/dev 기준)
git fetch origin dev
git checkout -b learn/{slug} origin/dev

# 파일 저장
# learned/{파일명}.md 생성
# learned/INDEX.md 업데이트 (기술 심화 섹션에 한 줄 추가)

# 커밋 — 반드시 이 두 파일만
git add learned/{파일명}.md
git add learned/INDEX.md
git commit -m "learn: {제목} 정리 추가"

# PR 오픈
GH_HOST=github.com GH_TOKEN=$GH_TOKEN gh pr create \
  --base dev \
  --title "learn: {제목}" \
  --body "{PR 본문}"

# 원래 브랜치 복귀
git checkout $ORIGIN_BRANCH
```

**PR 본문 형식:**
```markdown
## 왜 만들었나

{이 주제를 정리한 계기 — 버그, 질문, 삽질 등}

## 뭘 배웠나

{핵심 3~5개 bullet}

## 이 프로젝트와의 연관성

{실제 코드/버그/커밋과 연결 — 없으면 생략}

## 어떻게 쓰나

`learned/{파일명}.md` 참고
```

### 4. 완료 리포트

```
✅ 저장 완료
   파일: learned/{파일명}.md
   PR: {PR URL}
   현재 브랜치: {원래 브랜치}로 복귀 완료
```

---

## 제약

- 커밋에 다른 파일 절대 포함하지 않는다 (학습 파일 + INDEX.md만)
- PR base는 항상 dev
- 브랜치명: `learn/` 접두사 필수
- 원래 브랜치 복귀는 PR 오픈 직후 무조건 실행
- INDEX.md 업데이트 빠뜨리지 않는다
