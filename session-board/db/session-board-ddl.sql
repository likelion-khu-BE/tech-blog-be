-- ============================================================
-- Event Session Board — PostgreSQL DDL
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ------------------------------------------------------------
-- USER
-- ------------------------------------------------------------
CREATE TABLE "user" (
                        id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                        name        TEXT        NOT NULL,
                        initial     TEXT,
                        avatar_style TEXT,
                        created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- GENERATION
-- ------------------------------------------------------------
CREATE TABLE generation (
                            id         SERIAL      PRIMARY KEY,
                            label      TEXT        NOT NULL,
                            is_current BOOLEAN     NOT NULL DEFAULT false
);

-- ------------------------------------------------------------
-- EVENT_POST
-- ------------------------------------------------------------
CREATE TYPE post_status AS ENUM ('draft', 'published', 'hidden');

CREATE TABLE event_post (
                            id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                            author_id     UUID        NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                            generation_id INT         NOT NULL REFERENCES generation(id) ON DELETE RESTRICT,
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
                            id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                            post_id    UUID        NOT NULL REFERENCES event_post(id) ON DELETE CASCADE,
                            url        TEXT        NOT NULL,
                            "order"    INT         NOT NULL DEFAULT 0,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- LIKE
-- ------------------------------------------------------------
CREATE TABLE "like" (
                        id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                        user_id    UUID        NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                        post_id    UUID        NOT NULL REFERENCES event_post(id) ON DELETE CASCADE,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                        UNIQUE (user_id, post_id)
);

-- ------------------------------------------------------------
-- COMMENT
-- ------------------------------------------------------------
CREATE TABLE comment (
                         id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                         post_id    UUID        NOT NULL REFERENCES event_post(id) ON DELETE CASCADE,
                         author_id  UUID        NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                         parent_id  UUID        REFERENCES comment(id) ON DELETE CASCADE,
                         content    TEXT        NOT NULL,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- SESSION
-- ------------------------------------------------------------
CREATE TYPE session_status AS ENUM ('scheduled', 'ongoing', 'done');

CREATE TABLE session (
                         id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                         generation_id INT           NOT NULL REFERENCES generation(id) ON DELETE RESTRICT,
                         week_label    TEXT,
                         title         TEXT          NOT NULL,
                         status        session_status NOT NULL DEFAULT 'scheduled',
                         started_at    TIMESTAMPTZ
);

-- ------------------------------------------------------------
-- SESSION_SPEAKER
-- ------------------------------------------------------------
CREATE TABLE session_speaker (
                                 id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 session_id UUID NOT NULL REFERENCES session(id) ON DELETE CASCADE,
                                 user_id    UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                                 role       TEXT,
                                 UNIQUE (session_id, user_id)
);

-- ------------------------------------------------------------
-- SESSION_NOTE
-- ------------------------------------------------------------
CREATE TABLE session_note (
                              id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                              session_id UUID        NOT NULL REFERENCES session(id) ON DELETE CASCADE,
                              author_id  UUID        NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                              body       TEXT,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- NOTE_LINK
-- ------------------------------------------------------------
CREATE TABLE note_link (
                           id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           note_id UUID NOT NULL REFERENCES session_note(id) ON DELETE CASCADE,
                           label   TEXT,
                           url     TEXT NOT NULL,
                           "order" INT  NOT NULL DEFAULT 0
);

-- ------------------------------------------------------------
-- RESOURCE
-- ------------------------------------------------------------
CREATE TYPE resource_visibility AS ENUM ('public', 'member', 'private');

CREATE TABLE resource (
                          id          UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
                          session_id  UUID                NOT NULL REFERENCES session(id) ON DELETE CASCADE,
                          uploader_id UUID                NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
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
                       id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                       session_id UUID        NOT NULL REFERENCES session(id) ON DELETE CASCADE,
                       author_id  UUID        NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                       rating     INT         CHECK (rating BETWEEN 1 AND 5),
                       body       TEXT,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- NOTIFICATION
-- ------------------------------------------------------------
CREATE TABLE notification (
                              id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                              receiver_id UUID        NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                              actor_id    UUID        NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                              type        TEXT        NOT NULL,
                              target_id   UUID,
                              target_type TEXT,
                              is_read     BOOLEAN     NOT NULL DEFAULT false,
                              created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_event_post_author     ON event_post(author_id);
CREATE INDEX idx_event_post_generation ON event_post(generation_id);
CREATE INDEX idx_event_post_status     ON event_post(status);
CREATE INDEX idx_event_post_tags       ON event_post USING GIN(tags);

CREATE INDEX idx_post_image_post       ON post_image(post_id);

CREATE INDEX idx_like_post             ON "like"(post_id);

CREATE INDEX idx_comment_post          ON comment(post_id);
CREATE INDEX idx_comment_parent        ON comment(parent_id);

CREATE INDEX idx_session_generation    ON session(generation_id);
CREATE INDEX idx_session_status        ON session(status);

CREATE INDEX idx_session_note_session  ON session_note(session_id);

CREATE INDEX idx_note_link_note        ON note_link(note_id);

CREATE INDEX idx_resource_session      ON resource(session_id);
CREATE INDEX idx_resource_visibility   ON resource(visibility);

CREATE INDEX idx_retro_session         ON retro(session_id);

CREATE INDEX idx_notification_receiver ON notification(receiver_id);
CREATE INDEX idx_notification_is_read  ON notification(receiver_id, is_read);