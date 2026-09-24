# Combined Final Phase Implementation Plan

> Execute inline with executing-plans/TDD and one fresh final review.

**Spec:** `docs/MentorAI_Hackathon_Phases_and_Codex_Prompt.md`, combined Phase 11.
**Goal:** A reproducible synthetic demo, robust product navigation and verified release artifacts/documentation.
**Architecture:** An opt-in authenticated demo fixture uses existing profile/roadmap/progress services; additive V11 records ownership and seeded IDs. Existing deterministic policies remain authoritative. Production Docker/Compose and CI reuse the standalone frontend and packaged backend.

## Rulings and scope
- Baseline `8654974`, current feature checkout; preserve untracked tooling. User authorized the whole combined phase. Continue API/build/HTTP verification; no interactive browser or real model run. Prepare release artifacts locally; publishing requires a concrete target and authorization after local verification.
- Demo endpoints require authentication and DEMO_ENABLED (default false). Seeding only an unused account never replaces a personal profile or historical work. Record a synthetic banner permanently with the demo run. Retrying seed returns the same run, including concurrent requests; no reset/delete route. Rehearse afresh in a new account.
- Seed a second-year BCA student with Java BEGINNER, SQL INTERMEDIATE, Git BEGINNER, Backend career and eight weekly hours. Mark Git COMPLETED as explicit synthetic past work; create current weekly plan. Use the current UTC week so normal validation still applies. No mocked market claims or artificial AI success.
- An explicit exam action for that demo run submits a normal check-in: two actual hours, two available next week, LOW energy, NO_TIME, partial first task, other tasks missed, two-week EXAMS constraint. Existing adaptation proposal must still be explicitly accepted in the existing UI. Reject changed/current-source/week mismatches; retries never duplicate a check-in.
- The guide uses Spring Boot as the modeled blocked skill; Kubernetes is not in the Backend priority graph. Explain catalog limits instead of inventing a Kubernetes priority.
- Dashboard defaults to the current roadmap's career when no explicit selection exists. Replace development phase labels with student-facing labels. Add persistent demo banner, grouped native-details navigation, skip link, app loading/error states and review small-screen CSS without claiming browser verification.
- Security pass covers unknown-route sanitization, authenticated demo access, CORS/private health behavior, bounded frontend API waits, production dependency audit and tracked-secret scan. Existing prompt-injection/ownership suites remain mandatory.
- Deployment artifacts: non-root Java 25/Node standalone Dockerfiles, isolated PostgreSQL/required secrets in production Compose, CI for H2/PostgreSQL/lint/build/frontend tests and image smoke checks. Local dev Compose remains supported; optional Ollama stays opt-in. No cloud vendor, paid resource, secret or deployment is invented.
- Native PowerShell uses this plan as durable ledger, consistent with prior phases; one final checkpoint. Docker is currently absent, so verify config statically and direct production runtimes, and explicitly record container execution as pending unless a runtime becomes available.

## Tasks
1. [x] Demo/backend contracts: missing endpoints observed RED, then implementation and focused tests GREEN; final stale-week/manual-check-in regression included.
2. [x] Product/demo UI: guide/actions/forms, synthetic banner, dashboard default, mobile navigation and skip/error/loading states. Action tests, lint/build, HTTP pages and real server-action protocol passed.
3. [x] Release: Dockerfiles, production Compose, CI, portable seed/smoke scripts, audit, secret scan and runbooks. YAML/script syntax and build inputs checked; container execution explicitly pending.
4. [x] Final review and verification: one fresh review/fix pass; H2/PostgreSQL 124/124 each, preserved 42 table hashes, frontend 4/4/lint/build, live demo/adaptation/SSR/actions, Graphify and final report. Local checkpoint is the commit containing this completed ledger.

