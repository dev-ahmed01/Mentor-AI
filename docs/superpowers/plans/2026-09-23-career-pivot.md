# Career Pivot Implementation Plan

> Execute inline with executing-plans and test-driven-development; one fresh final review.

**Spec:** `docs/MentorAI_Hackathon_Phases_and_Codex_Prompt.md`, Phase 10.
**Goal:** Compare a new career and explicitly accept a revised roadmap while preserving past work.
**Architecture:** Saved deterministic pivot preview, immutable comparison/source snapshot, atomic acceptance creating a linked roadmap. Reuse LearningDecisionService and RoadmapGenerator; additive V10 audit table, authenticated API and Next.js pages.
**Tech stack:** Existing Spring Boot/JPA/JDBC/PostgreSQL, Next.js/React. No new dependency or AI call.

## Design and rulings
- Continue from `993c6cd` on the existing feature checkout. User's next-phase instruction authorizes implementation; retain API/build-only verification and local checkpoint workflow. No Phase 11, push or deployment.
- Require an owned current source roadmap and a different active destination career. Save a preview without changing current roadmap/profile/weekly plans. Acceptance explicitly creates a new current roadmap with previousRoadmapId pointing to source. Existing weekly commitments/check-ins remain on their original roadmap; show this in the UI.
- Effective planning skills are current profile proficiency plus COMPLETED source tasks at their recorded target, taking the higher value. Also retain satisfied-at-generation credit from source tasks still SKIPPED; this carries prior pivot credit forward. NEEDS_REVIEW, unfinished and manually skipped unknown tasks supply no task credit. Credits are planning assumptions, not verified mastery, and never update profile claims.
- Transferable skills are effective known skills used by the destination (partial knowledge included). Newly required skills are destination REQUIRED/REQUIRED_FOUNDATION items absent from the old required set. List satisfied prerequisites and destination targets that can be skipped. Compare priorities using the same effective skills/hours for both careers, including removed requirements.
- Effort reuses generator estimates (0/4/8 hours): TARGETS_MET at 0, SMALL through 16, MODERATE through 40, SUBSTANTIAL above 40. Show illustrative total hours and rounded capacity-weeks, never employment readiness or a promised completion date.
- Save source roadmap snapshot, profile fingerprint, both priority results, credited task provenance and ordered proposed stages. Accept only if current source/revision/profile/calculations still match. Refreshing stale previews requires a new preview; saved old comparisons stay readable.
- Serialize pivot acceptance, roadmap creation and profile edits using the owning user row. Acceptance additionally locks the source aggregate. A repeated acceptance returns the original accepted roadmap; competing proposals cannot both replace the same current source. Past records remain editable through their existing flows; the pivot's source snapshot stays frozen.
- Native PowerShell execution uses this plan as the durable ledger instead of bash helper scripts, consistent with preceding phases. One final checkpoint covers the coherent slice.

## Tasks
1. [x] Backend: write PivotApiIntegrationTest for preview isolation/credits, owner validation, stale acceptance and idempotent/competing accepts; observe missing endpoints fail. Add pivot DTO/policy/repository/service/controller, V10, owner locking and reusable roadmap description. Run focused then full H2 tests.
2. [x] UI: add typed pivot preview/create/accept server actions, `/pivot` and `/pivot/[id]`, loading/error states and roadmap/navigation links. Saved comparison shows provenance, priorities, proposed stages and explicit confirmation. Run lint and production build.
3. [x] Review and verify: one fresh review, address Important/Critical findings with regression proof, PostgreSQL suite/package, V9-to-V10 table fingerprints, live API/SSR/actions. Update policy/API/database/progress docs, Graphify and prepare the local checkpoint.

## Interfaces and pre-flight
- Backend preview POST `/api/pivots` consumes sourceRoadmapId + targetCareerId; GET list/detail returns owner-only audit data. POST `/{id}/accept` consumes expectedSourceRevision and returns the same detail with acceptedRoadmapId/status.
- Saved comparison contains sourceRoadmap, before/after LearningPrioritiesResponse, transferableSkills, newlyRequiredSkills, satisfiedPrerequisites, skippableSkills, changedPriorities, credits, effort and proposedStages. UI never calculates authoritative credit/effort.
- Acceptance consumes exactly the saved after-priorities using RoadmapGenerator. New roadmap prerequisite gates therefore match the preview calculation. Proposed task IDs are not reserved; stages identify skills.
- Lock order is user then source roadmap. Existing check-in/adaptation paths only lock roadmap and do not request user locks, avoiding a reverse lock order.

## Review focus
1. Manually skipped/needs-review tasks must not produce transfer credit; repeated pivots retain justified prior credit without changing profile.
2. Concurrent acceptance and ordinary roadmap creation cannot both claim the same current source; retries do not duplicate roadmaps.
3. Profile or source edits between preview and acceptance must reject the stale proposal atomically.
4. Ownership applies to source, preview, history and accept; errors disclose no foreign records.
5. Existing roadmap/task/weekly/check-in/adaptation records stay intact; archived audit snapshots remain stable after later edits.

## Ledger
- Graphify query completed (2443-node graph); sandbox interpreter access required escalation. Tracked baseline clean. Existing roadmap service retains prior roadmaps but regenerates only from profile, so pivot-specific credit and audit belong in a separate service.
- Ruling: retained generation credit is allowed only while the source task remains SKIPPED/satisfied-at-generation, or is COMPLETED. Reopened work explicitly loses that task credit; current profile evidence remains authoritative independently.
- Five API contracts failed on missing pivot endpoints before implementation, then passed. Full initial H2 suite passed 110 tests. Expanded coverage exposed two test assumptions: the starter Data Engineer has no modeled Python prerequisite edge (use ML career to verify that edge), and null nextAction is omitted by existing JSON configuration. Corrected those expectations without changing product behavior.
- Frontend lint and production build passed after fixing a JSX apostrophe. React checklist: authenticated actions, parallel server reads, small client-only forms, explicit labels/status/errors, no HTML injection.
- Final independent review found two Important credit defects: lower destination targets could downgrade retained proficiency on subsequent pivots; reopening then manually skipping restored revoked generation credit. Add regressions and a separate task-credit ledger preserving proficiency/revocation without changing historical generation facts or profile claims. No Critical/Minor findings; one fix pass, no second review.
- Review rulings: unrelated historical roadmap merging and automatic weekly rescheduling remain excluded and disclosed; browser/mobile/keyboard/no-JavaScript checks remain waived; implementer owns PostgreSQL/migration/live checks; no deployment or model behavior applies. Mixed acceptance/profile/source/ordinary-generation races deserve targeted coverage beyond pivot-only concurrency.
- Both Important findings reproduced RED, then all ten focused pivot tests passed after introducing roadmap_task_credits and transactional revocation. Added the mixed-operation concurrency regression; final H2 suite passed 116 tests with zero failures/errors/skips. Graphify AST updated: 2578 nodes, 6643 edges, 148 communities. No deferred Minor findings.
- Final PostgreSQL 17.11 suite passed 116 tests with zero failures/errors/skips; package built and V1–V10 clean migration passed. After interruption, the preserved cluster recovered without a reset. V9-to-V10 upgrade preserved counts and ordered row-content hashes for all 40 existing tables, checked before live verification writes.
- Live API/production SSR checks passed: preview isolation, completed credit, private history, duplicate acceptance, chained pivots, escaped text and JavaScript asset serving. Source roadmap, profile, weekly plan and check-in/adaptation history responses remained identical after acceptance. React-encoded create/accept actions and missing-confirmation rejection passed. No browser/model run or Phase 11 work performed.
