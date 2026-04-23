-- ============================================================
-- Event Session Board — PostgreSQL DDL
-- ============================================================
-- NOTE: 아래 테이블은 세션보드 팀이 소유하지 않습니다.
--   "user"    → 프로필 팀 DDL에서 생성
--   generation → 프로필 팀 DDL에서 생성
-- FK 제약은 shared DB의 참조 무결성을 위해 유지합니다.
-- notification → common 레이어에서 관리 (해당 DDL 별도 참고)
-- ============================================================

-- pg_trgm: 한국어를 포함한 텍스트 컬럼의 부분 일치 검색(LIKE '%keyword%')을 위한 확장.
-- PostgreSQL 기본 전문 검색(tsvector)은 한국어 형태소 분석을 지원하지 않으므로,
-- 언어에 무관하게 문자 단위 n-gram으로 인덱싱하는 pg_trgm을 사용한다.
-- 이 확장이 활성화되어야 아래 gin_trgm_ops 인덱스들이 동작한다.
CREATE EXTENSION IF NOT EXISTS "pg_trgm";


--임시 generation Table----
-- 이건 프로필 팀의 것이라 꼭 빼야함!! --
-- 테스트 용도로 잠시 넣어둔 것임 --
CREATE TABLE generation (
                            id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                            label      TEXT NOT NULL,
                            number     INT NOT NULL UNIQUE,
                            start_date DATE NOT NULL,
                            end_date   DATE,
                            is_current BOOLEAN NOT NULL DEFAULT FALSE,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- EVENT_POST
-- ------------------------------------------------------------
CREATE TYPE post_status AS ENUM ('draft', 'published', 'hidden');

CREATE TABLE event_post (
                            id            BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            author_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            generation_id BIGINT      NOT NULL REFERENCES generation(id) ON DELETE RESTRICT,
                            type          TEXT        NOT NULL,
                            title         TEXT        NOT NULL,
                            body          TEXT,
                            tags          TEXT[]      NOT NULL DEFAULT '{}',
                            status        post_status NOT NULL DEFAULT 'draft',
                            like_count    INT         NOT NULL DEFAULT 0,
                            published_at  TIMESTAMPTZ,
                            created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- POST_IMAGE
-- ------------------------------------------------------------
CREATE TABLE post_image (
                            id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            post_id    BIGINT      NOT NULL REFERENCES event_post(id) ON DELETE CASCADE,
                            url        TEXT        NOT NULL,
                            "order"    INT         NOT NULL DEFAULT 0,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- LIKE
-- ------------------------------------------------------------
CREATE TABLE "like" (
                        id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        user_id    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        post_id    BIGINT      NOT NULL REFERENCES event_post(id) ON DELETE CASCADE,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                        UNIQUE (user_id, post_id)
);

-- ------------------------------------------------------------
-- COMMENT
-- ------------------------------------------------------------
CREATE TABLE comment (
                         id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         post_id    BIGINT      NOT NULL REFERENCES event_post(id) ON DELETE CASCADE,
                         author_id  BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         parent_id  BIGINT      REFERENCES comment(id) ON DELETE CASCADE,
                         content    TEXT        NOT NULL,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- SESSION
-- ------------------------------------------------------------
CREATE TYPE session_status AS ENUM ('scheduled', 'ongoing', 'done');

CREATE TABLE session (
                         id            BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         generation_id BIGINT         NOT NULL REFERENCES generation(id) ON DELETE RESTRICT,
                         week_label    TEXT,
                         title         TEXT           NOT NULL,
                         status        session_status NOT NULL DEFAULT 'scheduled',
                         started_at    TIMESTAMPTZ
);

-- ------------------------------------------------------------
-- SESSION_SPEAKER
-- ------------------------------------------------------------
CREATE TABLE session_speaker (
                                 id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                 session_id BIGINT NOT NULL REFERENCES session(id) ON DELETE CASCADE,
                                 user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                 role       TEXT,
                                 UNIQUE (session_id, user_id)
);

-- ------------------------------------------------------------
-- SESSION_NOTE
-- ------------------------------------------------------------
CREATE TABLE session_note (
                              id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              session_id BIGINT      NOT NULL REFERENCES session(id) ON DELETE CASCADE,
                              author_id  BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              body       TEXT,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- NOTE_LINK
-- ------------------------------------------------------------
CREATE TABLE note_link (
                           id      BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                           note_id BIGINT NOT NULL REFERENCES session_note(id) ON DELETE CASCADE,
                           label   TEXT,
                           url     TEXT NOT NULL,
                           "order" INT  NOT NULL DEFAULT 0
);

-- ------------------------------------------------------------
-- RESOURCE
-- ------------------------------------------------------------
CREATE TYPE resource_visibility AS ENUM ('public', 'member', 'private');

CREATE TABLE resource (
                          id          BIGINT              GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          session_id  BIGINT              NOT NULL REFERENCES session(id) ON DELETE CASCADE,
                          uploader_id BIGINT              NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          type        TEXT                NOT NULL,
                          name        TEXT                NOT NULL,
                          url         TEXT                NOT NULL,
                          size_label  TEXT,
                          visibility  resource_visibility NOT NULL DEFAULT 'member',
                          uploaded_at TIMESTAMPTZ         NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- RETRO
-- ------------------------------------------------------------
CREATE TABLE retro (
                       id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       session_id BIGINT      NOT NULL REFERENCES session(id) ON DELETE CASCADE,
                       author_id  BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       rating     INT         CHECK (rating BETWEEN 1 AND 5),
                       body       TEXT,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_event_post_author     ON event_post(author_id);
CREATE INDEX idx_event_post_generation ON event_post(generation_id);
CREATE INDEX idx_event_post_status     ON event_post(status);
-- tags 컬럼은 TEXT[] 배열이므로 GIN 인덱스로 배열 포함 연산(@>)을 지원한다.
CREATE INDEX idx_event_post_tags       ON event_post USING GIN(tags);
-- 게시글 제목/본문 텍스트 검색 (ILIKE '%keyword%') — pg_trgm gin_trgm_ops 사용
CREATE INDEX idx_event_post_title      ON event_post USING GIN(title gin_trgm_ops);
CREATE INDEX idx_event_post_body       ON event_post USING GIN(body  gin_trgm_ops);

CREATE INDEX idx_post_image_post       ON post_image(post_id);

CREATE INDEX idx_like_post             ON "like"(post_id);

CREATE INDEX idx_comment_post          ON comment(post_id);
CREATE INDEX idx_comment_parent        ON comment(parent_id);

CREATE INDEX idx_session_generation    ON session(generation_id);
CREATE INDEX idx_session_status        ON session(status);

CREATE INDEX idx_session_note_session  ON session_note(session_id);
-- 세션 노트 본문 텍스트 검색 (ILIKE '%keyword%') — pg_trgm gin_trgm_ops 사용
CREATE INDEX idx_session_note_body     ON session_note USING GIN(body gin_trgm_ops);

CREATE INDEX idx_note_link_note        ON note_link(note_id);

CREATE INDEX idx_resource_session      ON resource(session_id);
CREATE INDEX idx_resource_visibility   ON resource(visibility);
-- 자료 이름 텍스트 검색 (ILIKE '%keyword%') — pg_trgm gin_trgm_ops 사용
CREATE INDEX idx_resource_name         ON resource USING GIN(name gin_trgm_ops);

CREATE INDEX idx_retro_session         ON retro(session_id);