-- ============================================================
-- PROFILE DOMAIN — PostgreSQL DDL (최종 정리본)
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ------------------------------------------------------------
-- 공통 ENUM 타입
-- ------------------------------------------------------------

-- 멤버 세션(트랙): 백엔드 / 프론트엔드 / 디자인 / AI / 기타
CREATE TYPE session_type AS ENUM ('backend', 'frontend', 'design', 'ai', 'pm', 'etc');

-- 기수 내 역할: 부원 / 운영진
CREATE TYPE generation_role AS ENUM ('member', 'operating');

-- 기술 스택 카테고리
CREATE TYPE tech_stack_category AS ENUM (
    'language',
    'backend',
    'frontend',
    'ai',
    'design',
    'tool',
    'infra',
    'etc'
);

-- 활동 타입
CREATE TYPE activity_type AS ENUM (
    'blog_post',
    'qna_answer',
    'qna_accepted',
    'session_talk',
    'other'
);

-- 기여도 집계 기간 타입
CREATE TYPE contribution_period_type AS ENUM ('month', 'three_month', 'year', 'all');

-- ------------------------------------------------------------
-- 1. MEMBER
-- ------------------------------------------------------------

CREATE TABLE member (
                        id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                        name              TEXT         NOT NULL,
                        email             TEXT         NOT NULL,
                        department        TEXT,
                        session_type      session_type NOT NULL,      -- backend / frontend / design / ai / ...
                        intro             TEXT,                      -- 소개글
                        profile_image_url TEXT,
                        github_url        TEXT,
                        links_json        JSONB,                    -- 기타 링크들 (블로그, 포폴, 노션 등)
                        created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                        updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_member_email ON member(email);

-- ------------------------------------------------------------
-- 2. GENERATION
-- ------------------------------------------------------------

CREATE TABLE generation (
                            id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                            label      TEXT        NOT NULL,         -- '14기' 같은 표시용 텍스트
                            number     INT         NOT NULL,         -- 기수 번호 (14, 15 ...)
                            start_date DATE        NOT NULL,         -- 활동 시작일
                            end_date   DATE,                         -- 활동 종료일 (진행 중이면 NULL 가능)
                            is_current BOOLEAN     NOT NULL DEFAULT false,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_generation_number ON generation(number);

-- ------------------------------------------------------------
-- 3. MEMBER_GENERATION (멤버–기수 관계: 부원/운영진)
-- ------------------------------------------------------------

CREATE TABLE member_generation (
                                   id            UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                                   member_id     UUID            NOT NULL REFERENCES member(id) ON DELETE CASCADE,
                                   generation_id UUID            NOT NULL REFERENCES generation(id) ON DELETE CASCADE,
                                   role_in_gen   generation_role NOT NULL DEFAULT 'member',  -- 부원 / 운영진
                                   joined_at     TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- 한 멤버가 같은 기수에 두 번 들어가는 건 금지
CREATE UNIQUE INDEX uq_member_generation
    ON member_generation(member_id, generation_id);

-- 기수별로 멤버를 빨리 찾기 위한 인덱스
CREATE INDEX idx_member_generation_generation
    ON member_generation(generation_id);

-- 역할(부원/운영진) 기준으로 필터링할 때 쓰는 인덱스
CREATE INDEX idx_member_generation_role
    ON member_generation(role_in_gen);

-- ------------------------------------------------------------
-- 4. TECH_STACK (기술 스택 마스터)
-- ------------------------------------------------------------

CREATE TABLE tech_stack (
                            id         UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
                            name       TEXT                NOT NULL,
                            category   tech_stack_category NOT NULL,
                            created_at TIMESTAMPTZ         NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_tech_stack_name ON tech_stack(name);

-- ------------------------------------------------------------
-- 5. MEMBER_TECH_STACK (멤버–기술 스택 N:N + 숙련도)
-- ------------------------------------------------------------

CREATE TABLE member_tech_stack (
                                   id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                   member_id      UUID        NOT NULL REFERENCES member(id) ON DELETE CASCADE,
                                   tech_stack_id  UUID        NOT NULL REFERENCES tech_stack(id) ON DELETE CASCADE,
                                   proficiency    INT,            -- 1~5 같은 스코어 (지금은 옵션, 나중에 써도 됨)
                                   created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 한 멤버가 같은 스택을 중복으로 등록하지 못하게 막는 제약
CREATE UNIQUE INDEX uq_member_tech_stack
    ON member_tech_stack(member_id, tech_stack_id);

-- "이 스택을 가진 사람들"을 빨리 찾기 위한 인덱스
CREATE INDEX idx_member_tech_stack_stack
    ON member_tech_stack(tech_stack_id);

-- "이 사람이 가진 모든 스택"을 빨리 불러오기 위한 인덱스
CREATE INDEX idx_member_tech_stack_member
    ON member_tech_stack(member_id);

-- ------------------------------------------------------------
-- 6. TEAM_PROFILE (팀/프로젝트 프로필)
-- ------------------------------------------------------------

CREATE TABLE team_profile (
                              id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                              generation_id UUID        REFERENCES generation(id) ON DELETE SET NULL,
                              name          TEXT        NOT NULL,         -- 팀/프로젝트 이름
                              description   TEXT,
                              project_url   TEXT,
                              created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                              updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- 7. TEAM_MEMBER (팀–멤버 N:N + 팀 내 역할)
-- ------------------------------------------------------------

CREATE TABLE team_member (
                             id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                             team_id      UUID        NOT NULL REFERENCES team_profile(id) ON DELETE CASCADE,
                             member_id    UUID        NOT NULL REFERENCES member(id) ON DELETE CASCADE,
                             role_in_team TEXT,                         -- 'BE', 'FE', 'Design', 'AI', 'PM', 'Lead' 등
                             is_lead      BOOLEAN     NOT NULL DEFAULT false,
                             created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 한 멤버가 같은 팀에 중복으로 들어가는 건 금지
CREATE UNIQUE INDEX uq_team_member
    ON team_member(team_id, member_id);

-- ------------------------------------------------------------
-- 8. TEAM_TECH_STACK (팀–기술 스택 N:N)
-- ------------------------------------------------------------

CREATE TABLE team_tech_stack (
                                 id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                 team_id       UUID        NOT NULL REFERENCES team_profile(id) ON DELETE CASCADE,
                                 tech_stack_id UUID        NOT NULL REFERENCES tech_stack(id) ON DELETE CASCADE,
                                 created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_team_tech_stack
    ON team_tech_stack(team_id, tech_stack_id);

-- ------------------------------------------------------------
-- 9. ACTIVITY (활동 로그)
-- ------------------------------------------------------------

CREATE TABLE activity (
                          id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                          member_id      UUID          NOT NULL REFERENCES member(id) ON DELETE CASCADE,
                          type           activity_type NOT NULL,
                          reference_id   UUID,                        -- 원본 글/답변/세션 ID
                          reference_type TEXT,                        -- 'blog', 'qna', 'session' 등
                          score          INT           NOT NULL,      -- 이 활동으로 얻은 점수 (+10, +5, +15 등)
                          created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_member_created_at
    ON activity(member_id, created_at);

CREATE INDEX idx_activity_created_at
    ON activity(created_at);

-- ------------------------------------------------------------
-- 10. CONTRIBUTION_SUMMARY (기간별/기수별 기여도 요약 - v1에선 미사용 가능)
-- ------------------------------------------------------------

CREATE TABLE contribution_summary (
                                      id            UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
                                      member_id     UUID                     NOT NULL REFERENCES member(id) ON DELETE CASCADE,
                                      generation_id UUID                     REFERENCES generation(id) ON DELETE SET NULL,
                                      period_type   contribution_period_type NOT NULL,   -- month / three_month / year / all
                                      period_start  DATE                     NOT NULL,
                                      period_end    DATE                     NOT NULL,
                                      total_score   INT                      NOT NULL,
                                      created_at    TIMESTAMPTZ             NOT NULL DEFAULT now()
);

CREATE INDEX idx_contribution_summary_period
    ON contribution_summary(period_type, period_start, period_end);

CREATE INDEX idx_contribution_summary_generation
    ON contribution_summary(generation_id);

CREATE INDEX idx_contribution_summary_score
    ON contribution_summary(total_score DESC);
