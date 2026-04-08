-- ============================================================
-- PROFILE DOMAIN — PostgreSQL DDL (aligned with session-board)
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ------------------------------------------------------------
-- MEMBER
-- ------------------------------------------------------------
CREATE TYPE session_type AS ENUM ('backend', 'frontend', 'design', 'pm', 'etc');

CREATE TABLE member (
                        id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                        name              TEXT         NOT NULL,
                        email             TEXT         NOT NULL,
                        school            TEXT,
                        department        TEXT,
                        session_type      session_type NOT NULL,      -- 백/프론트/디자인 등
                        bio               TEXT,
                        profile_image_url TEXT,
                        github_url        TEXT,
                        links_json        JSONB,                     -- 기타 링크들 (블로그, 포트폴리오 등)
                        created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                        updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_member_email ON member(email);

-- ------------------------------------------------------------
-- GENERATION
-- ------------------------------------------------------------
CREATE TABLE generation (
                            id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                            label      TEXT        NOT NULL,             -- '14기' 같은 표시용
                            number     INT         NOT NULL,             -- 기수 번호 (14, 15 ...)
                            start_date DATE        NOT NULL,
                            end_date   DATE,
                            is_current BOOLEAN     NOT NULL DEFAULT false,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_generation_number ON generation(number);

-- ------------------------------------------------------------
-- MEMBER_GENERATION (연임 + 역할 + 운영진 여부)
-- ------------------------------------------------------------
CREATE TYPE generation_role AS ENUM ('member', 'mentor', 'lead', 'admin');

CREATE TABLE member_generation (
                                   id            UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                                   member_id     UUID            NOT NULL REFERENCES member(id) ON DELETE CASCADE,
                                   generation_id UUID            NOT NULL REFERENCES generation(id) ON DELETE CASCADE,
                                   role_in_gen   generation_role NOT NULL DEFAULT 'member',
                                   is_operating  BOOLEAN         NOT NULL DEFAULT false,   -- 이 기수에서 운영진인지
                                   joined_at     TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_member_generation
    ON member_generation(member_id, generation_id);

CREATE INDEX idx_member_generation_generation
    ON member_generation(generation_id);

CREATE INDEX idx_member_generation_role
    ON member_generation(role_in_gen);

-- ------------------------------------------------------------
-- TECH STACK
-- ------------------------------------------------------------
CREATE TYPE tech_stack_category AS ENUM ('language', 'framework', 'tool', 'infra');

CREATE TABLE tech_stack (
                            id         UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
                            name       TEXT                NOT NULL,
                            category   tech_stack_category NOT NULL,
                            created_at TIMESTAMPTZ         NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_tech_stack_name ON tech_stack(name);

-- 멤버-기술 스택 N:N + 숙련도
CREATE TABLE member_tech_stack (
                                   id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                   member_id      UUID        NOT NULL REFERENCES member(id) ON DELETE CASCADE,
                                   tech_stack_id  UUID        NOT NULL REFERENCES tech_stack(id) ON DELETE CASCADE,
                                   proficiency    INT,                          -- 1~5 같은 스코어
                                   description    TEXT,                         -- "개인 프로젝트 2개 진행" 등 코멘트
                                   created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_member_tech_stack
    ON member_tech_stack(member_id, tech_stack_id);

CREATE INDEX idx_member_tech_stack_stack
    ON member_tech_stack(tech_stack_id);

CREATE INDEX idx_member_tech_stack_member
    ON member_tech_stack(member_id);

-- ------------------------------------------------------------
-- TEAM PROFILE (프로젝트)
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

-- 팀-멤버 관계 (역할 포함)
CREATE TABLE team_member (
                             id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                             team_id      UUID        NOT NULL REFERENCES team_profile(id) ON DELETE CASCADE,
                             member_id    UUID        NOT NULL REFERENCES member(id) ON DELETE CASCADE,
                             role_in_team TEXT,                         -- 'BE', 'FE', 'DESIGN', 'PM', 'LEAD' 등 자유 텍스트
                             is_lead      BOOLEAN     NOT NULL DEFAULT false,
                             created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_team_member
    ON team_member(team_id, member_id);

-- 팀이 사용한 기술 스택 (선택)
CREATE TABLE team_tech_stack (
                                 id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                 team_id       UUID        NOT NULL REFERENCES team_profile(id) ON DELETE CASCADE,
                                 tech_stack_id UUID        NOT NULL REFERENCES tech_stack(id) ON DELETE CASCADE,
                                 created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_team_tech_stack
    ON team_tech_stack(team_id, tech_stack_id);

-- ------------------------------------------------------------
-- ACTIVITY & CONTRIBUTION
-- ------------------------------------------------------------
CREATE TYPE activity_type AS ENUM (
    'blog_post',
    'qna_answer',
    'qna_accepted',
    'session_talk',
    'other'
);

CREATE TABLE activity (
                          id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                          member_id      UUID          NOT NULL REFERENCES member(id) ON DELETE CASCADE,
                          type           activity_type NOT NULL,
                          reference_id   UUID,                        -- 원본 글/답변/세션 ID
                          reference_type TEXT,                        -- 'blog', 'qna', 'session' 등
                          score          INT           NOT NULL,      -- 이 활동으로 얻은 점수 (+10, +5 ...)
                          created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_member_created_at
    ON activity(member_id, created_at);

CREATE INDEX idx_activity_created_at
    ON activity(created_at);

-- 랭킹/통계용 요약 (선택: Phase 3~4에서 쓸 후보)
CREATE TYPE contribution_period_type AS ENUM ('month', 'three_month', 'year', 'all');

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
