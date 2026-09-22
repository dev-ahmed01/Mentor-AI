CREATE TABLE market_sources (
    source VARCHAR(40) PRIMARY KEY,
    last_attempt_at TIMESTAMPTZ,
    last_success_at TIMESTAMPTZ,
    last_status VARCHAR(40) NOT NULL
);
INSERT INTO market_sources (source,last_status) VALUES ('ARBEITNOW','NEVER_COLLECTED');

CREATE TABLE market_observations (
    id UUID PRIMARY KEY,
    source VARCHAR(40) NOT NULL REFERENCES market_sources(source),
    source_id VARCHAR(300) NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    source_url VARCHAR(1500) NOT NULL,
    collected_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ NOT NULL,
    processing_version VARCHAR(60) NOT NULL,
    raw_payload TEXT NOT NULL,
    normalized_json TEXT NOT NULL,
    CONSTRAINT uk_market_observation UNIQUE(source,source_id,content_hash)
);
CREATE TABLE market_snapshots (
    id UUID PRIMARY KEY,
    career_id UUID NOT NULL REFERENCES careers(id),
    collected_at TIMESTAMPTZ NOT NULL,
    processing_version VARCHAR(60) NOT NULL,
    snapshot_json TEXT NOT NULL
);
CREATE INDEX idx_market_snapshot_latest ON market_snapshots(career_id,collected_at);
CREATE TABLE market_snapshot_observations (
    snapshot_id UUID NOT NULL REFERENCES market_snapshots(id),
    observation_id UUID NOT NULL REFERENCES market_observations(id),
    PRIMARY KEY(snapshot_id,observation_id)
);
CREATE TABLE market_decisions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    snapshot_id UUID NOT NULL REFERENCES market_snapshots(id),
    calculated_at TIMESTAMPTZ NOT NULL,
    calculation_version VARCHAR(60) NOT NULL,
    decision_json TEXT NOT NULL
);
CREATE INDEX idx_market_decision_owner ON market_decisions(user_id,id);
