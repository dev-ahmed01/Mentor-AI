# Skill Dependencies Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Compare a student's recorded skills with a target skill's prerequisites.

**Architecture:** Extend the existing skills module with a read-only directed
acyclic graph, backed by Flyway/JPA. Deterministic code calculates eligibility;
career scoring and existing career response contracts remain unchanged.

**Tech Stack:** Java 25 (current repository setting), Spring Boot, PostgreSQL,
Next.js 16, React, TypeScript. No additional application dependencies.

**Spec:** `docs/MentorAI_Hackathon_Phases_and_Codex_Prompt.md`, Phase 1.

## Global constraints

- Complete and checkpoint Phase 0 first; preserve existing work.
- New migration only; do not edit V1/V2.
- Reuse canonical skill IDs by normalized name, including user-created skills.
- Read APIs require existing bearer authentication. Profile comes from the
  authenticated user, never from a caller-supplied user ID.
- All modeled prerequisites are required. Importance is a 1–5 ordering hint.
- BEGINNER or greater satisfies a prerequisite; AWARENESS does not. This is
  self-reported preparation, not assessed competence or employment readiness.
- Check all transitive prerequisites, deduplicate shared ancestors, and reject
  cycles at startup. No public graph write endpoint in this phase.
- Empty graph coverage means no recorded prerequisites, not proven readiness.
- Seeded edges are DEMO DATA: illustrative learning guidance, not market evidence.

## Task 1: Preserve and verify baseline

- [ ] Run current Java 25 tests, frontend lint/build, PostgreSQL migration check.
- [ ] Browser-check profile editing, sign-in, careers and the console after fixes.
- [ ] Record results and commit the existing baseline, including the approved
  profile-save fix and preserved career implementation.

## Task 2: Dependency domain and authenticated API

Create under `backend/src/main/java/com/mentorai/skills/`:

- `entity/SkillDependency.java`: skill/prerequisite foreign keys and importance.
- `repository/SkillDependencyRepository.java`: fetch edges with both skills.
- `service/SkillDependencyGraph.java`: validate a DAG and collect ancestors.
- `service/SkillDependencyService.java`: load graph and compare profile evidence.
- `service/SkillDependencyValidator.java`: reject invalid seeded graphs at startup.
- `dto/SkillDependencyResponse.java`: direct edge contract.
- `dto/SkillPrerequisitesResponse.java`: target identity, prerequisites, eligibility,
  evidence label and per-prerequisite recorded proficiency/satisfaction.
- `controller/SkillController.java`: authenticated read endpoints.

Create `backend/src/main/resources/db/migration/V3__skill_dependencies.sql`.
Use unique `(skill_id, prerequisite_skill_id)`, non-self check, FK constraints,
importance check, and an index for reverse lookups. Resolve seed skill names
against the existing catalog instead of assuming fixed IDs.

Endpoints:

```text
GET /api/skills/{id}/dependencies       direct prerequisite edges
GET /api/skills/{id}/prerequisites      transitive personal readiness
GET /api/skills/prerequisites?careerId=...  batched career skill readiness
```

- [ ] Add integration tests first: retrieval, authentication, unknown IDs,
  beginner vs awareness vs absent skills, per-user isolation, unique/self edges.
- [ ] Run tests and observe failure before implementing the endpoints.
- [ ] Add graph unit tests for chains, shared ancestors, cycles and self-edges.
- [ ] Implement the smallest domain/API changes, then run all backend tests.
- [ ] Apply V3 on existing V2 PostgreSQL and validate clean V1–V3 migration.

## Task 3: Career prerequisite context and verification

Modify `frontend/src/types/api.ts`, `frontend/src/lib/careers.ts`, and
`frontend/src/app/(app)/careers/[slug]/page.tsx`. Fetch the career prerequisite
batch from the existing server-only API client. Show required foundations,
recorded/missing state and the self-report/DEMO DATA boundary using semantic
details and text; retain existing route loading/error behavior.

- [ ] Run lint and production build.
- [ ] Browser-check missing and satisfied prerequisites and console warnings.
- [ ] Update API/database/architecture docs and `docs/HACKATHON_PROGRESS.md`.
- [ ] Run full backend suite and `git diff --check`; review changed files.
- [ ] Commit and report Phase 1 verification before starting Phase 2.
