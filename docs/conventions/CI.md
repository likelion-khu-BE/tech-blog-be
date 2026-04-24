# CI 컨벤션

## 개요

모든 PR에 대해 자동으로 CI가 실행된다. CI를 통과하지 못하면 머지할 수 없다.

## 트리거

`pull_request` 이벤트. push-to-main에는 실행되지 않는다.
대상 베이스 브랜치: main, dev, profile, blog, qna, session-board

## CI 파이프라인 단계

### 1. Spotless 자동 수정

CI가 `spotlessApply`를 실행해 포맷을 자동으로 수정하고, 변경사항이 있으면 커밋을 푸시한다.

```
style: Spotless 자동 수정
```

**팀원이 직접 할 필요 없다.** 단, Spotless 봇 커밋 이후 로컬을 그대로 push하면 conflict가 난다.
작업 브랜치에서 `git pull --rebase origin {브랜치명}`으로 봇 커밋을 먼저 당겨온 뒤 push해야 한다.

### 2. 빌드 + 테스트

```
./gradlew compileJava test
```

컴파일 에러 또는 테스트 실패 시 CI가 실패한다.

### 3. PR 코멘트

CI 결과가 PR 코멘트로 자동으로 남는다.

- 성공: `CI 통과 — 빌드 및 테스트 성공`
- 실패: 전체 로그 링크 + 실패 원인 일부 발췌

## 머지 정책

CI가 통과되지 않으면 PR을 머지할 수 없다 (branch protection).
머지 방식은 **Squash and merge** 사용 — feature 브랜치의 커밋을 하나로 압축해 팀 베이스에 반영한다.

## AI 코드 리뷰 (CodeRabbit)

PR이 열리면 CodeRabbit이 자동으로 코드 리뷰를 남긴다.

- 언어: 한국어
- 프로필: chill (blocking 리뷰 없음, 참고용)
- 제외: `*.md`, `**/migration/**`

CodeRabbit 리뷰는 머지 필수 조건이 아니다. 참고해서 반영 여부는 팀원이 판단한다.

## 로컬에서 미리 확인하는 법

PR 올리기 전에 아래를 로컬에서 돌려보면 CI 실패를 방지할 수 있다.

```bash
# 포맷 확인 (수정까지)
./gradlew spotlessApply

# 빌드 + 테스트
./gradlew compileJava test
```
