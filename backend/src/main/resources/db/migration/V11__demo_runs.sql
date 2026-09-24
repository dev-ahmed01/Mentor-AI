CREATE TABLE demo_runs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    roadmap_id UUID NOT NULL REFERENCES roadmaps(id),
    plan_id UUID NOT NULL REFERENCES weekly_plans(id),
    week_start DATE NOT NULL,
    roadmap_revision BIGINT NOT NULL,
    plan_revision BIGINT NOT NULL,
    scenario_version VARCHAR(60) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    exam_check_in_id UUID UNIQUE REFERENCES weekly_check_ins(id)
);
