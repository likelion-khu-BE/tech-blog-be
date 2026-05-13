-- native enum → VARCHAR(50) 마이그레이션
-- 배경: Hibernate create-drop 테스트 환경에서 NAMED_ENUM 타입을 못 찾아 CI 실패
-- contribution_period_type: 어떤 테이블 컬럼에도 사용되지 않음 (API 파라미터용 Java enum만 존재)
-- activity.type: partial index(uq_activity_received, uq_activity_normal)가 enum 연산자 참조 → index 재생성 필요

ALTER TABLE member ALTER COLUMN session_type TYPE VARCHAR(50) USING session_type::text;
ALTER TABLE tech_stack ALTER COLUMN category TYPE VARCHAR(50) USING category::text;
ALTER TABLE team_member ALTER COLUMN status TYPE VARCHAR(50) USING status::text;
ALTER TABLE team_member_role ALTER COLUMN role TYPE VARCHAR(50) USING role::text;
ALTER TABLE member_generation ALTER COLUMN role_in_gen TYPE VARCHAR(50) USING role_in_gen::text;

-- activity.type: partial index 먼저 drop 후 ALTER, 재생성
DROP INDEX IF EXISTS uq_activity_received;
DROP INDEX IF EXISTS uq_activity_normal;
ALTER TABLE activity ALTER COLUMN type TYPE VARCHAR(50) USING type::text;
CREATE UNIQUE INDEX uq_activity_received ON activity (member_id, type, reference_id, actor_id)
    WHERE type IN ('blog_post_like_received', 'session_event_post_like_received');
CREATE UNIQUE INDEX uq_activity_normal ON activity (member_id, type, reference_id)
    WHERE type NOT IN ('blog_post_like_received', 'session_event_post_like_received');

-- DEFAULT 값이 enum 타입을 참조하므로 먼저 문자열 리터럴로 교체 후 DROP
ALTER TABLE member_generation ALTER COLUMN role_in_gen SET DEFAULT 'member';
ALTER TABLE team_member ALTER COLUMN status SET DEFAULT 'pending';

DROP TYPE IF EXISTS session_type;
DROP TYPE IF EXISTS generation_role;
DROP TYPE IF EXISTS tech_stack_category;
DROP TYPE IF EXISTS activity_type;
DROP TYPE IF EXISTS contribution_period_type;
DROP TYPE IF EXISTS role_in_team;
DROP TYPE IF EXISTS team_member_status;

-- ===================== 롤백 SQL =====================
-- CREATE TYPE session_type AS ENUM ('backend','frontend','design','ai','pm','etc');
-- CREATE TYPE generation_role AS ENUM ('member','operating');
-- CREATE TYPE tech_stack_category AS ENUM ('language','framework','ai','design','tool','infra','etc');
-- CREATE TYPE activity_type AS ENUM ('blog_post','blog_comment','blog_post_like','blog_post_like_received','qna_question','qna_answer','qna_accepted','qna_comment','session_speak','session_event_post','session_event_comment','session_event_post_like','session_event_post_like_received');
-- CREATE TYPE contribution_period_type AS ENUM ('month','three_month','year','all');
-- CREATE TYPE role_in_team AS ENUM ('backend','frontend','design','ai','pm','infra','etc');
-- CREATE TYPE team_member_status AS ENUM ('pending','accepted','rejected','left','kicked');
--
-- ALTER TABLE member ALTER COLUMN session_type TYPE session_type USING session_type::session_type;
-- ALTER TABLE activity ALTER COLUMN type TYPE activity_type USING type::activity_type;
-- ALTER TABLE tech_stack ALTER COLUMN category TYPE tech_stack_category USING category::tech_stack_category;
-- ALTER TABLE team_member ALTER COLUMN status TYPE team_member_status USING status::team_member_status;
-- ALTER TABLE team_member_role ALTER COLUMN role TYPE role_in_team USING role::role_in_team;
-- ALTER TABLE member_generation ALTER COLUMN role_in_gen TYPE generation_role USING role_in_gen::generation_role;
