CREATE TABLE career_pivots (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source_roadmap_id UUID NOT NULL REFERENCES roadmaps(id),
    target_career_id UUID NOT NULL REFERENCES careers(id),
    created_at TIMESTAMPTZ NOT NULL,
    comparison_json TEXT NOT NULL,
    profile_fingerprint VARCHAR(64) NOT NULL,
    accepted_roadmap_id UUID UNIQUE REFERENCES roadmaps(id),
    accepted_at TIMESTAMPTZ,
    CHECK ((accepted_roadmap_id IS NULL AND accepted_at IS NULL) OR
           (accepted_roadmap_id IS NOT NULL AND accepted_at IS NOT NULL))
);
CREATE INDEX idx_pivots_owner ON career_pivots(user_id, created_at);

-- Separate from immutable generation facts: stronger carried proficiency and
-- later revocation must survive destination targets and manual skip changes.
CREATE TABLE roadmap_task_credits (
    task_id UUID PRIMARY KEY REFERENCES roadmap_tasks(id) ON DELETE CASCADE,
    proficiency VARCHAR(30) NOT NULL CHECK (proficiency IN ('AWARENESS','BEGINNER','INTERMEDIATE','ADVANCED')),
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);
