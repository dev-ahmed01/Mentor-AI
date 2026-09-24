# Adaptive Roadmaps Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans for inline implementation and a fresh final review.

**Goal:** Propose explainable pacing changes after check-ins, with explicit acceptance or edits and durable revision history.

**Architecture:** Extend the existing weekly-plan/check-in flow. A deterministic policy proposes an allocation; a separate adaptation service persists before/proposed/accepted snapshots and applies a student's choice under the existing owned-roadmap lock. Career goals, task estimates and profile proficiency stay unchanged.

**Tech Stack:** Existing Java/Spring/JPA/Flyway, PostgreSQL/H2 and Next.js/React stack; no new dependencies.

**Spec:** `docs/MentorAI_Hackathon_Phases_and_Codex_Prompt.md`, Phase 5. Concrete policy and API decisions below refine that authorized phase.

## Global constraints and design decisions

- Preserve Phase 4 response fields and baseline next-plan creation. Automatically generate a PENDING revision after a check-in, but change the saved allocation only on explicit acceptance. Show both clearly.
- Below 60% of planned hours with unfinished work: suggest capacity `min(reported, max(1, actual))`, bounded by zero if reported zero; reduce focus to at most one fewer task than this week (minimum one). Preserve dependency order, prefer unfinished prerequisites, and never stack backlog.
- Completed work remains completed and unlocks dependent tasks using existing prerequisite checks. Hours alone never imply completion. Skills satisfied at generation stay excluded from new learning.
- Three DEFERRED outcomes for the same task since its most recent PARTIAL/COMPLETED outcome (across submitted weeks, not submission timestamps) pause automatic reassignment and surface a blocker question. Student may explicitly select it in an edited plan.
- Any recorded temporary constraint overlapping the target Monday–Sunday triggers MAINTENANCE: at most two hours and one lightweight review of an in-progress, needs-review, completed or already-known task. If none exists, leave the week empty. Preserve a ready unfinished resume point. Review outcomes do not change roadmap completion. After constraint expiry normal allocation resumes; capacity remains the student's latest report.
- Revision snapshots store what/why/when/trigger plus exact before, proposed and accepted allocations, mode, resume point and blocker questions. Reads never generate revisions.
- Accept or edit: POST `/api/adaptations/{id}/accept`, required expected roadmap and plan revisions; optional complete allocation override (capacity plus selected task IDs/hours). Validate ownership, current eligibility, duplicate IDs, integer hours, estimates, capacity and maintenance restrictions. Refuse already accepted, stale, historical or checked-in target plans with 409. No target on another roadmap is changed.
- GET `/api/adaptations/{id}` and paged GET `/api/roadmaps/{id}/adaptations` are owner-only. Add `adaptation` to check-in responses and `revision`/`mode` to weekly plans. V6 is additive; V1–V5 unchanged.
- Continue in the existing feature checkout as requested; preserve unrelated `.codex/`, `AGENTS.md` and `graphify-out/` files. API/build verification only, per user preference.

## Review focus

1. Late check-in targeting another roadmap or an already completed week must not rewrite it.
2. Concurrent accept/check-in operations must not overwrite recorded outcomes or apply a proposal twice.
3. Maintenance review must not reopen completed skills or falsely complete partial learning.
4. History must retain snapshots after student edits and task-title changes.
5. Empty/zero-capacity weeks, expired constraints and repeated deferrals must have a usable next step.

### Task 1: Backend proposal policy and persistence

**Files:** new `backend/.../progress/adaptation/` policy, DTOs, entity, repository, service and controller; V6 migration; modify weekly plan/check-in DTOs, entities, service and repositories.

**Interfaces:** `AdaptationPolicy.propose(...)` produces immutable snapshot data. `AdaptationService.propose(...)` persists it after the check-in flush. `forCheckIn(...)` reads it; `accept(...)` validates and updates only the weekly allocation.

