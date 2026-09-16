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

## Phase 2 schema

Migration `V2__career_catalog.sql` adds:

- `careers` — controlled career identity, descriptions, expectations, entry
  difficulty, reality guidance, study-time suggestion, and catalog labels
- `career_skills` — required/preferred links to the shared `skills` table with
  importance from 1 to 5
- `career_responsibilities`, `career_job_titles`, `career_risks`, and
  `career_market_considerations` — normalized ordered-independent facts
- `career_interest_signals` and `career_goal_signals` — deterministic matching
  vocabulary, not embedding or model-generated evidence

The migration seeds ten reference paths and the shared skills they need. Every
career is marked `demo_data=true`; its market considerations are research
questions and are never interpreted as current demand, salary, or hiring proof.

## Hackathon Phase 1 schema

`V3__skill_dependencies.sql` adds `skill_dependencies`, linking a target skill
to a prerequisite with importance from 1 to 5. All modeled prerequisites are
required; importance does not make an edge optional. Foreign keys, unique pairs,
a non-self constraint and an importance check protect the data. The unique-pair
index supports target lookup; a reverse index supports prerequisite lookup.

The migration adds three foundations only when their normalized names do not
already exist, then resolves all seeded edges by normalized name. Existing
student-created skills remain canonical, including their original UUIDs.

Thirteen illustrative edges cover Spring, relational database integration, web
UI and data/ML foundations. They are `DEMO DATA`, not a complete curriculum or
market evidence. The application validates acyclicity at startup and on graph
reads. Future write operations must validate the entire proposed graph before
committing; SQL constraints alone do not detect multi-edge cycles.

## Hackathon Phase 3 schema

`V4__roadmaps.sql` adds four tables without changing V1 through V3:

- `roadmaps`: owner and career foreign keys, snapshot title/career name/weekly
  hours, generation and decision versions, profile timestamp, previous-plan link,
  optimistic revision and audit times. Owner/creation index supports current lookup.
- `roadmap_phases`: ordered stages with unique positions within each roadmap.
- `roadmap_tasks`: canonical skill link plus snapshot name, target, priority,
  explanation, effort and state; unique positions within each phase.
- `roadmap_task_prerequisites`: task-to-task links with satisfaction at generation.
  Composite uniqueness, foreign keys and a non-self constraint protect links.

State, proficiency, priority and effort checks complement request validation.
Unfinished tasks require positive effort; all estimates are bounded to 168 hours.
JPA aggregate writes touch the parent so child-only changes also increment its
revision. Failed batches and concurrent conflicts roll back the transaction.
Reads always resolve the authenticated owner. Skill/profile records are not
updated when a roadmap task is completed. See [policy](../roadmap/GENERATION.md).

## Planned schema growth

Later migrations add market observations/snapshots with provenance and freshness;
jobs and job skills; projects, check-ins and adaptive roadmap revisions; mentor
conversations; learning resources; and vectorized evidence chunks. Vector columns
supplement relational ownership and filtering; they never replace those controls.

Synthetic development records must carry a `DEMO DATA` label and are never
counted as live market evidence.
