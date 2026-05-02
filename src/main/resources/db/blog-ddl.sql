-- ============================================================
-- Blog — PostgreSQL DDL
-- ============================================================
-- 이 파일은 기술 블로그의 게시글(blog_posts), 댓글(blog_comments),
-- 좋아요(likes), 북마크(bookmarks), 태그(tags) 테이블을 정의합니다.
-- ============================================================

-- ------------------------------------------------------------
-- BLOG_POST_STATUS ENUM
-- ------------------------------------------------------------
CREATE TYPE blog_post_status AS ENUM ('DRAFT', 'PUBLISHED');

-- ------------------------------------------------------------
-- BLOG_POSTS
-- ------------------------------------------------------------
CREATE TABLE blog_posts (
    id              BIGSERIAL         PRIMARY KEY,
    user_id         UUID              NOT NULL,
    title           VARCHAR(255)      NOT NULL,
    content         TEXT              NOT NULL,
    board           VARCHAR(20)       NOT NULL,
    category        VARCHAR(20)       NOT NULL,
    status          blog_post_status  NOT NULL,
    generation      VARCHAR(10)       NOT NULL,
    repost_from_id  BIGINT,
    created_at      TIMESTAMP(6)      NOT NULL,
    updated_at      TIMESTAMP(6)      NOT NULL
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
    user_id  UUID   NOT NULL,
    PRIMARY KEY (post_id, user_id)
);

CREATE INDEX idx_blog_post_like_user ON blog_post_likes (user_id);

-- ------------------------------------------------------------
-- BLOG_POST_BOOKMARKS
-- ------------------------------------------------------------
CREATE TABLE blog_post_bookmarks (
    post_id  BIGINT NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    user_id  UUID   NOT NULL,
    PRIMARY KEY (post_id, user_id)
);

CREATE INDEX idx_blog_post_bookmark_user ON blog_post_bookmarks (user_id);

-- ------------------------------------------------------------
-- BLOG_COMMENTS
-- ------------------------------------------------------------
CREATE TABLE blog_comments (
    id          BIGSERIAL    PRIMARY KEY,
    post_id     BIGINT       NOT NULL,
    user_id     UUID         NOT NULL,
    parent_id   BIGINT       REFERENCES blog_comments(id) ON DELETE CASCADE,
    content     TEXT         NOT NULL,
    created_at  TIMESTAMP(6) NOT NULL
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
    user_id     UUID   NOT NULL,
    PRIMARY KEY (comment_id, user_id)
);

CREATE INDEX idx_blog_comment_like_user ON blog_comment_likes (user_id);
