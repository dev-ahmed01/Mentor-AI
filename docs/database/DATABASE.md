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

## Hackathon Phase 4 schema

`V5__weekly_progress.sql` adds:

- `weekly_plans`: user/roadmap links, Monday week start, saved title/capacity,
  allocated hours, source revision, reason and creation timestamp. A unique
  user/week key prevents duplicate snapshots.
- `weekly_plan_tasks`: ordered task/title/hour allocations with foreign keys and
  unique task and position keys within the plan.
- `weekly_check_ins`: unique plan link, owner, actual/next-week hours, optional
  ratings/notes, energy, next-plan link, explanation and creation timestamp.
- `weekly_task_progress`: ordered outcome records with snapshot task titles.
- `weekly_check_in_blockers`: ordered generic blocker categories.
- `weekly_temporary_constraints`: optional category/date interval per check-in.

All public IDs are UUIDs. Foreign keys and uniqueness back application ownership
and duplicate protection. Integer hour bounds and rating/date checks supplement
request validation. Service transactions cover roadmap edits, check-in rows and
the new weekly allocation, so rejected submissions cannot leave partial progress.
Prior migrations are unchanged; existing roadmaps and student data are retained.

## Planned schema growth

Hackathon Phase 5 migration `V6__roadmap_adaptations.sql` adds a version counter
and NORMAL/MAINTENANCE mode to existing weekly plans, with defaults preserving
all Phase 4 allocations. `roadmap_adaptations` links the owner, roadmap, trigger
check-in and target plan. It stores the expected revisions, policy version,
PENDING/ACCEPTED status, creation/acceptance times and immutable original,
proposed and accepted JSON snapshots. Database checks enforce status/timestamp
consistency. Before/proposed snapshots are non-updatable JPA fields. Plan child
allocations are replaced only under the owned-roadmap lock after stale and
checked-in checks; explicit flush ordering respects task/position uniqueness.

Later migrations add market observations/snapshots with provenance and freshness;
jobs and job skills; projects; mentor
conversations; learning resources; and vectorized evidence chunks. Vector columns
supplement relational ownership and filtering; they never replace those controls.

Synthetic development records must carry a `DEMO DATA` label and are never
counted as live market evidence.
## Hackathon Phase 6 data access

The opportunity simulator adds no tables or migration. Flyway remains at V6.
It reads active careers, canonical skills, dependency edges and the authenticated
profile, then calculates against copied proficiency maps. Simulation never saves
profiles, student skills, roadmaps, weekly plans, check-ins or adaptation history.
## Phase 7 — immutable market evidence (V7)

- `market_sources`: persistent attempt cooldown and last collection outcome.
- `market_observations`: source ID/URL, first collection and publication time,
  content hash, processing version, original untrusted payload and normalized JSON.
  Unique source/ID/hash prevents duplicate revisions.
- `market_snapshots`: immutable per-career aggregate JSON, collection time and
  processing version, indexed by career/time.
- `market_snapshot_observations`: explicit immutable evidence membership with FKs.
- `market_decisions`: owner-only saved snapshot/profile calculation and version;
  indexed by owner/ID and cascaded on user deletion.

V7 adds tables only. No previous migration or business row is changed. Source
state contains an uncollected source identifier, never synthetic market evidence.
Rollback is application rollback with collection disabled; keep additive tables
and Flyway history. The [policy](../market/EVIDENCE_POLICY.md) defines provenance,
aggregation, eligibility, retention limits and historical reproducibility.
## Phase 8: private job analyses (V8)

`job_analyses` stores an immutable JSON result per explicit reviewed comparison,
with UUID, owner UUID, calculation time and version. An owner/ID index supports
private retrieval; the owner FK cascades only on account deletion. The payload
freezes original pasted text and extraction, reviewed job fields, relevant profile
inputs and the full deterministic result. It is never shared market evidence.

V8 adds one table and does not rewrite any prior migration or existing rows.
No seed job listings or analyses are inserted. Application rollback can leave the
unused table intact; do not delete historical analyses to roll back code.
## Phase 9: mentor conversations (V9)

`mentor_conversations` owns the selected career, optional private job analysis,
revision and bounded topic memory. `mentor_turns` stores immutable JSON snapshots
with unique `(conversation_id, request_id)` and `(conversation_id, revision)` keys.
Conversation revision is bounded to 0–60. Account deletion cascades through owned
conversations to turns; the private job reference is validated by the service.

Message generation holds no database transaction. A short compare-and-swap update
and insert commit together, so concurrent results cannot overwrite each other.
V9 is additive and inserts no fabricated conversations. Rollback can retain both
tables while running older application code; preserve stored history.

## V10: career pivot audit

`career_pivots` adds owner, source roadmap and destination career foreign keys,
creation time, immutable comparison JSON and a SHA-256 profile fingerprint. The
comparison contains the source roadmap revision/snapshot, captured priorities,
planning credits, effort and proposed stages. Acceptance adds a unique revised
roadmap foreign key and timestamp together. An owner/time index supports history.

Acceptance atomically saves a new roadmap linked through previous_roadmap_id and
updates this audit record. Existing rows are not rewritten. Owner-row locks
serialize acceptance with profile edits and ordinary roadmap generation; the
source roadmap is additionally locked against concurrent task changes. Rollback
to previous application code can leave the additive table intact. Do not delete
historical roadmaps or rewrite applied migrations.

`roadmap_task_credits` stores carried proficiency and revocation separately from
historical task-generation flags. Accepted pivots retain the full credited level;
reopening a satisfied task revokes its retained credit transactionally with the
roadmap edit. Re-skipping cannot undo revocation. Existing rows are not backfilled
or modified by migration; legacy satisfied tasks use their recorded target as
the conservative fallback until a credit/revocation entry is created.
