-- 1. ENUM 타입 설정 (DB 레벨의 예외 방지)
CREATE TYPE session_type AS ENUM ('backend', 'frontend', 'design', 'ai', 'pm', 'etc');
CREATE TYPE generation_role AS ENUM ('member', 'operating');
CREATE TYPE tech_stack_category AS ENUM ('language', 'framework', 'ai', 'design', 'tool', 'infra', 'etc');
CREATE TYPE activity_type AS ENUM ('blog_post', 'blog_comment', 'qna_answer', 'qna_question', 'qna_accepted', 'other');
CREATE TYPE contribution_period_type AS ENUM ('month', 'three_month', 'year', 'all');
CREATE TYPE role_in_team AS ENUM ('backend', 'frontend', 'design', 'ai', 'pm', 'infra', 'etc');

-- 2. 핵심 테이블 생성 (PK: BIGINT / Long)
CREATE TABLE member (
                        id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        department TEXT,
                        session_type session_type NOT NULL,
                        profile_image_url TEXT,
                        github_url TEXT,
                        links_json JSONB,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE generation (
                            id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                            label TEXT NOT NULL,
                            number INT NOT NULL UNIQUE,
                            start_date DATE NOT NULL,
                            end_date DATE,
                            is_current BOOLEAN NOT NULL DEFAULT FALSE,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. 관계 및 활동 테이블
CREATE TABLE member_generation (
                                   id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                   member_id BIGINT REFERENCES member(id) ON DELETE CASCADE,
                                   generation_id BIGINT REFERENCES generation(id) ON DELETE CASCADE,
                                   role_in_gen generation_role NOT NULL DEFAULT 'member',
                                   joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tech_stack (
                            id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                            name TEXT NOT NULL UNIQUE,
                            category tech_stack_category NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE member_tech_stack (
                                   id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                   member_id BIGINT REFERENCES member(id) ON DELETE CASCADE,
                                   tech_stack_id BIGINT REFERENCES tech_stack(id) ON DELETE CASCADE,
                                   proficiency INT,
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                   CONSTRAINT uq_member_tech_stack UNIQUE (member_id, tech_stack_id)
);

CREATE TABLE team_profile (
                              id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                              generation_id BIGINT REFERENCES generation(id) ON DELETE SET NULL,
                              name TEXT NOT NULL,
                              description TEXT,
                              project_url TEXT,
                              github_url TEXT,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE team_member (
                             id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                             team_id BIGINT REFERENCES team_profile(id) ON DELETE CASCADE,
                             member_id BIGINT REFERENCES member(id) ON DELETE CASCADE,
                             is_lead BOOLEAN NOT NULL DEFAULT FALSE,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE team_member_role (
                            id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                            team_member_id BIGINT REFERENCES team_member(id) ON DELETE CASCADE,
                            role role_in_team NOT NULL
);

CREATE TABLE team_image (
                            id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                            team_id BIGINT REFERENCES team_profile(id) ON DELETE CASCADE,
                            image_url TEXT NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE activity (
                          id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                          member_id BIGINT REFERENCES member(id) ON DELETE CASCADE,
                          type activity_type NOT NULL,
                          reference_id BIGINT,
                          reference_type TEXT,
                          score INT NOT NULL DEFAULT 0,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);