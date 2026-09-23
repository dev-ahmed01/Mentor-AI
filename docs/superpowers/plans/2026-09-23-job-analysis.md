# Job Analysis Implementation Plan

> Execute inline with executing-plans and test-driven-development; one fresh final review.

**Goal:** Compare a pasted job description with recorded student skills and recommend preparation while preserving saved learning plans.
**Spec:** `docs/MentorAI_Hackathon_Phases_and_Codex_Prompt.md`, Phase 8.
**Architecture:** Authenticated extraction preview, editable review, then an immutable owner-only analysis. Deterministic parsing/matching; existing prerequisite and priority policy supply preparation decisions. Additive V8 JSON snapshot table; Next.js server actions and private result route. No dependencies or AI.

## Design and rulings

- The existing phase specification and instruction to implement the next phase authorize this work. Continue the existing feature checkout from `601bdd0`; keep native PowerShell bookkeeping here. API/build-only verification persists. No push, deployment, Phase 9 or job board.
- `POST /api/jobs/extract` accepts nonblank description up to 20,000 characters, returning an unsaved draft. Recognize controlled catalog names/aliases at Unicode word boundaries. Only explicit required/preferred sections or unambiguous single-skill cues classify requirements. Negation and mixed cues remain unclassified. Original text stays intact; markup is never executed. Metadata extracted only from explicit labels/sections, otherwise blank for user review.
- `POST /api/jobs/analyses` requires explicit reviewed=true, original description, editable title/responsibilities/experience/location/technologies, and up to 100 required/preferred/unclassified names each. Trim and deduplicate by canonical catalog ID or normalized name; required takes precedence. Unknown requirements stay UNASSESSED, never silently satisfied. Save original text, original extraction, reviewed job, profile skill inputs/time/version, results and frozen prerequisite decisions.
- `GET /api/jobs/analyses/{id}` is owner-only, returning 404 to other users. No remote URL fetching, shared pasted jobs, automatic application, profile or roadmap updates.
- `job-match-v1`: each mapped required skill has weight 3, preferred 1. Missing=0, Awareness=1/3, Beginner=2/3, Intermediate/Advanced=1. Round 100*achieved/total; null when no classified mapped skills. Intermediate is an illustrative comparison target, not an extracted employer proficiency. Unknown/unclassified requirements and experience/location/responsibilities are excluded and visibly limit the result. Matched, partial, missing-required, missing-preferred and unassessed groups are explicit.
- Preparation reuses dependency graph and LearningPriorityPolicy without market bonuses. Include prerequisites for all mapped classified targets; foundation target Beginner, direct target Intermediate. Adapt career reason labels to job labels. Preserve readiness/time gates; results do not change career priorities or plans.
- `/jobs/analyze`: paste -> extract -> edit/review -> save -> `/jobs/analyses/{id}`. Changing the paste hides any stale draft; submission errors preserve inputs. Original and reviewed text render as escaped React text. No `/jobs` listing is added: pasted private analyses are not a verified job inventory.

## Tasks

### 1. Backend contract and matching
- [x] Write integration tests for missing endpoints, weighted math, partial/missing/unknown skills, aliases/dedup, validation, auth, immutable private snapshots and preparation gates; observe RED.
- [x] Add jobs models/controller/extractor/matcher/service/repository and V8 migration; reuse skill repository, dependency service and priority policy.
- [x] Run focused tests and full H2 suite, expected zero failures/errors. Record deviations and exact results.

### 2. Review and result UI
- [x] Read installed Next.js form guidance, add typed server actions/review form/result component/routes and navigation.
- [x] Run frontend lint and production build, expected success; check loading/errors/empty/unassessed results in implementation review.

### 3. Verification and checkpoint
- [x] Fresh independent review against Phase 8; reproduce and fix important findings in one pass.
- [x] Run final H2/PostgreSQL suites, V7-to-V8 preservation and live API/production SSR/action checks. Expected immutable results and no changes to profile/roadmap/weekly plans.
- [x] Update API/database/architecture/policy/progress docs, Graphify AST graph; check diff and commit local checkpoint.

