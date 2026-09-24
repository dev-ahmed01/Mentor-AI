# Learning Priorities Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Explain the next learning focus for an explicitly selected career.

**Architecture:** Add a read-only decision module using the existing career catalog,
profile and prerequisite graph. A pure policy ranks candidates; a server-rendered
dashboard shows four groups with a GET career selector. No schema change needed.

**Tech Stack:** Java 25, Spring Boot, existing JPA/Flyway, Next.js 16, React 19.

**Spec:** `docs/MentorAI_Hackathon_Phases_and_Codex_Prompt.md`, Phase 2.

## Policy and constraints

- Preserve existing career-fit formulas, API contracts and auth ownership.
- Candidate scope: selected career skills plus all modeled ancestors. Never
  enumerate unrelated user-created skills as recommendations.
- Direct career skill learning target: INTERMEDIATE. Foundation-only target:
  BEGINNER. Both are illustrative self-report thresholds, not assessed competence.
- Missing prerequisites force NOT_YET. A target already met also receives
  NOT_YET with ALREADY_PROFICIENT; do not recommend learning it again.
- Relevance base: required 50, preferred 25. Foundations inherit the strongest
  dependent requirement and importance; direct skills retain their own relevance
  unless they are an unmet foundation for a required skill.
- Score = min(100, relevance + 4 * importance + min(20, 5 * missing dependent
  career skills unlocked) + closeness). Closeness is 10 for beginner, 5 for
  awareness, 0 for absent. Already-met targets score 0. This is an ordering
  heuristic, not a percentage or market score. Stable ties: name then UUID.
- All modeled ancestors must be BEGINNER or higher. Bottleneck bonus applies
  only while the candidate is below BEGINNER; completed downstream skills do
  not count as blocked work.
- Weekly focus slots: unknown/zero = 0, 1-4 hours = 1, 5-9 = 2, 10+ = 3.
  Highest-ranked eligible candidates fill LEARN_NOW, next two LEARN_NEXT,
  remaining eligible LEARN_LATER. These are focus limits, not promised weekly
  completion or time estimates. Unknown time gets an explicit reason.
- Effort bands describe self-reported learning distance: FOUNDATIONS for absent
  or awareness, DEVELOPING for beginner direct skills, TARGET_MET otherwise.
- Every decision has reasons, prerequisite state, learning distance, current and
  target proficiency, score, evidence status. Market weight remains zero and
  no market score is returned. Label graph/heuristic DEMO DATA.
- Target is a query parameter, not a persisted career commitment. Missing or
  invalid selection shows a selector/help message without guessing a career.

## Task 1: Authenticated decision API

Create `backend/src/main/java/com/mentorai/decision/`:
- `dto/LearningPriority.java`: four priority values.
- `dto/LearningDecision.java`: one skill, its deterministic factors and reasons.
- `dto/LearningPrioritiesResponse.java`: target, version, capacity and decisions.
- `service/LearningPriorityPolicy.java`: pure ranking and classification.
- `service/LearningDecisionService.java`: profile/catalog/prerequisite assembly.
- `controller/LearningDecisionController.java`: GET
  `/api/decisions/learning-priorities?careerId=UUID`, caller identity from auth.

Modify `skills/service/SkillDependencyService.java` to expose a batch including
career foundations, using the same readiness implementation as Phase 1.

- [x] Write `backend/src/test/java/com/mentorai/decision/LearningDecisionApiIntegrationTest.java`.
  Assert Spring Boot is blocked for an empty profile, Java/Spring Fundamentals
  remove the block, proficient skills are not repeated, low/unknown time limits
  now slots, repeated requests are identical and never mutate the profile,
  different users remain isolated, invalid/missing career is 400/404, unauth is 401.
- [x] Run the API test before implementation and observe the absent-endpoint failure.
- [x] Write `LearningPriorityPolicyTest.java` for equal-factor required/preferred
  ordering, stable ties, completed skill exclusion and capacity boundaries.
- [x] Implement DTOs, policy, service and controller; run targeted then full suite.

```powershell
$env:JAVA_HOME=Join-Path $env:TEMP 'mentorai-jdk25-25.0.4.1\jdk-25.0.4.1+1'
.\mvnw.cmd '-Dmaven.repo.local=C:\Users\Admin\.m2\repository' -o test
```

## Task 2: Dashboard Next Best Action

Create `frontend/src/components/LearningPriorities.tsx` (server component) and
`frontend/src/lib/decisions.ts` (server-only authenticated API reader).
Modify `frontend/src/types/api.ts`, `frontend/src/app/(app)/dashboard/page.tsx`,
`frontend/src/app/globals.css`, and `frontend/src/app/(app)/careers/[slug]/page.tsx`.
The dashboard awaits searchParams, validates careerId against the current
catalog and fetches priorities only for the chosen career. The career detail
links to that selection. A GET form works without browser JS. Four groups have
empty states, readable reason text, native details and existing prerequisite UI.
API failures keep the section's selector and a retry link available.

- [x] Read installed Next.js docs for searchParams and server components.
- [x] Implement types, API reader, component and route integration.
- [x] Run `npm.cmd run lint` and `npm.cmd run build`.
- [x] Browser-check selection, missing/known skills, changed time, empty states,
  keyboard controls, mobile layout and console. Verify result refresh after edit.

## Task 3: Review and checkpoint

- [x] Review auth, factor explanations, deterministic ordering and existing APIs.
- [x] Update README, API/architecture docs, implementation plan, progress report.
- [x] Run `git diff --check`, inspect final diff and commit this phase only.

No Phase 3 roadmap persistence/generation, market ingestion or AI in this change.
