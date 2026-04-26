-- ============================================================
-- Auth / User — PostgreSQL DDL
-- ============================================================
-- 이 파일은 users 테이블과
-- 인증에 사용되는 refresh_tokens 테이블을 정의합니다.
-- ============================================================

-- ------------------------------------------------------------
-- USERS
-- ------------------------------------------------------------
CREATE TYPE user_role   AS ENUM ('ADMIN', 'MEMBER');
CREATE TYPE user_status AS ENUM ('PENDING', 'ACTIVE', 'REJECTED', 'EXPIRED');

CREATE TABLE users (
    id                   BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login_email          TEXT        NOT NULL UNIQUE,
    password_hash        TEXT        NOT NULL,
    role                 user_role   NOT NULL DEFAULT 'MEMBER',
    status               user_status NOT NULL DEFAULT 'PENDING',
    signup_requested_at  TIMESTAMPTZ NOT NULL,
    approved_at          TIMESTAMPTZ,
    approved_by          BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    expired_at           TIMESTAMPTZ,
    last_login_at        TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX idx_user_login_email ON users(login_email);
CREATE        INDEX idx_user_status      ON users(status);

-- ------------------------------------------------------------
-- REFRESH_TOKENS
-- ------------------------------------------------------------
CREATE TYPE refresh_token_status AS ENUM ('ACTIVE', 'USED', 'REVOKED');

CREATE TABLE refresh_tokens (
    id          BIGINT               GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT               NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  TEXT                 NOT NULL UNIQUE,
    family_id   UUID                 NOT NULL,
    status      refresh_token_status NOT NULL,
    expires_at  TIMESTAMPTZ          NOT NULL,
    created_at  TIMESTAMPTZ          NOT NULL
);

CREATE UNIQUE INDEX idx_refresh_token_hash   ON refresh_tokens(token_hash);
CREATE        INDEX idx_refresh_token_family ON refresh_tokens(family_id);
CREATE        INDEX idx_refresh_token_user   ON refresh_tokens(user_id);
