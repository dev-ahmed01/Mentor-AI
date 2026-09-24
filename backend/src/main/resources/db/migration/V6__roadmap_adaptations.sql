ALTER TABLE weekly_plans ADD COLUMN revision BIGINT NOT NULL DEFAULT 0;
ALTER TABLE weekly_plans ADD COLUMN mode VARCHAR(20) NOT NULL DEFAULT 'NORMAL';
ALTER TABLE weekly_plans ADD CONSTRAINT ck_weekly_plan_mode CHECK (mode IN ('NORMAL', 'MAINTENANCE'));
ALTER TABLE weekly_plans ADD CONSTRAINT ck_weekly_plan_edit_revision CHECK (revision >= 0);

CREATE TABLE roadmap_adaptations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    roadmap_id UUID NOT NULL REFERENCES roadmaps(id),
    check_in_id UUID NOT NULL UNIQUE REFERENCES weekly_check_ins(id),
    plan_id UUID NOT NULL REFERENCES weekly_plans(id),
    roadmap_revision BIGINT NOT NULL CHECK (roadmap_revision >= 0),
    plan_revision BIGINT NOT NULL CHECK (plan_revision >= 0),
    policy_version VARCHAR(60) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED')),
    before_json TEXT NOT NULL,
    decision_json TEXT NOT NULL,
    accepted_json TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ,
    CONSTRAINT ck_adaptation_acceptance CHECK (
        (status = 'PENDING' AND accepted_json IS NULL AND accepted_at IS NULL) OR
        (status = 'ACCEPTED' AND accepted_json IS NOT NULL AND accepted_at IS NOT NULL)
    )
);
CREATE INDEX idx_adaptation_roadmap_owner ON roadmap_adaptations (roadmap_id, user_id, created_at DESC);
