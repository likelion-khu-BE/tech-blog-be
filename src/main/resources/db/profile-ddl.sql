-- ============================================================
-- Profile — PostgreSQL DDL
-- ============================================================

-- ------------------------------------------------------------
-- GENERATION
-- ------------------------------------------------------------
CREATE TABLE generation (
    id          BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    label       TEXT        NOT NULL,
    number      INT         NOT NULL UNIQUE,
    start_date  DATE        NOT NULL,
    end_date    DATE,
    is_current  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX idx_generation_number ON generation(number);

-- ------------------------------------------------------------
-- TECH_STACK
-- ------------------------------------------------------------
CREATE TABLE tech_stack (
    id          BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    category    VARCHAR(20)  NOT NULL,
    logo_url    TEXT,
    created_at  TIMESTAMPTZ  NOT NULL
);

CREATE UNIQUE INDEX idx_tech_stack_name ON tech_stack(name);

-- ------------------------------------------------------------
-- MEMBER
-- ------------------------------------------------------------
CREATE TABLE member (
    id                BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id           BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    name              VARCHAR(100) NOT NULL,
    department        VARCHAR(100),
    session_type      VARCHAR(20)  NOT NULL,
    profile_image_url TEXT,
    github_url        TEXT,
    displayed_email   TEXT,
    intro             TEXT,
    links_json        JSONB,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL
);

CREATE UNIQUE INDEX idx_member_user_id ON member(user_id);

-- ------------------------------------------------------------
-- MEMBER_GENERATION
-- ------------------------------------------------------------
CREATE TABLE member_generation (
    id            BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id     BIGINT      NOT NULL REFERENCES member(id)     ON DELETE CASCADE,
    generation_id BIGINT      NOT NULL REFERENCES generation(id) ON DELETE CASCADE,
    role_in_gen   VARCHAR(20) NOT NULL DEFAULT 'member',
    joined_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_member_generation UNIQUE (member_id, generation_id)
);

-- ------------------------------------------------------------
-- MEMBER_TECH_STACK
-- ------------------------------------------------------------
CREATE TABLE member_tech_stack (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id     BIGINT NOT NULL REFERENCES member(id)     ON DELETE CASCADE,
    tech_stack_id BIGINT NOT NULL REFERENCES tech_stack(id) ON DELETE CASCADE,
    proficiency   INT,
    created_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_member_tech_stack UNIQUE (member_id, tech_stack_id)
);

-- ------------------------------------------------------------
-- ACTIVITY
-- ------------------------------------------------------------
CREATE TABLE activity (
    id             BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id      BIGINT      NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    type           VARCHAR(30) NOT NULL,
    reference_id   BIGINT,
    reference_type VARCHAR(50),
    score          INT         NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_activity_member ON activity(member_id);
CREATE INDEX idx_activity_type   ON activity(type);

-- ------------------------------------------------------------
-- TEAM_PROFILE
-- ------------------------------------------------------------
CREATE TABLE team_profile (
    id            BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    generation_id BIGINT       REFERENCES generation(id) ON DELETE SET NULL,
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    project_url   TEXT,
    github_url    TEXT,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_team_profile_generation ON team_profile(generation_id);

-- ------------------------------------------------------------
-- TEAM_IMAGE
-- ------------------------------------------------------------
CREATE TABLE team_image (
    id          BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    team_id     BIGINT      NOT NULL REFERENCES team_profile(id) ON DELETE CASCADE,
    image_url   TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_team_image_team ON team_image(team_id);

-- ------------------------------------------------------------
-- TEAM_MEMBER
-- ------------------------------------------------------------
CREATE TABLE team_member (
    id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    team_id    BIGINT      NOT NULL REFERENCES team_profile(id) ON DELETE CASCADE,
    member_id  BIGINT      NOT NULL REFERENCES member(id)       ON DELETE CASCADE,
    is_lead    BOOLEAN     NOT NULL DEFAULT FALSE,
    status     VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_team_member UNIQUE (team_id, member_id)
);

CREATE INDEX idx_team_member_team   ON team_member(team_id);
CREATE INDEX idx_team_member_member ON team_member(member_id);

-- ------------------------------------------------------------
-- TEAM_MEMBER_ROLE
-- ------------------------------------------------------------
CREATE TABLE team_member_role (
    id             BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    team_member_id BIGINT      NOT NULL REFERENCES team_member(id) ON DELETE CASCADE,
    role           VARCHAR(20) NOT NULL
);

CREATE INDEX idx_team_member_role_member ON team_member_role(team_member_id);

-- ------------------------------------------------------------
-- TEAM_TECH_STACK
-- ------------------------------------------------------------
CREATE TABLE team_tech_stack (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    team_id       BIGINT NOT NULL REFERENCES team_profile(id) ON DELETE CASCADE,
    tech_stack_id BIGINT NOT NULL REFERENCES tech_stack(id)   ON DELETE CASCADE,
    CONSTRAINT uq_team_tech_stack UNIQUE (team_id, tech_stack_id)
);

CREATE INDEX idx_team_tech_stack_team ON team_tech_stack(team_id);