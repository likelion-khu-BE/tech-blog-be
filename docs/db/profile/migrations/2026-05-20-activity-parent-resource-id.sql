-- ============================================================
-- Migration: activity 테이블에 parent_resource_id 컬럼 추가
-- ============================================================
-- 목적: 활동 read API(§6-2/§6-3) 응답의 link 필드 생성을 위해 라우팅용 부모 리소스 id 저장.
--   - 댓글 → 부모 글 id
--   - 답변/vote → 부모 질문 id
--   - 루트(글/질문 등) → NULL
--
-- cascade 동작 영향 0 — reference_id 의미는 그대로(child id 식별용) 유지.
-- 기존 row의 parent_resource_id는 NULL (마이그레이션 전 데이터는 link 못 만듦 — 신규 활동부터 적용).
-- ============================================================

ALTER TABLE activity
    ADD COLUMN parent_resource_id BIGINT NULL;
