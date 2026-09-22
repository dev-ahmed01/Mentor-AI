# Architecture

## Shape

MentorAI is a modular monolith. The Next.js application renders the product UI
and delegates domain operations to a single Spring Boot API. PostgreSQL is the
system of record and will also host pgvector. Ollama is a replaceable local AI
provider behind Spring AI service interfaces.

Backend packages are organized by domain (`auth`, `profile`, `skills`, `career`,
`decision`, `roadmap`, `progress`, and later `market`, `jobs`, `mentor`, `ai`) with shared
configuration, errors, and security under `common`. Controllers accept DTOs,
application services own transactions and business rules, repositories only
handle persistence, and external providers sit behind ports.

## Phase 1 request flow

```text
Browser form
  -> Next.js Server Action
  -> typed centralized API client
  -> Spring Security bearer authentication
  -> validated controller DTO
  -> ownership-aware service
  -> JPA repository
  -> PostgreSQL
```

The backend issues a signed JWT after password authentication. The Next.js
server stores that token in an HttpOnly, Secure-in-production, SameSite=Lax
cookie. Client JavaScript cannot read it. Server Components and Actions exchange
it for backend responses; every private backend route independently verifies it.

## Reliability and trust boundaries

- The browser is untrusted; frontend visibility is never authorization.
- User inputs are validated at the REST boundary and constrained in PostgreSQL.
- Request IDs correlate safe errors and logs; credentials and tokens are not
  intentionally logged.
- Missing AI does not affect auth/profile features.
- Missing future market data produces an explicit unavailable/stale state, never
  synthetic current-looking data.
- Retrieved documents and job descriptions will be isolated as untrusted prompt
  content before AI integration.

## Phase 2 career flow

```text
Authenticated profile + controlled career catalog
  -> deterministic factor calculations
  -> normalization across available profile evidence
  -> ranked candidates + gaps + alternatives + uncertainties
  -> Next.js career explorer and reality pages
```

Career scoring is a pure, versioned service. It has no model or network
dependency. Phase 2 reserves the configured market weight but excludes it from
the numerator and normalization because no validated observations exist. Future
market or AI modules must cross explicit service boundaries and cannot silently
change `career-fit-v1` results.

## Skill dependency flow (hackathon Phase 1)

```text
Shared skills + illustrative prerequisite edges
  -> startup DAG validation
  -> direct/transitive prerequisite retrieval
  -> authenticated profile proficiency comparison
  -> career skill context (native expandable details)
```

`SkillDependencyGraph` performs cycle detection and deduplicated ancestor
traversal without database, profile, model, or network dependencies.
`SkillDependencyService` loads a small graph once per request, resolves the
authenticated profile through the existing service, and emits typed responses.
The career batch avoids one frontend request per displayed skill. No graph or
readiness result is stored in a cross-user cache.

All required ancestors must be recorded at BEGINNER or above. Absent coverage is
explicit rather than treated as proof of preparation. Existing `career-fit-v1`
calculations and career response contracts are unchanged. AI and learning-priority
ranking are not part of this phase.

## Learning decision flow (hackathon Phase 2)

```text
Explicit career selection + authenticated profile
  -> career requirements + batched prerequisite foundations
  -> LearningPriorityPolicy (pure deterministic ordering and capacity gates)
  -> learning-priorities-v1 with reasons and evidence status
  -> dashboard Next Best Action (server-rendered GET selector and native details)
```

`LearningDecisionService` assembles inputs inside a read-only transaction.
`LearningPriorityPolicy` owns scores and group assignment and has no database,
network or model dependencies. `SkillDependencyService` reuses Phase 1 readiness
for both career skills and their foundations. The public Phase 1 batch continues
to return only career skills. No cross-user decision cache is introduced.

The dashboard's target is carried in `careerId` searchParams, validated against
the current catalog, and sent through the existing server-only bearer client.
An invalid selection produces a selection prompt. Decision API failure keeps
the selector/retry control available. Profile writes remain in the existing
profile flow. Career scoring and response contracts remain unchanged.

No migration or decision persistence is needed. Market weight stays zero;
roadmap generation and AI remain later phases. The [policy](../decision/SCORING.md)
documents the formula, tie-breaking, capacity limits and self-report thresholds.

## Roadmap flow (hackathon Phase 3)

```text
Authenticated profile + selected career + learning-priorities-v1
  -> RoadmapGenerator (topological ordering, known-skill skipping, demo effort)
  -> persisted Roadmap / RoadmapPhase / RoadmapTask aggregate
  -> derived current stage, next action and bounded weekly focus
  -> server-rendered roadmap with title/task Server Actions
```

