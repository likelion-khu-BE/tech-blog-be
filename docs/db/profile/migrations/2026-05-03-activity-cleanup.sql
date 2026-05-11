-- Date: 2026-05-03
-- Context: PR #2 (외부 BC 통합 이벤트 + ActivityType 13종 정리) 머지 시점에 RDS에 적용.
--
-- 목적:
--   1. activity.reference_type 컬럼 제거 (PR #1 잠정 ADD COLUMN 잔존 — 본 코드엔 referenceType 필드 없음)
--   2. activity_type ENUM 정리:
--      - 제거: session_note
--      - 추가: blog_post_like, blog_post_like_received, qna_comment,
--              session_event_post_like, session_event_post_like_received
--      - 유지: blog_post, blog_comment, qna_question, qna_answer, qna_accepted,
--              session_speak, session_event_post, session_event_comment
--
-- 주의: PostgreSQL ENUM은 DROP VALUE 미지원 → 새 ENUM 만들어 swap.
-- 운영 데이터 없는 dev RDS 전제. 데이터 있으면 step 2 전에 clean-up 필요.

BEGIN;

-- 1. 잠정 컬럼 제거
ALTER TABLE activity DROP COLUMN IF EXISTS reference_type;

-- 2. ENUM 교체
-- 2-1. (옵션) 제거 대상 enum 사용 행 정리. dev 단계라 보통 0건.
DELETE FROM activity WHERE type IN ('session_note');

-- 2-2. 새 ENUM 생성 (13종 — 좋아요 누름/받음 양방향 포함)
CREATE TYPE activity_type_new AS ENUM (
  'blog_post',
  'blog_comment',
  'blog_post_like',
  'blog_post_like_received',
  'qna_question',
  'qna_answer',
  'qna_accepted',
  'qna_comment',
  'session_speak',
  'session_event_post',
  'session_event_comment',
  'session_event_post_like',
  'session_event_post_like_received'
);

-- 2-3. 컬럼 타입 교체
ALTER TABLE activity
  ALTER COLUMN type TYPE activity_type_new
  USING type::text::activity_type_new;

-- 2-4. 옛 ENUM 삭제 + rename
DROP TYPE activity_type;
ALTER TYPE activity_type_new RENAME TO activity_type;

COMMIT;