## Review focus
1. Mixed, negated and section-based requirement language must not silently invent mandatory skills.
2. Unknown requirements, aliases, duplicate names and empty matches must not produce misleading full-job readiness claims.
3. Saved analyses must isolate users and freeze the exact inputs/results through later profile edits.
4. Required preparation foundations and time gates must survive direct/preferred overlaps.
5. Long/untrusted pasted text, stale edited forms and validation failures must stay bounded and preserve a reviewable result.

## Ledger
- Baseline `601bdd0`: tracked tree clean; 81 tests each H2/PostgreSQL and frontend build passed in Phase 7. Graphify query succeeded with escalation after sandbox Python lookup failure.
- Pre-flight: extraction draft and analysis request share editable fields; server revalidates every field and remaps reviewed names against controlled vocabulary. Matcher outputs immutable analysis consumed by the result route. Existing market/learning endpoints remain unchanged.
- Ruling: use existing approved phase scope and current feature checkout instead of repeating design approval or creating another worktree. The paste/review flow is a new subsystem but its requirements are supplied by the phase plan; implementation details above remain reviewable. Cost if wrong: adjust the local implementation before any release.
- Task 1: initial missing-endpoint failures observed. Corrected a profile fixture missing mandatory category/source, then confirmed weighted matching also reached the absent endpoint. Initial implementation revealed two fixture assumptions: PostgreSQL is not in the seeded controlled catalog, and null response fields are omitted globally. Tested real REST aliases and the existing omission contract instead; no catalog expansion. Full H2 suite passed: 89 tests, zero failures/errors/skips.
- Task 2: installed Next.js form guide and React best-practices checks applied. Review fields remain controlled on errors; changed source text hides stale drafts; auth checked in each server action; saved result remains a server component. Lint and TypeScript/production build passed with `/jobs/analyze` and `/jobs/analyses/[id]`.
- Verification: captured V7 fingerprints (counts + ordered row-content hashes) for all 37 existing tables in the isolated live database before upgrading. PostgreSQL suite and fresh independent review are running.
- Initial PostgreSQL suite passed all 89 tests and packaged the backend. Final review found three Important issues, no Critical/Minor issues: sentence cues affecting contextual skills/unsupported inline sections, contracted negation, and failed re-extraction discarding edited review state. Three parser regressions failed (explicit-section control passed); the server-action regression failed by losing the previous draft. Fixes keep ambiguous mentions unclassified, reset unsupported headings, recognize contractions and retain the previous draft on failure. The action regression now passes; final suites follow.
- Final review rulings: broad NLP completeness/alternatives/non-English extraction remain limited and require explicit review (cost: manual corrections); custom profile aliases retain existing identity behavior, now documented (cost: apparent gaps until profile names are corrected); extraction is recomputed and frozen at save, while reviewed requirements remain authoritative (cost: original suggestions may differ if a future catalog edit occurs between preview/save). Browser interaction remains unverified by user preference. Job-board/history/edit/delete APIs, AI and employment probability remain outside the phase scope. No review findings are deferred.
- Final fix verification: all 93 H2 tests passed, including four new extraction regressions; the server-action draft-retention regression passed; final frontend lint, TypeScript and production build passed. Graphify AST update succeeded: 2277 nodes / 5632 edges / 143 communities. No semantic relabeling or API expenditure was required.
- Final PostgreSQL 17.11 suite: 93 tests, zero failures/errors/skips; package built. Live checks resumed after usage-limit approval rejection and the user's continue. The local PostgreSQL cluster recovered without resetting data. V7-to-V8 preserved all 37 existing table counts and ordered content hashes before new smoke-test accounts were created.
- Live API/SSR/actions passed: extraction, weighted result 43/100 with PARTIAL_ANALYSIS, unknown requirements, immutable owner-only results after profile edits, invalid/unreviewed input rejection, unchanged profile/roadmap/weekly plan, authenticated result pages and escaped source markup. Manual Flight FormData encoding in the ignored test harness failed validation; the installed React client encoder produced successful extraction and save/redirect requests. No product change was required. Browser and no-JavaScript checks remain unverified under the requested scope.
