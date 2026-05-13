-- 1. 핵심 테이블 생성 (PK: BIGINT / Long)
-- 전제: users 테이블은 auth 팀 DDL이 먼저 생성해야 한다.
CREATE TABLE member (
    id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id           BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    name              TEXT NOT NULL,
    department        TEXT,
    session_type      VARCHAR(50) NOT NULL,
    profile_image_url TEXT,
    github_url        TEXT,
    displayed_email   TEXT,
    intro             TEXT,
    links_json        JSONB,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE generation (
    number     INT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date   DATE,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 한 시점에 활동 중인 기수는 1개 (is_current=TRUE인 row 1개로 제한)
CREATE UNIQUE INDEX uq_generation_is_current ON generation (is_current) WHERE is_current = TRUE;

-- 3. 관계 및 활동 테이블
CREATE TABLE member_generation (
    id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    member_id         BIGINT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    generation_number INT NOT NULL REFERENCES generation(number) ON DELETE CASCADE,
    role_in_gen       VARCHAR(50) NOT NULL DEFAULT 'member',
    joined_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_member_generation UNIQUE (member_id, generation_number)
);

CREATE TABLE tech_stack (
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name       TEXT NOT NULL UNIQUE,
    category   VARCHAR(50) NOT NULL,
    logo_url   TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE member_tech_stack (
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    member_id     BIGINT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    tech_stack_id BIGINT NOT NULL REFERENCES tech_stack(id) ON DELETE CASCADE,
    proficiency   INT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_member_tech_stack UNIQUE (member_id, tech_stack_id)
);

CREATE TABLE team_profile (
    id                     BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    generation_number      INT REFERENCES generation(number) ON DELETE SET NULL,
    name                   TEXT NOT NULL,
    description            TEXT,
    project_url            TEXT,
    github_url             TEXT,
    invite_code            TEXT NOT NULL UNIQUE,
    invite_code_expires_at TIMESTAMPTZ NOT NULL,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE team_tech_stack (
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    team_id       BIGINT NOT NULL REFERENCES team_profile(id) ON DELETE CASCADE,
    tech_stack_id BIGINT NOT NULL REFERENCES tech_stack(id) ON DELETE CASCADE,
    CONSTRAINT uq_team_tech_stack UNIQUE (team_id, tech_stack_id)
);

CREATE TABLE team_member (
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    team_id    BIGINT NOT NULL REFERENCES team_profile(id) ON DELETE CASCADE,
    member_id  BIGINT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    is_lead    BOOLEAN NOT NULL DEFAULT FALSE,
    status     VARCHAR(50) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_team_member UNIQUE (team_id, member_id)
);

CREATE TABLE team_member_role (
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    team_member_id BIGINT NOT NULL REFERENCES team_member(id) ON DELETE CASCADE,
    role           VARCHAR(50) NOT NULL
);

CREATE TABLE team_image (
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    team_id    BIGINT NOT NULL REFERENCES team_profile(id) ON DELETE CASCADE,
    image_url  TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE activity (
    id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    member_id    BIGINT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    type         VARCHAR(50) NOT NULL,
    reference_id BIGINT,
    actor_id     BIGINT,
    score        INT NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 멱등성 보장: 같은 활동을 두 번 기록 차단.
-- received류는 actor_id(누가 누른 좋아요인지)까지 포함해 unique.
-- 그 외 type은 (member, type, reference)만으로 unique.
CREATE UNIQUE INDEX uq_activity_received
    ON activity (member_id, type, reference_id, actor_id)
    WHERE type IN ('blog_post_like_received', 'session_event_post_like_received');

CREATE UNIQUE INDEX uq_activity_normal
    ON activity (member_id, type, reference_id)
    WHERE type NOT IN ('blog_post_like_received', 'session_event_post_like_received');

-- 활동 기록 실패 영구 로그. ADR 0003 §처리 실패 시 복구 전략의 DB 백업.
-- 운영자가 SQL 조회로 누락 발견 + payload_json 보고 수동 보정.
CREATE TABLE activity_failure (
    id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    event_type   TEXT NOT NULL,
    payload_json JSONB NOT NULL,
    error_class  TEXT NOT NULL,
    error_msg    TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_failure_created ON activity_failure (created_at DESC);