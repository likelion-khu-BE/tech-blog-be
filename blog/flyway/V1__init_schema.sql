-- =============================================================================
-- Blog Module — Initial Schema
-- Version  : V1
-- Date     : 2026-04-11
-- Description: 블로그 모듈 전체 테이블 초기 생성.
--              모든 FK에 ON DELETE CASCADE 적용:
--                posts 삭제 → post_tags, post_likes, post_bookmarks, comments 자동 삭제
--                comments 삭제 → comment_likes, 대댓글(parent_id) 자동 삭제
-- =============================================================================

-- -----------------------------------------------------------------------------
-- posts
-- -----------------------------------------------------------------------------
CREATE TABLE posts (
    id             BIGSERIAL     PRIMARY KEY,
    user_id        UUID          NOT NULL,
    title          VARCHAR(255)  NOT NULL,
    content        TEXT          NOT NULL,
    board          VARCHAR(20)   NOT NULL,
    category       VARCHAR(20)   NOT NULL,
    status         VARCHAR(20)   NOT NULL,       -- 'PUBLISHED' | 'DRAFT'
    generation     VARCHAR(10)   NOT NULL,
    repost_from_id BIGINT        REFERENCES posts(id) ON DELETE SET NULL,
    created_at     TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_posts_user_id   ON posts(user_id);
CREATE INDEX idx_posts_status    ON posts(status);
CREATE INDEX idx_posts_board     ON posts(board);
CREATE INDEX idx_posts_generation ON posts(generation);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

-- -----------------------------------------------------------------------------
-- post_tags  (복합 PK: post_id + tag_name)
-- -----------------------------------------------------------------------------
CREATE TABLE post_tags (
    post_id  BIGINT       NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    tag_name VARCHAR(50)  NOT NULL,
    PRIMARY KEY (post_id, tag_name)
);

-- -----------------------------------------------------------------------------
-- post_likes  (복합 PK: post_id + user_id)
-- -----------------------------------------------------------------------------
CREATE TABLE post_likes (
    post_id  BIGINT  NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id  UUID    NOT NULL,
    PRIMARY KEY (post_id, user_id)
);

-- -----------------------------------------------------------------------------
-- post_bookmarks  (복합 PK: post_id + user_id)
-- -----------------------------------------------------------------------------
CREATE TABLE post_bookmarks (
    post_id  BIGINT  NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id  UUID    NOT NULL,
    PRIMARY KEY (post_id, user_id)
);

-- -----------------------------------------------------------------------------
-- comments
--   * parent_id 자기참조: 부모 댓글 삭제 시 대댓글도 CASCADE 삭제
--   * post_id 에는 DB FK 제약 없음 (Post 엔티티에 @ManyToOne 없어서 JPA 미관리)
--     → posts 삭제 시 댓글 삭제는 애플리케이션/서비스 레이어 처리 (또는 아래 FK 추가 가능)
-- -----------------------------------------------------------------------------
CREATE TABLE comments (
    id         BIGSERIAL   PRIMARY KEY,
    post_id    BIGINT      NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL,
    parent_id  BIGINT      REFERENCES comments(id) ON DELETE CASCADE,
    content    TEXT        NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE INDEX idx_comments_post_id    ON comments(post_id);
CREATE INDEX idx_comments_parent_id  ON comments(parent_id);
CREATE INDEX idx_comments_created_at ON comments(created_at ASC);

-- -----------------------------------------------------------------------------
-- comment_likes  (복합 PK: comment_id + user_id)
-- -----------------------------------------------------------------------------
CREATE TABLE comment_likes (
    comment_id  BIGINT  NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    user_id     UUID    NOT NULL,
    PRIMARY KEY (comment_id, user_id)
);
