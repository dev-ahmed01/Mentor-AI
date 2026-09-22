# Market Evidence Implementation Plan

> **For agentic workers:** Execute inline with superpowers:executing-plans, then one fresh final review.

**Goal:** Ingest source-traceable public observations, publish bounded immutable market snapshots, and enable explicit evidence-based career/skill decisions only when evidence qualifies.

**Architecture:** A replaceable provider feeds normalization, immutable observation storage and per-career snapshots. An opt-in startup/scheduled collector uses one fixed Arbeitnow API endpoint; application users cannot initiate network collection. Authenticated reads expose provenance and limitations. Explicit snapshot decisions preserve original profile-only APIs and saved roadmaps.

**Tech Stack:** Existing Spring/JDBC/JPA/Flyway and Next.js. Java HTTP client; no new dependencies.

**Spec:** `docs/MentorAI_Hackathon_Phases_and_Codex_Prompt.md`, Phase 7 (lines 748–817).

## Design and rulings

- Existing phase specification and “next phase” authorize this implementation. Continue the existing feature checkout at `8fb8c95`; preserve untracked tooling. Keep the ledger here using native PowerShell instead of the skill's shell bookkeeping helpers.
- Provider: Arbeitnow public API, fixed HTTPS host/path, one page (at most 100 records), no redirects, 20-second timeout and 5 MB response cap. API terms require backlink attribution. Collection disabled by default; operator opt-in, persistent six-hour attempt cooldown, failures never replace evidence with fabricated data. No credentials, scraping or user-controlled fetch URLs.
- Store original payload, source identity/URL, publication and collection time, location/remote flag, normalization version and deterministic extracted skills. Description text is untrusted and never rendered as HTML. Match controlled skill names/aliases at word boundaries; explicit required/preferred markers only, otherwise UNSPECIFIED. No inferred proficiency or mandatory status from mere mentions.
- Deduplicate within a fetch by canonical URL and normalized employer/title/location; keep immutable revisions across refreshes. A new snapshot uses only that fetch's valid records published in the preceding 30 days, never cumulative repost counts. Original evidence rows and snapshot membership remain addressable.
- Conservative title matching to catalog career/job titles. Record the match policy and title list with each snapshot. Keep local source locations, not an invented global region. Show this is a bounded single-source sample, not total demand or entry-level suitability.
- Qualifying evidence requires 10 distinct listings, 3 distinct employers, and catalog skill matches on at least 60% of listings. Fresh for 72 hours from collection; stale snapshots remain readable but cannot influence a new decision. Thresholds are product guardrails, not statistical confidence claims.
- Store snapshots, frequencies, observation IDs and policy version immutably. Public DTOs omit raw descriptions. No synthetic market seed rows.
- `GET /api/market?careerId=...` returns latest evidence or UNAVAILABLE; `GET /api/market/snapshots/{id}` and `/observations/{id}` retain traceability. All authenticated.
- `POST /api/market/snapshots/{id}/decisions` saves an owner-only immutable decision; `GET /api/market/decisions/{id}` retrieves it. Include profile inputs/timestamp, snapshot, evaluation time, formula version and result. No user profile/goal/roadmap mutation.
- Career compatibility measures recorded skill coverage of this sample's mentions (all mention categories count once); Intermediate/Advanced=1, Beginner=2/3, Awareness=1/3, missing=0. Use existing profile factors' exact weighted sum plus the reserved 15 market points, denominator 100. If evidence is ineligible, preserve the profile-only indicator and market weight 0.
- Skill priorities optionally add up to 10 points (`round(10*mentionCount/sampleSize)`) to existing unsatisfied skill scores, without overriding prerequisite/proficiency gates or time limits. Existing priority and simulator endpoints retain v1 behavior. Return evidence context in the saved market decision wrapper.
- `/market` displays evidence status, update/window/sample/employers/coverage, attribution, frequency counts and source links, with a button to evaluate the student's profile against that exact snapshot and a durable decision URL. Errors/loading/empty/stale states are explicit. No Phase 8 job matching or AI.

## Review focus

1. Duplicate/reposted data or changed old records must not inflate a frozen snapshot.
2. Stale/undersized/low-coverage evidence must never activate scoring.
3. Remote locations and single-source samples must not imply global applicability.
4. Raw content, malicious URLs, absent fields and provider failures must remain bounded and safe.
5. Saved decision retrieval must isolate owners and remain unchanged after profile/evidence updates.

### Task 1: Ingestion and evidence

- [x] Write failing integration tests for unavailable API/auth and schema; then normalization/aggregation and provider-failure tests.
- [x] Add V7 source state, immutable observations, snapshots/membership and owner decisions; add provider/normalizer/repository/analytics/ingestion services.
- [x] Verify deduplication, provenance, invalid/future/old records, stale/insufficient coverage, failures and cooldown with synthetic fixtures.

### Task 2: Evidence-based decisions and UI

