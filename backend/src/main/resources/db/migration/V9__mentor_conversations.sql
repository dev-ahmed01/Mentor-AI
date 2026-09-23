CREATE TABLE mentor_conversations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    career_id UUID NOT NULL REFERENCES careers(id),
    career_name VARCHAR(200) NOT NULL,
    job_analysis_id UUID REFERENCES job_analyses(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    revision BIGINT NOT NULL DEFAULT 0,
    memory VARCHAR(300) NOT NULL DEFAULT '',
    CHECK (revision BETWEEN 0 AND 60)
);
CREATE INDEX idx_mentor_owner ON mentor_conversations(user_id,updated_at);
CREATE TABLE mentor_turns (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES mentor_conversations(id) ON DELETE CASCADE,
    request_id UUID NOT NULL,
    revision BIGINT NOT NULL,
    turn_json TEXT NOT NULL,
    UNIQUE(conversation_id,request_id),
    UNIQUE(conversation_id,revision)
);
