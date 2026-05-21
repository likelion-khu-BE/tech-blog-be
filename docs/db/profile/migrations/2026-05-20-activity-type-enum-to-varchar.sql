-- ============================================================
-- Migration: activity.type 컬럼 native enum → VARCHAR(50)
-- ============================================================
-- 배경:
--   - profile-ddl.sql은 VARCHAR(50)로 정의돼있는데 RDS는 native enum(activity_type) 그대로
--   - 코드 매핑 @Enumerated(EnumType.STRING) → VARCHAR 전송 → PG가 enum vs varchar 비교 못 함
--   - 2026-05-13-enum-to-varchar.sql 중 activity 관련 라인만 떼서 적용 (다른 팀 enum 컬럼은 그대로 유지)
--
-- partial index가 enum 연산자를 참조하므로 ALTER 전 drop, 후 재생성.
-- ============================================================

DROP INDEX IF EXISTS uq_activity_received;
DROP INDEX IF EXISTS uq_activity_normal;

ALTER TABLE activity ALTER COLUMN type TYPE VARCHAR(50) USING type::text;

CREATE UNIQUE INDEX uq_activity_received
    ON activity (member_id, type, reference_id, actor_id)
    WHERE type IN ('blog_post_like_received', 'session_event_post_like_received');

CREATE UNIQUE INDEX uq_activity_normal
    ON activity (member_id, type, reference_id)
    WHERE type NOT IN ('blog_post_like_received', 'session_event_post_like_received');

DROP TYPE IF EXISTS activity_type;
