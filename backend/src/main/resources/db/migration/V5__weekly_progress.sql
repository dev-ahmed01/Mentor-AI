CREATE TABLE weekly_plans (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    roadmap_id UUID NOT NULL,
    roadmap_title VARCHAR(200) NOT NULL,
    week_start DATE NOT NULL,
    capacity_hours INTEGER NOT NULL,
    planned_hours INTEGER NOT NULL,
    roadmap_revision BIGINT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_weekly_plans_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_weekly_plans_roadmap FOREIGN KEY (roadmap_id) REFERENCES roadmaps (id) ON DELETE CASCADE,
    CONSTRAINT uk_weekly_plans_user_week UNIQUE (user_id, week_start),
    CONSTRAINT ck_weekly_plans_capacity CHECK (capacity_hours BETWEEN 0 AND 168),
    CONSTRAINT ck_weekly_plans_planned CHECK (planned_hours BETWEEN 0 AND capacity_hours),
    CONSTRAINT ck_weekly_plans_revision CHECK (roadmap_revision >= 0)
);

CREATE TABLE weekly_plan_tasks (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL,
    task_id UUID NOT NULL,
    position INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    planned_hours INTEGER NOT NULL,
    CONSTRAINT fk_weekly_plan_tasks_plan FOREIGN KEY (plan_id) REFERENCES weekly_plans (id) ON DELETE CASCADE,
    CONSTRAINT fk_weekly_plan_tasks_task FOREIGN KEY (task_id) REFERENCES roadmap_tasks (id),
    CONSTRAINT uk_weekly_plan_task_position UNIQUE (plan_id, position),
    CONSTRAINT uk_weekly_plan_task UNIQUE (plan_id, task_id),
    CONSTRAINT ck_weekly_plan_task_hours CHECK (planned_hours BETWEEN 1 AND 168),
    CONSTRAINT ck_weekly_plan_task_position CHECK (position >= 0)
);

CREATE TABLE weekly_check_ins (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL,
    user_id UUID NOT NULL,
    roadmap_id UUID NOT NULL,
    actual_hours INTEGER NOT NULL,
    available_hours_next_week INTEGER NOT NULL,
    difficulty_rating INTEGER,
    confidence_rating INTEGER,
    energy_band VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    next_plan_id UUID NOT NULL,
    explanation VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_weekly_check_ins_plan UNIQUE (plan_id),
    CONSTRAINT fk_weekly_check_ins_plan FOREIGN KEY (plan_id) REFERENCES weekly_plans (id),
    CONSTRAINT fk_weekly_check_ins_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_weekly_check_ins_roadmap FOREIGN KEY (roadmap_id) REFERENCES roadmaps (id),
    CONSTRAINT fk_weekly_check_ins_next_plan FOREIGN KEY (next_plan_id) REFERENCES weekly_plans (id),
    CONSTRAINT ck_weekly_check_ins_actual CHECK (actual_hours BETWEEN 0 AND 168),
    CONSTRAINT ck_weekly_check_ins_next_hours CHECK (available_hours_next_week BETWEEN 0 AND 168),
    CONSTRAINT ck_weekly_check_ins_difficulty CHECK (difficulty_rating IS NULL OR difficulty_rating BETWEEN 1 AND 5),
    CONSTRAINT ck_weekly_check_ins_confidence CHECK (confidence_rating IS NULL OR confidence_rating BETWEEN 1 AND 5),
    CONSTRAINT ck_weekly_check_ins_energy CHECK (energy_band IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE TABLE weekly_task_progress (
    id UUID PRIMARY KEY,
    check_in_id UUID NOT NULL,
    task_id UUID NOT NULL,
    position INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    outcome VARCHAR(20) NOT NULL,
    CONSTRAINT fk_weekly_task_progress_check_in FOREIGN KEY (check_in_id) REFERENCES weekly_check_ins (id) ON DELETE CASCADE,
    CONSTRAINT fk_weekly_task_progress_task FOREIGN KEY (task_id) REFERENCES roadmap_tasks (id),
    CONSTRAINT uk_weekly_task_progress_position UNIQUE (check_in_id, position),
    CONSTRAINT uk_weekly_task_progress_task UNIQUE (check_in_id, task_id),
    CONSTRAINT ck_weekly_task_progress_position CHECK (position >= 0),
    CONSTRAINT ck_weekly_task_progress_outcome CHECK (outcome IN ('COMPLETED', 'PARTIAL', 'MISSED', 'DEFERRED'))
);

CREATE TABLE weekly_check_in_blockers (
    check_in_id UUID NOT NULL,
    position INTEGER NOT NULL,
    blocker VARCHAR(40) NOT NULL,
    PRIMARY KEY (check_in_id, position),
    CONSTRAINT fk_weekly_check_in_blockers FOREIGN KEY (check_in_id) REFERENCES weekly_check_ins (id) ON DELETE CASCADE,
    CONSTRAINT ck_weekly_check_in_blocker_position CHECK (position >= 0),
    CONSTRAINT ck_weekly_check_in_blocker CHECK (blocker IN ('NO_TIME', 'TOO_DIFFICULT', 'UNCLEAR_NEXT_STEP', 'RESOURCE_ACCESS', 'OTHER'))
);

CREATE TABLE weekly_temporary_constraints (
    check_in_id UUID PRIMARY KEY,
    constraint_type VARCHAR(40) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT fk_weekly_temporary_constraints FOREIGN KEY (check_in_id) REFERENCES weekly_check_ins (id) ON DELETE CASCADE,
    CONSTRAINT ck_weekly_constraint_dates CHECK (end_date >= start_date),
    CONSTRAINT ck_weekly_constraint_type CHECK (constraint_type IN ('EXAMS', 'ASSIGNMENTS', 'INTERNSHIP', 'HEALTH_OR_PERSONAL', 'TRAVEL', 'PLACEMENT_PREP', 'OTHER'))
);

CREATE INDEX idx_weekly_plans_user_week ON weekly_plans (user_id, week_start DESC);
CREATE INDEX idx_weekly_check_ins_user ON weekly_check_ins (user_id);
