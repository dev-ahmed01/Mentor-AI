# Database

PostgreSQL is the production datastore. The Compose image includes pgvector so
semantic retrieval can be added without a separate database. Flyway migrations
under `backend/src/main/resources/db/migration` are authoritative; production
Hibernate uses `ddl-auto=validate`.

## Phase 1 schema

- `users` — identity, normalized email, password hash, role, enabled state, audit
  times
- `student_profiles` — one-to-one user-owned profile and scalar preferences
- `skills` — shared canonical skill identity and category
- `student_skills` — profile-to-skill relation with proficiency, confidence, and
  source
- `profile_*` tables — ordered normalized collections for interests, goals,
  languages, domains, locations, projects, certifications, and avoidances

UUIDs are used for public-safe identifiers. Foreign keys cascade from a user to
their private profile data. The shared skill record is retained when one user is
deleted because other careers, jobs, or students may reference it. Check and
unique constraints complement API validation.

## Planned schema growth

Later migrations add careers and career skills; market observations/snapshots
with provenance and freshness; jobs and job skills; roadmaps, phases, tasks,
projects, and progress; mentor conversations; learning resources; and vectorized
evidence chunks. Vector columns supplement relational ownership and filtering;
they never replace those controls.

Synthetic development records must carry a `DEMO DATA` label and are never
counted as live market evidence.
