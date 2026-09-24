CREATE TABLE roadmaps (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    career_id UUID NOT NULL REFERENCES careers(id),
    career_name VARCHAR(160) NOT NULL,
    title VARCHAR(200) NOT NULL,
    weekly_hours INTEGER NOT NULL CHECK (weekly_hours BETWEEN 1 AND 168),
    generation_version VARCHAR(60) NOT NULL,
    decision_version VARCHAR(60) NOT NULL,
    profile_updated_at TIMESTAMPTZ NOT NULL,
    previous_roadmap_id UUID REFERENCES roadmaps(id),
    revision BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_roadmaps_user_created ON roadmaps(user_id, created_at DESC, id DESC);

CREATE TABLE roadmap_phases (
    id UUID PRIMARY KEY,
    roadmap_id UUID NOT NULL REFERENCES roadmaps(id),
    position INTEGER NOT NULL CHECK (position >= 0),
    title VARCHAR(200) NOT NULL,
    CONSTRAINT uk_roadmap_phase_position UNIQUE (roadmap_id, position)
);

CREATE TABLE roadmap_tasks (
    id UUID PRIMARY KEY,
    phase_id UUID NOT NULL REFERENCES roadmap_phases(id),
    skill_id UUID NOT NULL REFERENCES skills(id),
    skill_name VARCHAR(120) NOT NULL,
    position INTEGER NOT NULL CHECK (position >= 0),
    title VARCHAR(200) NOT NULL,
    state VARCHAR(30) NOT NULL CHECK (state IN ('NOT_STARTED','IN_PROGRESS','COMPLETED','SKIPPED','NEEDS_REVIEW')),
    target_proficiency VARCHAR(30) NOT NULL CHECK (target_proficiency IN ('BEGINNER','INTERMEDIATE')),
    estimated_hours INTEGER NOT NULL CHECK (estimated_hours BETWEEN 0 AND 168),
    satisfied_at_generation BOOLEAN NOT NULL,
    initial_priority VARCHAR(30) NOT NULL,
    priority_points INTEGER NOT NULL CHECK (priority_points BETWEEN 0 AND 100),
    ordering_reason VARCHAR(1000) NOT NULL,
    CONSTRAINT uk_roadmap_task_position UNIQUE (phase_id, position),
    CONSTRAINT ck_roadmap_active_effort CHECK (state IN ('COMPLETED','SKIPPED') OR estimated_hours > 0)
);
CREATE INDEX idx_roadmap_tasks_skill ON roadmap_tasks(skill_id);

CREATE TABLE roadmap_task_prerequisites (
    task_id UUID NOT NULL REFERENCES roadmap_tasks(id),
    prerequisite_task_id UUID NOT NULL REFERENCES roadmap_tasks(id),
    snapshot_satisfied BOOLEAN NOT NULL,
    PRIMARY KEY (task_id, prerequisite_task_id),
    CONSTRAINT ck_roadmap_task_not_self CHECK (task_id <> prerequisite_task_id)
);
