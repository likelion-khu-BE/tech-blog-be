-- ============================================================
-- Blog — PostgreSQL DDL
-- ============================================================
-- 이 파일은 기술 블로그의 게시글(blog_posts), 댓글(blog_comments),
-- 좋아요(likes), 북마크(bookmarks), 태그(tags) 테이블을 정의합니다.
-- ============================================================

-- ------------------------------------------------------------
-- BLOG_POSTS
-- ------------------------------------------------------------
CREATE TABLE blog_posts (
    id              BIGSERIAL     PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    title           VARCHAR(255)  NOT NULL,
    content         TEXT          NOT NULL,
    board           VARCHAR(20)   NOT NULL,
    category        VARCHAR(20)   NOT NULL,
    status          VARCHAR(20)   NOT NULL CHECK (status IN ('DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'REJECTED', 'HIDDEN')),
    generation      VARCHAR(10),
    reply_to_id     BIGINT,
    rejected_reason TEXT,
    created_at      TIMESTAMP(6)  NOT NULL,
    updated_at      TIMESTAMP(6)  NOT NULL,
    hidden_at       TIMESTAMP(6)
);

CREATE INDEX idx_blog_post_user           ON blog_posts (user_id);
CREATE INDEX idx_blog_post_status         ON blog_posts (status);
CREATE INDEX idx_blog_post_board_category ON blog_posts (board, category);
CREATE INDEX idx_blog_post_generation     ON blog_posts (generation);
CREATE INDEX idx_blog_post_created        ON blog_posts (created_at DESC);

-- ------------------------------------------------------------
-- BLOG_POST_TAGS
-- ------------------------------------------------------------
CREATE TABLE blog_post_tags (
    post_id   BIGINT      NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    tag_name  VARCHAR(50) NOT NULL,
    PRIMARY KEY (post_id, tag_name)
);

CREATE INDEX idx_blog_post_tag_name ON blog_post_tags (tag_name);

-- ------------------------------------------------------------
-- BLOG_POST_LIKES
-- ------------------------------------------------------------
CREATE TABLE blog_post_likes (
    post_id  BIGINT NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    user_id  BIGINT NOT NULL,
    PRIMARY KEY (post_id, user_id)
);

CREATE INDEX idx_blog_post_like_user ON blog_post_likes (user_id);

-- ------------------------------------------------------------
-- BLOG_POST_BOOKMARKS
-- ------------------------------------------------------------
CREATE TABLE blog_post_bookmarks (
    post_id  BIGINT NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    user_id  BIGINT NOT NULL,
    PRIMARY KEY (post_id, user_id)
);

CREATE INDEX idx_blog_post_bookmark_user ON blog_post_bookmarks (user_id);

-- ------------------------------------------------------------
-- BLOG_COMMENTS
-- ------------------------------------------------------------
CREATE TABLE blog_comments (
    id          BIGSERIAL    PRIMARY KEY,
    post_id     BIGINT       NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    user_id     BIGINT       NOT NULL,
    parent_id   BIGINT       REFERENCES blog_comments(id) ON DELETE CASCADE,
    content     TEXT         NOT NULL,
    created_at  TIMESTAMP(6) NOT NULL,
    deleted_at  TIMESTAMP(6),
    hidden_at   TIMESTAMP(6)
);

CREATE INDEX idx_blog_comment_post    ON blog_comments (post_id);
CREATE INDEX idx_blog_comment_user    ON blog_comments (user_id);
CREATE INDEX idx_blog_comment_parent  ON blog_comments (parent_id);
CREATE INDEX idx_blog_comment_created ON blog_comments (created_at);

-- ------------------------------------------------------------
-- BLOG_COMMENT_LIKES
-- ------------------------------------------------------------
CREATE TABLE blog_comment_likes (
    comment_id  BIGINT NOT NULL REFERENCES blog_comments(id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL,
    PRIMARY KEY (comment_id, user_id)
);

CREATE INDEX idx_blog_comment_like_user ON blog_comment_likes (user_id);

-- ------------------------------------------------------------
-- ADMIN_ACTION_LOGS
-- ------------------------------------------------------------
CREATE TABLE admin_action_logs (
    id             BIGSERIAL    PRIMARY KEY,
    actor_user_id  BIGINT       NOT NULL,
    target_type    VARCHAR(20)  NOT NULL,
    target_id      TEXT         NOT NULL,
    action_type    VARCHAR(30)  NOT NULL,
    before_value   TEXT,
    after_value    TEXT,
    reason         TEXT,
    created_at     TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_admin_log_actor      ON admin_action_logs (actor_user_id);
CREATE INDEX idx_admin_log_target     ON admin_action_logs (target_type, target_id);
CREATE INDEX idx_admin_log_created    ON admin_action_logs (created_at DESC);