Generation creates an explicit snapshot with policy versions, profile timestamp
and saved weekly hours. Subsequent profile changes do not rewrite it. New plans
preserve old IDs and link to the previous plan. The current plan is the latest
created, not the most recently edited. Completion never updates profile skills.

`RoadmapService` owns transactions and resolves each resource by ID and caller.
Updates validate all proposed task states before mutation. Prerequisites use
saved profile evidence or completed tasks; skipping an unknown foundation does
not unlock it. Parent optimistic locking covers child edits and returns a safe
409 on stale saves. Concurrent failed transactions roll back all edits.

The `/roadmap` page exposes empty/loading/error states, current and older plans,
native stage disclosures and controlled editors that retain input on errors.
Editor identity includes the roadmap/task ID and revision to reset local state
when navigating between saved plans. This week is a capacity suggestion, not a
dated progress ledger. See [generation policy](../roadmap/GENERATION.md).

## Weekly progress flow (hackathon Phase 4)

```text
Owned roadmap + explicit Start this week
  -> WeeklyAllocationPolicy (ready tasks + bounded capacity)
  -> saved WeeklyPlan and task snapshots
  -> student outcomes, actual time, next capacity and optional constraints
  -> one transaction: validate -> explicit roadmap edits -> next plan -> check-in
  -> current reflection, next allocation and private weekly history
```

`WeeklyProgressService` resolves the authenticated owner and enforces Monday/UTC
periods, exact task coverage, duplicate prevention and atomic persistence.
`RoadmapService` retains authority over transitions/prerequisites; check-ins use
its revision protection. Constraints and capacity ratings record student context
without inferring sensitive circumstances or changing profile proficiency.
An injected UTC Clock makes week boundaries and late submissions testable.

The `/progress` page reads current/selected week, reflection and paginated history
through the server-only bearer client. Controlled form inputs remain after failed
saves, and a missed-week shortcut records zero hours without shame language.
Snapshot allocations remain separate from the roadmap's live suggested focus.
An already saved next-week plan is preserved on late submissions; the UI shows
both reported availability and saved capacity when they differ. Phase 5 will
introduce broader adaptation. See [weekly policy](../progress/WEEKLY_CHECK_INS.md).

## Architecture decisions

- Modular monolith over microservices keeps local development understandable.
- Stateless bearer authentication supports a separate Next.js client while the
  HttpOnly server cookie avoids browser token storage.
- Flyway owns production DDL; Hibernate validates it.
- Shared normalized skills support student, career, and job relations.
- Server Components perform authenticated reads and Server Actions perform UI
  mutations, minimizing client bundles and avoiding duplicated browser fetches.
## Phase 5 adaptation flow

The progress module owns `AdaptationPolicy` (deterministic recommendation),
`AdaptationService` (authorization, snapshots and transactional acceptance) and
the additive V6 audit schema. Weekly check-in submission flushes its progress
record, then generates a proposal using the updated roadmap and chronologically
ordered reflections through that week. GET requests only retrieve saved data.

The saved next-week allocation remains the Phase 4 baseline until explicit
acceptance. Acceptance and check-ins share the owned-roadmap pessimistic lock;
the service refreshes entities after acquiring it and checks both roadmap and
weekly-plan versions. This prevents an old form from recording outcomes against
a revised allocation, including when its task IDs happen to be unchanged.

The `/progress` screen shows before/proposed/accepted values, maintenance mode,
the learning resume point and blocker questions. Its controlled edit form posts
through an authenticated Server Action; the backend revalidates all values and
eligibility. Historical plans that were already checked in cannot be overwritten.
See [the complete policy](../progress/ADAPTIVE_ROADMAPS.md).
## Phase 6 simulation flow

The simulator module calculates a transient before/after view from one recorded
profile and one active career. `SkillSimulationService` creates an immutable
proficiency map plus a hypothetical copy, then calls the explicit-map overloads
in `LearningDecisionService` and `SkillDependencyService`. Existing authenticated
calculation methods delegate to the same code, preserving policy behavior.

Only the selected skill is raised to at least INTERMEDIATE. No entity is changed
or saved, and the module has no dependency on roadmap/progress mutation services.
The response retains missing-ancestor context and internal demo-data labels.
The frontend authenticates its calculation Server Action and renders transient
before/after state; changing selections discards the old preview. No persistence,
cache invalidation, AI provider or market ingestion is introduced in this phase.
