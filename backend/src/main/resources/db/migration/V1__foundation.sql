CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE student_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    degree VARCHAR(120),
    study_year INTEGER,
    semester INTEGER,
    short_term_goal VARCHAR(1000),
    long_term_goal VARCHAR(1000),
    experience_summary VARCHAR(2000),
    remote_preference VARCHAR(30),
    time_available_per_week INTEGER,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_student_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_student_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_student_profiles_year CHECK (study_year IS NULL OR study_year BETWEEN 1 AND 8),
    CONSTRAINT ck_student_profiles_semester CHECK (semester IS NULL OR semester BETWEEN 1 AND 16),
    CONSTRAINT ck_student_profiles_hours CHECK (
        time_available_per_week IS NULL OR time_available_per_week BETWEEN 1 AND 168
    )
);

CREATE TABLE skills (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,
    category VARCHAR(60) NOT NULL,
    description VARCHAR(1000),
    CONSTRAINT uk_skills_normalized_name UNIQUE (normalized_name)
);

CREATE TABLE student_skills (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    skill_id UUID NOT NULL,
    proficiency VARCHAR(30) NOT NULL,
    confidence VARCHAR(30) NOT NULL,
    source VARCHAR(30) NOT NULL,
    CONSTRAINT fk_student_skills_profile FOREIGN KEY (profile_id)
        REFERENCES student_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_student_skills_skill FOREIGN KEY (skill_id)
        REFERENCES skills (id),
    CONSTRAINT uk_student_skill_profile_skill UNIQUE (profile_id, skill_id)
);

CREATE INDEX idx_student_skills_profile ON student_skills (profile_id);
CREATE INDEX idx_student_skills_skill ON student_skills (skill_id);

CREATE TABLE profile_interests (
    profile_id UUID NOT NULL REFERENCES student_profiles (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    interest VARCHAR(120) NOT NULL,
    PRIMARY KEY (profile_id, position)
);

CREATE TABLE profile_goals (
    profile_id UUID NOT NULL REFERENCES student_profiles (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    goal VARCHAR(300) NOT NULL,
    PRIMARY KEY (profile_id, position)
);

CREATE TABLE profile_programming_languages (
    profile_id UUID NOT NULL REFERENCES student_profiles (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    programming_language VARCHAR(100) NOT NULL,
    PRIMARY KEY (profile_id, position)
);

CREATE TABLE profile_preferred_domains (
    profile_id UUID NOT NULL REFERENCES student_profiles (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    preferred_domain VARCHAR(120) NOT NULL,
    PRIMARY KEY (profile_id, position)
);

CREATE TABLE profile_target_locations (
    profile_id UUID NOT NULL REFERENCES student_profiles (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    target_location VARCHAR(120) NOT NULL,
    PRIMARY KEY (profile_id, position)
);

CREATE TABLE profile_projects (
    profile_id UUID NOT NULL REFERENCES student_profiles (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    project VARCHAR(300) NOT NULL,
    PRIMARY KEY (profile_id, position)
);

CREATE TABLE profile_certifications (
    profile_id UUID NOT NULL REFERENCES student_profiles (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    certification VARCHAR(200) NOT NULL,
    PRIMARY KEY (profile_id, position)
);

CREATE TABLE profile_avoidances (
    profile_id UUID NOT NULL REFERENCES student_profiles (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    avoidance VARCHAR(200) NOT NULL,
    PRIMARY KEY (profile_id, position)
);