## Shared interfaces
- GET `/api/demo` returns enabled flag and optional owned run with source IDs; POST `/api/demo/start` requires confirmSynthetic=true and returns the same run on retries. POST `/api/demo/exam` consumes expectedRoadmapRevision/expectedPlanRevision and returns run plus saved check-in result. No caller-supplied owner or foreign resource ID.
- DemoService locks user then roadmap through existing services, matching pivot order. DemoRepository supplies account ownership and run metadata; frontend banner uses GET status only. Frontend and CLI both use ordinary authenticated APIs.
- V11 creates one demo_runs table without rewriting existing rows. Roadmap/check-in/adaptation policies and recorded history stay intact. All synthetic effects are confined to the signed-in demo account.

## Review focus
1. Demo opt-in cannot overwrite personal data, create unauthenticated accounts or bypass ownership.
2. Duplicate/concurrent demo requests, later weeks and manually edited plans must not duplicate or silently overwrite work.
3. Production secrets stay out of images/Git, database/backend are not publicly exposed, and real AI/market requests remain opt-in.
4. Small screens retain navigation and primary actions; server-rendered errors/loading states do not leak internals.
5. Release instructions/CI/scripts match actual startup, data preservation and verification limits; no deployed/container/browser claim without evidence.

## Ledger
- Graphify query completed; tracked baseline clean. Existing runtime is Java 25 / Spring Boot 3.5.16 / Next.js 16.3.1. No Docker executable discovered. Current mobile CSS hides all app navigation; current unknown authenticated routes fall through generic 500. Both are in final-hardening scope.
- Primary deployment references checked: Next.js self-hosting/standalone output, Docker Compose required interpolation and Eclipse Temurin official Java 25 image catalog. No new application dependency planned.
- Task 1: initial focused suite failed on missing demo endpoints; 7 focused checks then passed. An additional expired-week/manual-check-in preservation test passed in both complete 124-test suites. V11 is additive and writes no fixture data at migration time.
- Task 2: missing action module observed RED; 4 frontend tests now pass. Lint/TypeScript/build and production JavaScript server-action protocol passed, including missing confirmations and pending adaptation. Browser/keyboard/mobile/no-JS and a real model were not run, per user scope.
- Task 3: Next.js 16.3.6, Sharp 0.35.4 and compatible js-yaml update yield zero full npm audit findings. YAML, script syntax, tracked-secret guard and CLI first/repeated seed verified. Added a tracked public directory placeholder after checking standalone Docker COPY inputs. Docker executable is unavailable; remote CI and public deployment are not claimed.
- Final review: fresh gpt-6-astra reviewer found no Critical issue, one Important mobile menu issue and a release-instruction wording issue. Mobile menu now spans its nav container rather than extending offscreen when its summary wraps. Clarified Compose recreation after environment changes. CSS/docs are reversible low-impact changes: followed developer instruction to verify statically/build rather than add implementation-mirroring tests. No review findings deferred.
- Final verification: H2 and PostgreSQL each 124 tests, zero failures/errors/skips; packaged backend; frontend 4 tests/lint/build; production API/SSR/static assets; seed and exam action protocol; read-only simulation and AI-unavailable state; portable seed retry all passed. Full and production npm audits have zero findings; tracked-secret guard passed across 344 files. Final Graphify AST updated to 2701 nodes/7005 edges/166 communities. Original V10 fingerprints were checked again unchanged; both temporary APIs and both database processes were stopped cleanly.
- Ruling: the old temporary PostgreSQL cluster has missing template/role-catalog files and cannot produce a normal pg_dump. Leave it intact; clone its 42 readable application tables into a fresh V10 schema in a separate cluster, verify matching hashes, then upgrade the clone to V11. Every hash survived. Cost/limit: this verifies application-data preservation, not whole-cluster recovery or a valid full backup; report that explicitly.
- Final environment note: a frontend rebuild hit EBUSY while its temporary server held the standalone directory. Stop that identified process, rebuild successfully, then start the verified output for HTTP checks. No application change was needed. Release preparation is complete; container execution and target HTTPS verification remain external gates in the deployment guide.
