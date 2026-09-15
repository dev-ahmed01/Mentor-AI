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
above. Phase 2 has not started.
