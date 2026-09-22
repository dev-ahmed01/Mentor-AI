# Hackathon implementation progress

Active source: [phases and Codex prompt](MentorAI_Hackathon_Phases_and_Codex_Prompt.md).
The original foundation/career phase numbers differ from the hackathon numbers.

## Phase 0 — baseline verification (15 September 2026)

- Preserved the existing uncommitted career implementation.
- Added career authentication, boundary, stable-list-identity, and reproducibility checks.
- Reproduced profile editing failure (HTTP 409) in the browser against PostgreSQL
  and in a failing integration test. Flush skill orphan removals before inserting
  replacement rows, within the same transaction; repeated saves and removal pass.
- Updated stale onboarding/dashboard copy and superseded AI-first plan pointers.
- Added Next.js's smooth-scroll attribute after reproducing its runtime warning.
- Java 21 and current Java 25: 11 tests passed, zero failures/errors.
- Frontend: locked install, lint, and production build passed using `npm.cmd`.
- PostgreSQL 17.11: V1 and V2 applied to an empty isolated database, followed by
  Hibernate schema validation. Windows launch uses `-Duser.timezone=UTC` because
  this PostgreSQL image rejects the legacy `Asia/Calcutta` name.
- Browser: registration, seven-step onboarding, dashboard, analysis, and career
  detail verified. No React list-key warning reproduced; all rendered IDs are
  present and distinct in the new API regression test.

The existing npm dependency audit reported 2 high and 1 critical findings.
Dependency hardening remains separate from feature verification.

- Current Java 25/PostgreSQL API smoke: registration, sign-in, repeated profile
  save (8 to 6 hours while retaining skills), career analysis and detail passed.
- Independent read-only review found no blockers in the baseline fixes.
- Post-fix Chrome recheck passed against the isolated H2 backend: sign-in,
  profile save, then another save changing 8 to 6 hours while retaining both
  skills. The regression also passes against real PostgreSQL.

## Next gate

Baseline committed as `35e0d40`. Phase 1 implementation is on
`feature/skill-dependencies`.

## Phase 1 — skill dependencies

- Added V3 with three reusable foundation skills and thirteen illustrative edges.
- Added direct/transitive prerequisite APIs and a career batch endpoint.
- Validated graph cycles at startup and reads; database constraints reject
  duplicate/self edges and invalid importance values.
- Added deterministic `skill-prerequisites-v1` eligibility: all modeled ancestors
  require BEGINNER or greater. Missing graph coverage is explicitly labeled.
- Added expandable prerequisite context to career pages. Existing career-fit
  scoring remains unchanged. Graph content is marked `DEMO DATA`.
- Invalid/missing request parameters now use the safe HTTP 400 error contract.
- Added graph and API tests for traversal, shared ancestors, cycles, authentication,
  isolation, read-only behavior, missing resources, proficiency and constraints.

Verification on Java 25.0.4.1:

| Check | Result |
| --- | --- |
| Full suite with H2 and clean V1–V3 migrations | 19 tests, 0 failures/errors |
| Full suite with fresh PostgreSQL 17.11 | 19 tests, 0 failures/errors |
| V2 to V3 PostgreSQL upgrade | Applied; existing student skill UUID retained |
| Existing user readiness after upgrade | Spring Boot eligible; both ancestors returned |
| Frontend lint and production build | Passed |
| Server-rendered missing/satisfied prerequisite pages | HTTP 200; expected status and DEMO DATA text |
| Chrome with production frontend and isolated H2 backend | Sign-in, repeated profile saves, career catalog/detail passed |
| Prerequisite interaction | Missing to satisfied after profile edit; mouse expand and keyboard collapse passed |
| Desktop and 390 x 844 mobile viewport | Readable details; no horizontal overflow at mobile width |
| Captured browser console | No warnings/errors |
| Independent code review | No actionable findings |
| Git whitespace check | Passed |