- [x] Add saved snapshot decisions and optional market priority calculation, preserving v1 behavior.
- [x] Verify scoring math, eligibility gates, ownership, reproducibility, prerequisites and saved-data preservation.
- [x] Build authenticated market pages/action/types, navigation and context links; lint and build after review fixes.

### Task 3: Verify and checkpoint

- [x] Full H2/PostgreSQL tests and V6-to-V7 preservation check; live allowed provider ingestion and API/production SSR verification.
- [x] Fresh independent review and one fix pass; update API/database/architecture/policy/progress docs and Graphify AST graph.
- [x] Check diff and create local Phase 7 checkpoint, no push. Report limitations honestly.

## Ledger

- Baseline `8fb8c95`: 65 tests each on H2/PostgreSQL from Phase 6; tracked tree clean. API/build-only preference persists.
- Pre-flight: ingestion produces immutable snapshot DTOs; decisions consume only a pinned snapshot; UI consumes the same DTOs and saved decision URLs. Default existing decision/simulator APIs consume no market snapshots, avoiding silent roadmap changes.
- Sources checked 22 September 2026: https://www.arbeitnow.com/blog/job-board-api and https://www.arbeitnow.com/terms (section 11). API is explicitly offered for projects, with backlink attribution. Source coverage and weak extraction remain visible limitations.
- Initial missing endpoint/schema failures observed, then ingestion and decision tests passed. Edge regressions reproduced dotted-name extraction, negation/empty visible identity and rejected-source success status; fixed before full suites.
- Initial full H2 and PostgreSQL suites: 76 tests each, zero failures/errors; PostgreSQL package and frontend production build passed. Final review fixes require rerunning these checks.
- Fresh independent review completed after usage-limit interruption: three important findings, no critical findings. Reproduced all three before fixing: user-created skills polluting evidence vocabulary; sentence-wide required/preferred cues leaking across skills; incomplete saved scoring inputs. Extraction now uses controlled career/prerequisite IDs, ambiguous multi-skill sentences stay UNSPECIFIED, and private comparisons save the complete scoring-input subset, catalog inputs and base policy version.
- Review rulings: thresholds are documented product guardrails, not statistical confidence. Source representativeness/listing truth cannot be established by code review and remain explicit UI limits. Source API/terms were checked by the implementer. Browser accessibility/rendering remains unverified under the user's API/build-only choice. Runtime provider behavior and final test/upgrade outcomes are covered by remaining implementer checks.
- Review regression tests observed RED (two assertion failures and missing persisted input), then all 78 H2 tests passed. Final frontend lint has no warnings; TypeScript/build passed with both market routes. PostgreSQL rerun in the earlier test database hit nine duplicate fixed-email fixtures; use a fresh isolated database for the final suite, preserving the prior database.
- Runtime ruling: the first real source response exceeded the original 2 MB cap; Java surfaced cancellation as an HTTP/2 stream error. A bounded HTTP/1.1 diagnostic established the payload exceeded 2 MB. Raise the bounded cap to 5 MB; offline transport tests reproduce acceptance of a valid ~2.7 MB/100-record page and rejection beyond 5 MB. The initial failed collection/cooldown stays intact; final real-source verification uses a separate fresh database. No source data is fabricated or substituted for real evidence.
- V6-to-V7 upgrade passed before live-account creation: row counts and ordered row-content hashes matched for all 32 existing tables. Final H2 suite after transport fix: 80 tests, zero failures/errors/skips. Graphify AST update: 2148 nodes / 5219 edges / 140 communities; user tooling remains uncommitted.
- Final PostgreSQL 17.11 suite after transport fix: 80 tests, zero failures/errors/skips, package built. Both database suites validate V1–V7. Final frontend lint/TypeScript/production build passed; no new dependencies were introduced.
- Runtime ruling: the API currently returns 250 records in a ~2.05 MB page. After raising the byte cap, the original 100-record page rejection still prevented collection. Process only the first 100 records from the bounded page and describe that sampling limit explicitly. A 250-record regression first failed; the bounded prefix behavior is the intended contract. No pagination or additional source requests are introduced into the collector.
- Final bounded-prefix implementation: H2 and PostgreSQL 17.11 each passed 81 tests with zero failures/errors/skips; backend package built. Frontend lint/TypeScript/build remain passing with no subsequent frontend edits. Graphify AST update: 2149 nodes / 5222 edges / 143 communities.
- Live verification completed on 23 September local time: SUCCESS, 100 received / 91 accepted / 9 rejected / 10 snapshots. Full Stack Developer had 5 listings and 2 employers; INSUFFICIENT_SAMPLE correctly retained market weight 0. API smoke passed provenance, owner isolation, immutable saved decisions, profile/roadmap/weekly-plan preservation, validation, authenticated production SSR and JavaScript server-action save/redirect. Browser checks remain waived. A duplicate frontend launch found port 3005 already served by the existing verification process; the smoke used that running production build.
