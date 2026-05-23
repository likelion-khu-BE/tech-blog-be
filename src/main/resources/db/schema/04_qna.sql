CREATE TABLE IF NOT EXISTS tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS question (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    generation INT NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    answer_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS question_tag (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    CONSTRAINT uq_question_tag UNIQUE (question_id, tag_id),
    CONSTRAINT fk_question_tag_question FOREIGN KEY (question_id) REFERENCES question(id),
    CONSTRAINT fk_question_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id)
);

CREATE TABLE IF NOT EXISTS answer (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    accepted BOOLEAN NOT NULL DEFAULT false,
    vote_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES question(id)
);

CREATE TABLE IF NOT EXISTS vote (
    id BIGSERIAL PRIMARY KEY,
    answer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uq_vote_answer_user UNIQUE (answer_id, user_id),
    CONSTRAINT fk_vote_answer FOREIGN KEY (answer_id) REFERENCES answer(id)
);

CREATE TABLE IF NOT EXISTS comment (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT,
    answer_id BIGINT,
    content TEXT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    CONSTRAINT fk_comment_question FOREIGN KEY (question_id) REFERENCES question(id),
    CONSTRAINT fk_comment_answer FOREIGN KEY (answer_id) REFERENCES answer(id)
);

CREATE INDEX IF NOT EXISTS idx_question_user ON question (user_id);
CREATE INDEX IF NOT EXISTS idx_question_status_created ON question (status, created_at);
CREATE INDEX IF NOT EXISTS idx_question_generation_status_created ON question (generation, status, created_at);

CREATE INDEX IF NOT EXISTS idx_question_tag_tag ON question_tag (tag_id);

CREATE INDEX IF NOT EXISTS idx_answer_question ON answer (question_id);
CREATE INDEX IF NOT EXISTS idx_answer_accepted ON answer (question_id, accepted);
CREATE INDEX IF NOT EXISTS idx_answer_question_accepted_vote ON answer (question_id, accepted, vote_count);
CREATE INDEX IF NOT EXISTS idx_answer_user ON answer (user_id);

CREATE INDEX IF NOT EXISTS idx_vote_answer_type ON vote (answer_id, type);

CREATE INDEX IF NOT EXISTS idx_comment_question ON comment (question_id);
CREATE INDEX IF NOT EXISTS idx_comment_answer ON comment (answer_id);
