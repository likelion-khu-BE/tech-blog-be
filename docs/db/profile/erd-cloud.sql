-- 1. 멤버 테이블 (프로필)
CREATE TABLE member
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL UNIQUE,
    name              VARCHAR(255) NOT NULL,
    department        VARCHAR(255),
    session_type      ENUM ('backend','frontend','design','ai','pm','etc') NOT NULL,
    profile_image_url TEXT,
    github_url        TEXT,
    displayed_email   TEXT,
    intro             TEXT,
    links_json        JSON,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 2. 기수 테이블
CREATE TABLE generation
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    label      VARCHAR(255) NOT NULL,
    number     INT          NOT NULL UNIQUE,
    start_date DATE         NOT NULL,
    end_date   DATE,
    is_current BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 3. 멤버-기수 연결 테이블
CREATE TABLE member_generation
(
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    member_id     BIGINT   NOT NULL,
    generation_id BIGINT   NOT NULL,
    role_in_gen   ENUM ('member','operating') NOT NULL DEFAULT 'member',
    joined_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    FOREIGN KEY (generation_id) REFERENCES generation (id) ON DELETE CASCADE
);

-- 4. 공통 기술 스택 마스터 테이블
CREATE TABLE tech_stack
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL UNIQUE,
    category   ENUM ('language','framework','ai','design','tool','infra','etc') NOT NULL,
    logo_url   TEXT,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 5. 멤버별 보유 기술 스택 (숙련도 포함)
CREATE TABLE member_tech_stack
(
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    member_id     BIGINT   NOT NULL,
    tech_stack_id BIGINT   NOT NULL,
    proficiency   INT,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_member_tech_stack (member_id, tech_stack_id),
    FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    FOREIGN KEY (tech_stack_id) REFERENCES tech_stack (id) ON DELETE CASCADE
);

-- 6. 팀 프로필 테이블
CREATE TABLE team_profile
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    generation_id BIGINT,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    project_url   TEXT,
    github_url    TEXT,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (generation_id) REFERENCES generation (id) ON DELETE SET NULL
);

-- 7. 팀별 사용 기술 스택
CREATE TABLE team_tech_stack
(
    id            BIGINT NOT NULL AUTO_INCREMENT,
    team_id       BIGINT NOT NULL,
    tech_stack_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_team_tech_stack (team_id, tech_stack_id),
    FOREIGN KEY (team_id) REFERENCES team_profile (id) ON DELETE CASCADE,
    FOREIGN KEY (tech_stack_id) REFERENCES tech_stack (id) ON DELETE CASCADE
);

-- 8. 팀-멤버 연결 테이블
CREATE TABLE team_member
(
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    team_id    BIGINT   NOT NULL,
    member_id  BIGINT   NOT NULL,
    is_lead    BOOLEAN  NOT NULL DEFAULT FALSE,
    status     ENUM ('pending','accepted','rejected','left','kicked') NOT NULL DEFAULT 'pending',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (team_id) REFERENCES team_profile (id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);

-- 9. 팀 내 상세 역할 (한 명이 여러 역할 가능)
CREATE TABLE team_member_role
(
    id             BIGINT NOT NULL AUTO_INCREMENT,
    team_member_id BIGINT NOT NULL,
    role           ENUM ('backend','frontend','design','ai','pm','infra','etc') NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (team_member_id) REFERENCES team_member (id) ON DELETE CASCADE
);

-- 10. 팀 프로젝트 이미지 (0~N개)
CREATE TABLE team_image
(
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    team_id    BIGINT   NOT NULL,
    image_url  TEXT     NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (team_id) REFERENCES team_profile (id) ON DELETE CASCADE
);

-- 11. 활동 점수 및 로그 테이블
CREATE TABLE activity
(
    id             BIGINT   NOT NULL AUTO_INCREMENT,
    member_id      BIGINT   NOT NULL,
    type           ENUM ('blog_post','blog_comment','qna_answer','qna_question','qna_accepted','other') NOT NULL,
    reference_id   BIGINT,
    reference_type VARCHAR(255),
    score          INT      NOT NULL DEFAULT 0,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);
