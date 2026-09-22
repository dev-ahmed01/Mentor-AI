# Opportunity Simulator Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans inline, then a fresh final review.

**Goal:** Let students compare recorded and hypothetical skill coverage, prerequisite eligibility and priorities without changing their saved data.

**Architecture:** Reuse the dependency graph and learning-priority policy with an explicit in-memory proficiency map. The authenticated simulator reads one profile, copies its map and raises one selected skill to at least INTERMEDIATE. Both states use the selected active career's requirements and existing policies. No persistence or migration is needed.

**Tech Stack:** Existing Spring/JPA and Next.js/React; no added dependencies.

**Spec:** `docs/MentorAI_Hackathon_Phases_and_Codex_Prompt.md`, Phase 6.

## Design and constraints

- POST `/api/simulator/skill` requires `{skillId,targetCareerId}` UUIDs and authentication. Unknown skill or inactive/unknown career returns 404; malformed/missing input 400.
- Raise only the selected skill to INTERMEDIATE; preserve ADVANCED and never infer its prerequisites. This matches direct career targets in learning-priorities-v1. Foundation-only skills can also be simulated.
- Before/after include required/preferred satisfied counts and totals, eligible unfinished learning count and the full existing priority response. Satisfied means at least INTERMEDIATE for direct career requirements; modeled prerequisite threshold remains BEGINNER.
- Return newly satisfied selected-career requirements, newly eligible unfinished skills, effective assumed proficiency, selected-skill prerequisites, profile timestamp and policy/data labels. Unrelated or already-known skills can yield no change; make that explicit.
- Label SIMULATION / DEMO DATA / no market evidence. Counts describe internal catalog requirements, not jobs, hiring odds, overall career-fit scores or proof of learning.
- Never call profile, roadmap, check-in or adaptation mutation APIs. No hypothetical entity is attached to JPA; immutable map copies only. Preserve career-fit-v1 and existing priority API output.
- `/simulator` offers a career selection, relevant skill/foundation selection and a read-only preview. Hide old results when the selected skill changes. Display missing prerequisite caveats, both states, changed priorities and zero-impact cases. Link from navigation, roadmap and career details.
- Continue in the existing feature checkout; unrelated untracked tooling stays uncommitted. User selected API/build verification; no interactive browser checks.

## Review focus

1. Selected skill's missing ancestors remain missing; no false dependent unlocks.
2. Higher existing proficiency is never lowered; repeated simulation stays identical.
3. No persisted profile, roadmap, weekly plan or revision changes.
4. Empty profiles, missing availability, unrelated skills and no-effect cases remain usable.
5. Frontend selection changes cannot present a previous skill's result as current.

### Task 1: Backend simulation and regression tests

**Files:** `skills/service/SkillDependencyService.java`, `decision/service/LearningDecisionService.java`; new `simulator` request/response/service/controller and `SimulatorIntegrationTest`.
**Interfaces:** existing authenticated methods delegate to explicit-map calculation overloads; simulator responds with before/after LearningPrioritiesResponse plus coverage summaries.

- [x] Write tests for genuine Java/Foundation unlock, missing ancestor, already-known/unrelated/empty-profile cases, validation/auth and complete read-only behavior; observe missing endpoint failures.
- [x] Add explicit-map calculation paths and implement the read-only endpoint; run targeted and full backend suites, including priority compatibility.

### Task 2: Simulator UI

**Files:** new simulator types/action/form/page/loading state; existing navigation, roadmap and career-detail links.
**Interfaces:** authenticated action returns typed simulation data or a safe error; no save/accept mutation or cache invalidation.

- [x] Implement labeled before/after preview, coverage/eligibility/priority changes, prerequisite caveats and empty/error/pending states using installed Next.js guidance.
- [x] Run frontend lint/build; verify authenticated production rendering and live API with synthetic data.

### Task 3: Review and checkpoint

- [x] Fresh independent review; fix actionable findings and verify lint/build (interactive regression check waived by user).
- [x] Full PostgreSQL suite; document no schema change and preserve existing data.
- [x] Update API/architecture/policy/progress/implementation docs, run Graphify AST update, check diff and prepare the Phase 6 checkpoint.

## Ledger

- Baseline `d3eab86`, 58 tests from Phase 5. No tracked changes at task start. Existing `.codex/`, `AGENTS.md`, `graphify-out/` preserved.
- Ruling: the existing phase specification and explicit request authorize implementation without another permission gate. Skill-level simulation is a hypothetical minimum INTERMEDIATE, not a profile change or career-fit probability.
- Seven new integration tests first failed against the missing endpoint. Full H2 and PostgreSQL 17.11 suites then passed: 65 tests each, zero failures/errors/skips; PostgreSQL package built. Schema remains V6.
- Fresh independent review found stale career results could remain under a changed dropdown. The controlled career picker now hides the previous scenario until the selected career loads. Frontend lint, TypeScript and production build passed after this fix.
- Live PostgreSQL HTTP checks passed: direct coverage, valid foundation unlock, missing-ancestor protection, identical repeated simulations, and unchanged profile/roadmap/weekly-plan snapshots. Authenticated production page rendering and the JavaScript server-action HTTP protocol passed.
- Verification limitation: a separate no-JavaScript multipart form submission timed out with both PowerShell and Node clients; its cause remains unresolved. The JavaScript action protocol succeeds. Do not claim progressive-enhancement form submission or interactive browser behavior is verified. Browser/mobile/keyboard checks were omitted per the user's API/build-only preference.
- Local services were restarted after the interruption against the existing isolated database; no database reset or additional schema migration was needed.
- Graphify AST update passed: 1960 nodes, 4628 edges, 129 communities. Generated graph and unrelated local tooling remain uncommitted. Documentation semantic extraction was not requested.