The PostgreSQL suite used a disposable database and environment overrides for
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`,
and `SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver`, with JVM UTC.
No production database or existing user data was reset.

Chrome reconnected and the remaining browser checks passed on 15 September 2026.
The original PostgreSQL test services had stopped, so the final browser pass used
an isolated in-memory H2 backend with the production frontend build. PostgreSQL
coverage is provided by the full integration suite and upgrade/API smoke checks
above. Phase 1 was committed as `a07835f` before starting Phase 2.

## Phase 2 — learning priorities

- Added the authenticated read-only decision API and `learning-priorities-v1`.
- Reused the prerequisite batch to include foundational skills, while preserving
  the original career-only batch contract.
- Added required/preferred relevance, prerequisite bottleneck and proficiency
  factors with deterministic ordering, reasons and no market score.
- Added weekly immediate-focus limits: missing time = zero, 1–4 hours = one,
  5–9 = two, 10+ = three. These are focus limits, not completion-time promises.
- Added dashboard career selection, Learn now / Learn next / Later / Not yet,
  expandable explanations, prerequisite evidence and a career-detail entry link.
- Added the [policy specification](decision/SCORING.md), API and architecture docs.
- No migration, roadmap persistence, market ingestion or AI dependency added.

Verification on Java 25.0.4.1:

| Check | Result |
| --- | --- |
| New API tests before implementation | Five expected failures for the absent endpoint |
| Full backend suite, H2 with clean V1–V3 | 27 tests, zero failures/errors |
| Ranking/capacity/auth/isolation/read-only checks | Passed |
| Frontend lint and production build | Passed |
| Chrome production frontend + isolated H2 | Sign-in, target selection, profile changes and priority refresh passed |
| Missing availability / two hours / twelve hours | Zero / one / three immediate focuses respectively |
| Missing and satisfied foundations | Spring Boot moves out of Not yet after prerequisite skills are recorded |
| Already-met foundation | Spring Fundamentals is not recommended again |
| Desktop and 390 x 844 mobile viewport | Readable explanations and no horizontal overflow |
| Keyboard | Explanation disclosure opens with click and closes with Enter |
| Independent review | No actionable findings |

The browser pass used `127.0.0.1:3001`. An initial visit to `localhost:3001`
encountered an old session for a user from the previous, stopped in-memory test
database and produced the existing authentication error boundary. Using the
isolated origin with a fresh demo login resolved the test-session mismatch;
no further browser warnings/errors were captured during the Phase 2 flow.

PostgreSQL V1–V3 and upgrade verification from Phase 1 remains recorded above.
The Phase 2 suite ran on H2; it was not rerun on PostgreSQL because the earlier
Docker test environment was unavailable. Existing migrations are unchanged.
The previously recorded dependency-audit findings remain outside this phase.
Phase 2 was committed as `98169ef` before starting Phase 3.

## Phase 3 — personalized roadmaps (15–16 September 2026)

- Added persisted user-owned roadmaps, learning stages, skill tasks and saved
  prerequisite evidence with additive V4 migration.
- Added deterministic generation from Phase 2 priorities, prerequisite order,
  recorded proficiency and weekly capacity; known targets start skipped.
- Added create/current/detail/update APIs, previous-plan links, atomic task edits,
  prerequisite gates and optimistic revision protection, including child edits.
- Added `/roadmap` with next action, current stage, weekly focus, later work,
  completed work, ordering reasons, title/effort/state editors and navigation.
- Added [generation/state policy](roadmap/GENERATION.md) and updated API,
  database and architecture documentation. Career fit scoring remains unchanged.

Verification on Java 25.0.4.1:

| Check | Result |
| --- | --- |
| New API tests before implementation | Seven expected absent-route failures |
| Full suite, H2 with clean V1–V4 | 34 tests, zero failures/errors |
| Full suite, PostgreSQL 17.11 with clean V1–V4 | 34 tests, zero failures/errors |
| PostgreSQL V3 to V4 upgrade and Hibernate validation | Passed; existing profile fields and skill UUID preserved |
| Ownership, generation/order, known-skill skips, state transitions, atomic rollback and stale revisions | Passed |
| Frontend lint and production build | Passed, including the review fix |
| Chrome production frontend + PostgreSQL | Sign-in, empty state, generation, task edit, completion and reload passed |
| Prerequisite UI | Blocked start/complete options disabled; completing Java unlocked Spring Fundamentals |
| Two-hour weekly availability | Two-hour partial allocation, then next eligible task after completion |
| Stale browser tab | Save rejected; typed title retained with refresh link |
| New generation and previous-plan navigation | Both plans preserved; title editor correct between distinct plans at revision 1 |
| Desktop and 390 x 844 mobile viewport | Readable roadmap, weekly focus and tasks; no horizontal overflow (375px content width) |
| Keyboard | Task state selected by keyboard; explanation disclosure closes with Enter |
| Captured browser console | No warnings/errors during verified flow |
| Independent review | Backend had no actionable findings; fixed frontend title editor identity across plans |

PostgreSQL ran from official portable binaries in an isolated temporary cluster
on loopback port 55433, using separate clean-suite and upgrade databases. No
production database was reset. The upgrade preserved business fields and UUIDs;
the profile timestamp was stored at PostgreSQL microsecond precision (the initial
write response included finer nanoseconds). Browser verification resumed on
16 September after an account usage limit interrupted the earlier attempt.

Weekly focus is a capacity suggestion, not a dated hours-worked ledger. Completion
is self-reported and does not change profile proficiency. No Phase 4 check-ins or
automatic adaptation were added. Previously recorded npm audit findings remain
outside this phase. Next: Phase 4 weekly check-ins and progress tracking.

## Phase 4 — weekly progress and life-aware check-ins (16–22 September 2026)

- Added V5 for weekly plans, allocated tasks, check-ins, task outcomes, blocker
  categories and optional temporary constraints. Existing migrations are unchanged.
- Added authenticated plan creation, current-week reads, final check-in submission
  and private history with 20-item pages. Weeks start Monday in UTC.
- Added atomic explicit roadmap completion/partial updates, stale-revision and
  duplicate protection, and next-week allocations bounded by reported capacity.
- Added zero-hour weeks, late reflections, deferred work and preserved existing
  next-week snapshots. Generic constraint categories require no private explanation.
- Added `/progress`, short controlled forms, planned/actual summaries, completed
  and carried-forward work, deferred work, history and next-plan explanations.
- Added [weekly policy](progress/WEEKLY_CHECK_INS.md), API/database/architecture
  documentation and navigation from the dashboard and roadmap.

| Check | Result |
| --- | --- |
| Baseline | 34 tests passed before implementation |
| New tests before implementation | Five expected missing-endpoint failures |
| Full H2 suite, clean V1–V5 | 44 tests, zero failures/errors |
| Full PostgreSQL 17.11 suite, clean V1–V5 | 44 tests, zero failures/errors; packaged backend successfully |
| PostgreSQL V4-to-V5 upgrade | Passed; existing profile, skill, task and roadmap rows unchanged |
| Partial/missed/deferred/completed outcomes and capacity boundaries | Passed |
| Temporary constraints, dates, integer-hour/rating validation | Passed |
| Ownership, duplicate check-in, stale revision, private paginated history | Passed |
| Late submission and existing next-week snapshot preservation | Passed |
| Frontend lint and production build | Passed; `/progress` included |
| Live HTTP with production backend and PostgreSQL | Plan/check-in/history, task updates, constraint, duplicate 409 and unchanged profile passed |
| Authenticated production server rendering | Submitted summary and zero-hour next-plan message passed |
| Independent frontend review | Fixed reported availability versus preserved plan capacity display; focused re-review passed |
| Final independent code review | No actionable findings |
| Interactive browser, mobile and keyboard checks | Not completed; user explicitly chose API/build verification only |

Verification used isolated loopback services and synthetic accounts. The live
HTTP/server-rendering checks resumed on 22 September after usage-limit
interruptions. The initial server-rendering smoke used a raw Cookie header that
did not authenticate; the corrected HTTP cookie-container check passed. This
does not substitute for interactive browser coverage, which remains unverified.

Next-week allocation uses stated capacity and explicit task outcomes. Broader
adaptive sequencing and roadmap revision policy remain Phase 5. Previously
recorded npm dependency-audit findings remain outside this phase.
## Phase 5 — adaptive roadmap engine (22 September 2026)

- Added deterministic proposals for behind/ahead weeks, repeated deferrals,
  maintenance during temporary constraints and resumed learning after expiry.
- Preserved the Phase 4 saved allocation until explicit acceptance; students can
  accept the suggestion or edit task selection and hours, including a zero-hour week.
- Added immutable before/proposed/accepted revision history, owner-only APIs,
  weekly-plan version checks and serialized acceptance/check-in operations.
- Added maintenance review outcomes that leave learning completion unchanged,
  plus a visible resume point and gentle blocker questions.
- Added the review/edit UI and [documented adaptation policy](progress/ADAPTIVE_ROADMAPS.md).

| Check | Result |
| --- | --- |
| Initial RED scenarios | Six missing proposal/endpoint failures observed |
| Stale allocation regression | Reproduced old form accepted with 201; fixed to 409 |
| Full H2 suite | 58 tests, zero failures/errors |
| Full PostgreSQL 17.11 suite | 58 tests, zero failures/errors; backend package built |
| Clean migrations | V1–V6 passed on H2 and PostgreSQL |
| V5-to-V6 upgrade | Existing profile, skill, roadmap, task, weekly-plan and check-in data preserved |
| Scenarios | Behind/ahead, repeated deferral, known skills, zero capacity, active/expired constraints passed |
| Safety and history | Ownership, stale/duplicate/invalid edits, historical targets, pagination and concurrent accept/check-in passed |
| Independent review | One finding fixed: missed maintenance weeks now resume at reported capacity after expiry; regression observed RED then GREEN |
| Frontend lint / production build | Passed, including TypeScript and `/progress` |
| Live API / production SSR | Pending proposal, edited and unedited acceptance, zero-hour plan, duplicate 409, immutable history, profile preservation and maintenance/resume display passed |
| Graphify AST update | Passed: 1841 nodes / 4297 edges; six SQL files omitted because optional SQL parser is absent |
| Interactive browser, mobile, keyboard | Not run, per user's API/build-only preference |

The previous local frontend process initially held the standalone build directory
open; stopping that identified process resolved the build. PostgreSQL was restarted
after an interruption before upgrade verification. Graphify's interpreter was
accessible with tool escalation; no interpreter repair or package install was needed.

Old check-in clients may omit `expectedPlanRevision` for an unrevised plan (version
0); accepted revisions require the refreshed value. Constraints remain active
through their recorded end date. No AI, market estimates, career changes or effort
recalculation were introduced. Next: Phase 6 opportunity unlock simulator.