- [x] Write scenario integration tests for behind/ahead/maintenance/resume/repeated deferral/known skills; run against Phase 4 and observe missing proposal failures.
- [x] Implement the deterministic policy, V6 and proposal storage. Reuse existing authorization, task readiness and strict integer parsing.
- [x] Add acceptance, edit and history tests for immutable snapshots, malformed/foreign/stale/duplicate requests, expired targets and maintenance outcomes. Run the targeted tests, then full H2 suite.

### Task 2: Review and edit UI

**Files:** new `frontend/src/components/AdaptationReview.tsx`; progress types/actions/view; relevant installed Next.js forms/server-client docs.

**Interfaces:** check-in `adaptation` carries before/proposed/accepted snapshots, candidate tasks, status, live revisions and history metadata. Server action POSTs acceptance/edits and refreshes progress/roadmap/dashboard.

- [x] Present what changed and why, maintenance/resume point and blocker questions; show a controlled editable capacity/task allocation form with pending/error states.
- [x] Show accepted historical snapshots and revision dates without confusing proposals with saved allocations. Explain review-only outcomes during maintenance.
- [x] Run frontend lint and production build.

### Task 3: Verification, review and documentation

- [x] Run full PostgreSQL suite and V5-to-V6 upgrade preserving old rows; exercise live API and authenticated production rendering.
- [x] Independent whole-change review, fix actionable findings with regression coverage.
- [x] Update policy/API/database/architecture/readme/implementation status and verification ledger; attempt `graphify update .` as repository instructions require.
- [x] Check staged diff, commit only Phase 5 files and report tests and any limitations.

## Execution ledger

- Baseline commit: `b3486f0`. No tracked changes; unrelated untracked repository tooling preserved.
- Ruling: existing phase specification and explicit request to implement authorize inline execution; no repeated design permission gate.
- Graphify query attempted: installed launcher references missing Python 3.12 executable. Direct inspection fallback; update will also be attempted.
- Initial six adaptation tests failed on absent proposals/endpoints; seven initial scenarios then passed after adding weekly-plan version validation. The stale-form regression first returned 201, then correctly returned 409.
- Full H2 suite reached 57 passing tests after known-skill, validation, late-submission, pagination and concurrent accept/check-in scenarios. One test fixture was corrected to use the existing PUT roadmap route; another retained weekly availability when replacing a profile.
- Frontend lint and production build passed. First build encountered the previous local standalone server's Windows directory lock; stopped the identified Phase 4 process and rebuilt successfully.
- Final independent review found one pacing issue: missed maintenance work incorrectly took precedence over resume after constraint expiry. Added `missedMaintenanceWeekResumesAtReportedCapacityOnceConstraintExpires`, observed BEHIND instead of RESUME, and excluded maintenance sources from the behind rule. Final whole-suite/database verification follows.
- Ruling: missing `expectedPlanRevision` means 0 for compatibility with old plans; revised allocations require the actual version, preventing silent submission against a changed budget even if task IDs match.
- Final backend verification: 58 tests, zero failures/errors on H2 and PostgreSQL 17.11; packaged backend successfully. Logs: `backend/target/phase5-h2-tests.log` and `phase5-postgres-tests.log`.
- PostgreSQL upgrade from V5 to V6 passed with exact existing profile/skill/roadmap/task/weekly-plan/check-in business data preserved; health UP. Local cluster was restarted after interruption, with its existing data retained.
- Live HTTP and authenticated production rendering passed: pending behind proposal, explicit zero-hour edited acceptance, immutable snapshots, duplicate 409, unchanged profile, maintenance proposal/acceptance/resume-point display. Helper: ignored `backend/target/phase5-api-smoke.ps1`.
- Graphify's initial missing-interpreter messages were sandbox access failures. Its installed interpreter worked with escalation; `graphify update .` succeeded (1841 nodes, 4297 edges). Six SQL files were skipped because optional `tree_sitter_sql` is absent; docs semantic extraction was not requested. No package installation or user tooling edits were required.
- Interactive browser/mobile/keyboard checks remain outside the user-selected API/build verification scope.
