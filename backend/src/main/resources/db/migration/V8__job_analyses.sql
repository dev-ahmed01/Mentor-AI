CREATE TABLE job_analyses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    calculated_at TIMESTAMPTZ NOT NULL,
    calculation_version VARCHAR(60) NOT NULL,
    analysis_json TEXT NOT NULL
);
CREATE INDEX idx_job_analysis_owner ON job_analyses(user_id,id);
