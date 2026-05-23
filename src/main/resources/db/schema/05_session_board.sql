-- NOTE: pg_trgm 설치는 superuser 권한이 필요합니다.
-- RDS 등 매니지드 환경에서는 아래 구문을 별도 superuser 세션 또는 프로비저닝 단계에서 실행하세요.
-- CREATE EXTENSION IF NOT EXISTS "pg_trgm";

CREATE TYPE post_status AS ENUM ('DRAFT', 'PUBLISHED', 'HIDDEN');

CREATE TABLE event_post (
    id            BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id     BIGINT      NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    generation_id BIGINT      NOT NULL REFERENCES generation(id) ON DELETE RESTRICT,
    type          TEXT        NOT NULL,
    title         TEXT        NOT NULL,
    body          TEXT,
    tags          TEXT[]      NOT NULL DEFAULT '{}',
    status        post_status NOT NULL DEFAULT 'DRAFT',
    like_count    INT         NOT NULL DEFAULT 0,
    comment_count INT         NOT NULL DEFAULT 0,
    has_thumb     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ
);

CREATE TABLE event_post_image (
    id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id    BIGINT      NOT NULL REFERENCES event_post(id) ON DELETE CASCADE,
    url        TEXT        NOT NULL,
    "order"    INT         NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE event_post_like (
    id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id  BIGINT      NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    post_id    BIGINT      NOT NULL REFERENCES event_post(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (member_id, post_id)
);

CREATE TABLE event_post_comment (
    id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id    BIGINT      NOT NULL REFERENCES event_post(id) ON DELETE CASCADE,
    author_id  BIGINT      NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    parent_id  BIGINT      REFERENCES event_post_comment (id) ON DELETE CASCADE,
    content    TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TYPE session_status AS ENUM ('SCHEDULED', 'ONGOING', 'DONE');

CREATE TABLE session (
    id            BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    generation_id BIGINT         NOT NULL REFERENCES generation(id) ON DELETE RESTRICT,
    week_label    TEXT,
    title         TEXT           NOT NULL,
    status        session_status NOT NULL DEFAULT 'SCHEDULED',
    started_at    TIMESTAMPTZ
);

CREATE TABLE session_speaker (
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    session_id BIGINT NOT NULL REFERENCES session(id) ON DELETE CASCADE,
    member_id  BIGINT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    role       TEXT,
    UNIQUE (session_id, member_id)
);

CREATE TABLE session_note (
    id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id BIGINT      NOT NULL REFERENCES session(id) ON DELETE CASCADE,
    author_id  BIGINT      NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    body       TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE note_link (
    id      BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    note_id BIGINT NOT NULL REFERENCES session_note(id) ON DELETE CASCADE,
    label   TEXT,
    url     TEXT NOT NULL,
    "order" INT  NOT NULL DEFAULT 0
);

CREATE TYPE resource_visibility AS ENUM ('PUBLIC', 'MEMBER', 'PRIVATE');

CREATE TABLE resource (
    id          BIGINT              GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id  BIGINT              NOT NULL REFERENCES session(id) ON DELETE CASCADE,
    uploader_id BIGINT              NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    type        TEXT                NOT NULL,
    name        TEXT                NOT NULL,
    url         TEXT                NOT NULL,
    size_label  TEXT,
    visibility  resource_visibility NOT NULL DEFAULT 'MEMBER',
    uploaded_at TIMESTAMPTZ         NOT NULL DEFAULT now()
);

CREATE TABLE retro (
    id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id BIGINT      NOT NULL REFERENCES session(id) ON DELETE CASCADE,
    author_id  BIGINT      NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    rating     INT         CHECK (rating BETWEEN 1 AND 5),
    body       TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_event_post_author     ON event_post(author_id);
CREATE INDEX idx_event_post_generation ON event_post(generation_id);
CREATE INDEX idx_event_post_status     ON event_post(status);
CREATE INDEX idx_event_post_tags       ON event_post USING GIN(tags);
CREATE INDEX idx_event_post_title      ON event_post USING GIN(title gin_trgm_ops);
CREATE INDEX idx_event_post_body       ON event_post USING GIN(body  gin_trgm_ops);

CREATE INDEX idx_post_image_post       ON event_post_image(post_id);

CREATE INDEX idx_like_post             ON event_post_like (post_id);

CREATE INDEX idx_comment_post          ON event_post_comment (post_id);
CREATE INDEX idx_comment_parent        ON event_post_comment (parent_id);

CREATE INDEX idx_session_generation    ON session(generation_id);
CREATE INDEX idx_session_status        ON session(status);

CREATE INDEX idx_session_note_session  ON session_note(session_id);
CREATE INDEX idx_session_note_body     ON session_note USING GIN(body gin_trgm_ops);

CREATE INDEX idx_note_link_note        ON note_link(note_id);

CREATE INDEX idx_resource_session      ON resource(session_id);
CREATE INDEX idx_resource_visibility   ON resource(visibility);
CREATE INDEX idx_resource_name         ON resource USING GIN(name gin_trgm_ops);

CREATE INDEX idx_retro_session         ON retro(session_id);
