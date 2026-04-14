-- QnA 모듈 테이블 생성 마이그레이션
-- ddl-auto=validate: 이 파일이 DDL의 유일한 진실
-- member 테이블은 별도 생성 없이 공통 users 테이블을 공유한다

-- 1. tag
CREATE TABLE IF NOT EXISTS tag
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_tag_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 2. question
CREATE TABLE IF NOT EXISTS question
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    member_id    BIGINT       NOT NULL,
    title        VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    generation   INT          NOT NULL,
    view_count   INT          NOT NULL DEFAULT 0,
    answer_count INT          NOT NULL DEFAULT 0,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    deleted_at   DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_question_member
        FOREIGN KEY (member_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_question_member
    ON question (member_id);
CREATE INDEX idx_question_status_created
    ON question (status, created_at);
CREATE INDEX idx_question_generation_status_created
    ON question (generation, status, created_at);

-- 3. question_tag
CREATE TABLE IF NOT EXISTS question_tag
(
    id          BIGINT NOT NULL AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    tag_id      BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_question_tag (question_id, tag_id),
    CONSTRAINT fk_question_tag_question
        FOREIGN KEY (question_id) REFERENCES question (id),
    CONSTRAINT fk_question_tag_tag
        FOREIGN KEY (tag_id) REFERENCES tag (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_question_tag_tag
    ON question_tag (tag_id);

-- 4. answer
CREATE TABLE IF NOT EXISTS answer
(
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    question_id   BIGINT      NOT NULL,
    member_id     BIGINT      NOT NULL,
    content       TEXT        NOT NULL,
    accepted      TINYINT(1)  NOT NULL DEFAULT 0,
    vote_count    INT         NOT NULL DEFAULT 0,
    comment_count INT         NOT NULL DEFAULT 0,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    deleted_at    DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_answer_question
        FOREIGN KEY (question_id) REFERENCES question (id),
    CONSTRAINT fk_answer_member
        FOREIGN KEY (member_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_answer_question
    ON answer (question_id);
CREATE INDEX idx_answer_accepted
    ON answer (question_id, accepted);
CREATE INDEX idx_answer_question_accepted_vote
    ON answer (question_id, accepted, vote_count);
CREATE INDEX idx_answer_member
    ON answer (member_id);

-- 5. vote
CREATE TABLE IF NOT EXISTS vote
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    answer_id  BIGINT      NOT NULL,
    member_id  BIGINT      NOT NULL,
    type       VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_vote_answer_member (answer_id, member_id),
    CONSTRAINT fk_vote_answer
        FOREIGN KEY (answer_id) REFERENCES answer (id),
    CONSTRAINT fk_vote_member
        FOREIGN KEY (member_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_vote_answer_type
    ON vote (answer_id, type);

-- 6. comment
CREATE TABLE IF NOT EXISTS comment
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    member_id   BIGINT      NOT NULL,
    question_id BIGINT      NULL,
    answer_id   BIGINT      NULL,
    parent_id   BIGINT      NULL,
    content     TEXT        NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    deleted_at  DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_member
        FOREIGN KEY (member_id) REFERENCES users (id),
    CONSTRAINT fk_comment_question
        FOREIGN KEY (question_id) REFERENCES question (id),
    CONSTRAINT fk_comment_answer
        FOREIGN KEY (answer_id) REFERENCES answer (id),
    CONSTRAINT fk_comment_parent
        FOREIGN KEY (parent_id) REFERENCES comment (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_comment_question_parent
    ON comment (question_id, parent_id);
CREATE INDEX idx_comment_answer_parent
    ON comment (answer_id, parent_id);
