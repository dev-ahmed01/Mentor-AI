CREATE TABLE skill_dependencies (
    id UUID PRIMARY KEY,
    skill_id UUID NOT NULL REFERENCES skills (id),
    prerequisite_skill_id UUID NOT NULL REFERENCES skills (id),
    importance INTEGER NOT NULL,
    CONSTRAINT uk_skill_dependency_pair UNIQUE (skill_id, prerequisite_skill_id),
    CONSTRAINT ck_skill_dependency_not_self CHECK (skill_id <> prerequisite_skill_id),
    CONSTRAINT ck_skill_dependency_importance CHECK (importance BETWEEN 1 AND 5)
);
CREATE INDEX idx_skill_dependency_prerequisite ON skill_dependencies (prerequisite_skill_id);

-- DEMO DATA: illustrative prerequisite guidance, not a complete curriculum or market evidence.
-- Resolve all identities by normalized name so existing user-created skills remain canonical.
INSERT INTO skills SELECT '30000000-0000-0000-0000-000000000001', 'Spring Fundamentals', 'spring fundamentals', 'Backend', 'Dependency injection and Spring application fundamentals' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'spring fundamentals');
INSERT INTO skills SELECT '30000000-0000-0000-0000-000000000002', 'Database Integration', 'database integration', 'Data', 'Connecting applications to relational data stores' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'database integration');
INSERT INTO skills SELECT '30000000-0000-0000-0000-000000000003', 'Data Handling', 'data handling', 'Data', 'Loading, cleaning, and validating data with Python' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'data handling');

INSERT INTO skill_dependencies (id, skill_id, prerequisite_skill_id, importance)
SELECT CAST(seed.id AS UUID), target.id, prerequisite.id, seed.importance
FROM (VALUES
    ('40000000-0000-0000-0000-000000000001', 'spring fundamentals', 'java', 5),
    ('40000000-0000-0000-0000-000000000002', 'spring boot', 'spring fundamentals', 5),
    ('40000000-0000-0000-0000-000000000003', 'database integration', 'sql', 5),
    ('40000000-0000-0000-0000-000000000004', 'react', 'javascript', 5),
    ('40000000-0000-0000-0000-000000000005', 'react', 'html', 5),
    ('40000000-0000-0000-0000-000000000006', 'react', 'css', 4),
    ('40000000-0000-0000-0000-000000000007', 'typescript', 'javascript', 5),
    ('40000000-0000-0000-0000-000000000008', 'node.js', 'javascript', 5),
    ('40000000-0000-0000-0000-000000000009', 'data handling', 'python', 5),
    ('40000000-0000-0000-0000-00000000000a', 'machine learning', 'data handling', 5),
    ('40000000-0000-0000-0000-00000000000b', 'machine learning', 'statistics', 5),
    ('40000000-0000-0000-0000-00000000000c', 'machine learning', 'linear algebra', 4),
    ('40000000-0000-0000-0000-00000000000d', 'deep learning', 'machine learning', 5)
) AS seed(id, skill_name, prerequisite_name, importance)
JOIN skills target ON target.normalized_name = seed.skill_name
JOIN skills prerequisite ON prerequisite.normalized_name = seed.prerequisite_name;
