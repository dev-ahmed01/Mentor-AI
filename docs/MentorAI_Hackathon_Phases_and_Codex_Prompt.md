# MentorAI — Hackathon Implementation Phases

> **For agentic workers:** implement this plan phase-by-phase. Do not skip verification gates. Do not rewrite working Phase 1/2 code merely to fit a preferred architecture.

**Goal:** Evolve the existing MentorAI repository into an evidence-backed, adaptive career decision engine that tells a student what to learn now, what to postpone, how their choices affect opportunity, and how their roadmap should adapt to real weekly life constraints.

**Architecture:** Preserve the current modular-monolith architecture: Next.js frontend → Spring Boot REST API → PostgreSQL. Deterministic services own scores, priorities, matching, permissions, progress, and schedule adaptation. AI is introduced only after deterministic decision and evidence layers exist, and is used primarily for explanation, summarization, conversational mentoring, and structured interpretation.

**Tech Stack:** Java 25, Spring Boot, Maven, Spring Data JPA, Spring Security/JWT, Bean Validation, Flyway, PostgreSQL, Next.js 16, React 19, TypeScript, Tailwind CSS 4. Later: Spring AI, Ollama, pgvector/RAG only where justified.

**Source of truth:** `CONTEXT.md`, `DESIGN.md`, this document, and the existing working repository.

---

## 0. Existing System — Preserve This Baseline

The repository already contains working Phase 1 and substantial Phase 2 functionality. Codex must inspect and preserve it before making changes.

### Existing backend modules

- `com.mentorai.auth` — registration, login, JWT, current user.
- `com.mentorai.profile` — student profile and profile editing.
- `com.mentorai.skills` — normalized skills and student skills.
- `com.mentorai.career` — career catalog, career details, deterministic career fit analysis, alternatives, skill gaps.
- `com.mentorai.common` — security, request IDs, exception handling.

### Existing frontend routes

- `/`
- `/login`
- `/register`
- `/onboarding`
- `/dashboard`
- `/profile`
- `/careers`
- `/careers/[slug]`

### Existing tests

- `backend/src/test/java/com/mentorai/auth/AuthAndProfileIntegrationTest.java`
- `backend/src/test/java/com/mentorai/career/CareerApiIntegrationTest.java`

### Existing migrations

- `V1__foundation.sql`
- `V2__career_catalog.sql`

### Critical existing behavior that must not regress

1. Authentication and ownership/security checks.
2. Profile onboarding and editing.
3. Normalized student skill storage.
4. Career catalog and career detail pages.
5. Deterministic career fit scoring.
6. Skill-gap output.
7. Alternative career suggestions.
8. Explicit handling of missing market evidence instead of invented statistics.
9. Existing UX language that distinguishes evidence, calculated metrics, recommendations, and uncertainty.

---

# Global Engineering Rules

These apply to every phase.

## Preserve-first rule

Before editing a module:

1. Read the relevant existing files.
2. Identify current public APIs and UI behavior.
3. Add or update tests that describe intended behavior.
4. Make the smallest compatible change.
5. Do not rename or move working classes/routes unless necessary.
6. Do not replace a working implementation just because another design is preferred.

## Verification gate

No phase is complete until all applicable commands pass locally.

### Backend

```powershell
cd backend
.\mvnw.cmd test
```

If Maven is already installed, `mvn test` is acceptable.

### Frontend

```powershell
cd frontend
npm ci
npm run lint
npm run build
```

### Repository

```powershell
git status
git diff --check
```

Do not proceed with known compile errors, failing tests, TypeScript/build errors, migration errors, React warnings introduced by the phase, or unresolved merge conflicts.

## Database rules

- Use Flyway migrations.
- Never modify an already-applied migration to change production behavior; add a new migration.
- Normalize skills and relationships.
- Add indexes/constraints deliberately.
- No destructive reset of user data for normal development.

## Deterministic-vs-AI rule

Use deterministic application code for:

- career scores
- skill priorities
- prerequisite eligibility
- opportunity-unlock calculations
- progress percentages
- weekly schedule adaptation
- job matching
- readiness indicators
- filtering
- ownership/authorization
- evidence aggregation

Use AI for:

- explaining calculated decisions
- summarizing weekly check-ins
- extracting structured fields from unstructured job text when necessary
- conversational mentoring
- translating evidence into understandable language

The LLM must never invent a score or silently change a student's plan.

## Evidence rule

Never invent:

- job counts
- salaries
- percentages
- market demand claims
- company demand
- hiring trends
- opportunity-unlock numbers

Synthetic/demo data must be clearly labeled `DEMO DATA`.

## Accessibility and UX

Maintain WCAG-oriented practices already defined in `DESIGN.md`: keyboard navigation, semantic HTML, visible focus, accessible forms, readable contrast, responsive layouts, meaningful loading/error/empty states, and no color-only meaning.

## Commit rule

Commit after each independently working phase or meaningful sub-phase. Keep commits small enough to revert safely.

---

# Phase 0 — Protect and Verify the Current Baseline

**Goal:** Turn the current Phase 2 work into a verified checkpoint before adding new features.

### Required actions

- Read `CONTEXT.md`, `DESIGN.md`, `docs/IMPLEMENTATION_PLAN.md`, and existing career docs.
- Inspect `git status` and preserve all current work.
- Fix existing React list-key warnings in `CandidateResult` or related career components.
- Run backend tests.
- Run frontend lint and production build.
- Verify auth → onboarding → dashboard → careers → career detail manually.
- Confirm existing migrations apply to a clean development database.
- Update docs only where they no longer match current behavior.
- Commit the current deterministic-career-intelligence baseline.

### Suggested commit

```text
feat: complete deterministic career intelligence baseline
```

### Stop condition

Do not begin Phase 1 below until the repository has a clean, verified checkpoint.

---

# Phase 1 — Skill Dependency Graph

**Goal:** Represent not only which skills a career needs, but which skills depend on which other skills.

This becomes the foundation for `LEARN_NOW`, `LEARN_NEXT`, `LEARN_LATER`, and `NOT_YET`.

## Backend design

Add a focused dependency model under the existing `skills` module.

Suggested files:

```text
backend/src/main/java/com/mentorai/skills/entity/SkillDependency.java
backend/src/main/java/com/mentorai/skills/repository/SkillDependencyRepository.java
backend/src/main/java/com/mentorai/skills/service/SkillDependencyService.java
backend/src/main/java/com/mentorai/skills/dto/SkillDependencyResponse.java
backend/src/main/resources/db/migration/V3__skill_dependencies.sql
```

Minimum relationship:

```text
SkillDependency
- id
- skill_id
- prerequisite_skill_id
- importance / dependency_strength
```

Rules:

- A skill cannot depend on itself.
- Prevent duplicate `(skill, prerequisite)` edges.
- Prevent obvious cycles during write/seed validation.
- Dependencies are reusable across careers.

Seed a small, defensible graph for hackathon demo paths, for example:

```text
Java -> Spring Fundamentals -> Spring Boot -> REST APIs -> Testing -> Docker
SQL -> Database Integration
HTML/CSS -> JavaScript -> React
Python -> Statistics/Data Handling -> ML Foundations
```

Do not attempt to model the entire technology ecosystem.

## API

Add read-only endpoints needed by the frontend/decision engine, for example:

```text
GET /api/skills/{id}/dependencies
GET /api/skills/{id}/prerequisites
```

Exact endpoint shape may follow existing API conventions.

## Tests

Add integration/unit coverage for:

- dependency retrieval
- duplicate-edge prevention
- self-dependency rejection
- eligibility calculation for a student who has/does not have prerequisites

## Frontend

Do not build a huge graph visualization yet. Add minimal dependency context where useful on career/skill views.

### Definition of done

A student's known skills can be compared against a target skill's prerequisites deterministically.

---

# Phase 2 — Career Decision Engine: Learn Now / Next / Later / Not Yet

**Goal:** Answer the core MentorAI question:

> What is the highest-value next learning action this student can realistically take?

## New backend module

Create a focused module instead of stuffing logic into `CareerAnalysisService`.

Suggested structure:

```text
com.mentorai.decision/
  dto/
  service/
  controller/
```

Suggested concepts:

```text
LearningPriority
- LEARN_NOW
- LEARN_NEXT
- LEARN_LATER
- NOT_YET

LearningDecision
- skill
- priority
- deterministicScore
- prerequisiteReadiness
- careerRelevance
- currentProficiency
- estimatedEffortBand
- reasonCodes
- evidenceStatus
```

## Decision factors

Start with factors that can be supported by current data:

- target career relevance
- required vs preferred career skill
- student current proficiency
- prerequisite readiness
- learning distance
- student's weekly time availability
- whether another missing skill is a prerequisite bottleneck

Reserve market weight until real market evidence exists.

### Example

For a Java student targeting backend:

```text
Spring Boot -> LEARN_NOW
REST APIs -> LEARN_NEXT
Testing -> LEARN_LATER
Docker -> LEARN_LATER
Kubernetes -> NOT_YET
```

`NOT_YET` must never mean "bad skill". It means lower present value or missing prerequisites for this user's current goal.

## Transparency

Every decision must return reason codes that the UI can explain without calling an LLM.

Examples:

```text
TARGET_CAREER_REQUIRED_SKILL
PREREQUISITES_MET
PREREQUISITES_MISSING
ALREADY_PROFICIENT
LOW_CURRENT_RELEVANCE
TIME_BUDGET_CONSTRAINT
MARKET_EVIDENCE_UNAVAILABLE
```

## API

Suggested endpoint:

```text
GET /api/decisions/learning-priorities
```

or a target-career scoped equivalent.

## Frontend

Create a dedicated **Next Best Action** section on the dashboard.

Display:

- Learn now
- Learn next
- Later
- Not yet
- Why
- prerequisite state
- evidence availability

Do not display fake market percentages.

## Tests

Cover at least:

1. missing critical prerequisite => `NOT_YET`
2. prerequisites met + required career skill => high priority
3. already proficient skill => not recommended again
4. preferred skill ranks below required skill where other factors are equal
5. missing market evidence does not fabricate a market score
6. time constraint reduces workload/priority where appropriate

### Definition of done

The system can explain, deterministically, why one skill should be learned before another.

---

# Phase 3 — Roadmap Domain and Personalized Roadmap Generation

**Goal:** Convert the decision engine into an ordered, editable career roadmap without overwhelming the student.

## Backend

Add:

```text
roadmap/
  entity/Roadmap.java
  entity/RoadmapPhase.java
  entity/RoadmapTask.java
  repository/...
  service/RoadmapService.java
  controller/RoadmapController.java
  dto/...
```

Add a Flyway migration.

Minimum structure:

```text
Roadmap
  -> RoadmapPhase
      -> RoadmapTask
```

Tasks should reference skills where appropriate.

### Required roadmap concepts

- current phase
- current priority
- next action
- later skills
- task state
- estimated effort
- target career
- version/revision metadata

Task states:

```text
NOT_STARTED
IN_PROGRESS
COMPLETED
SKIPPED
NEEDS_REVIEW
```

## Generation

Roadmap ordering should come from:

1. decision engine
2. skill prerequisites
3. career requirement importance
4. current student proficiency
5. weekly time availability

AI is not required to order tasks.

## API

Support at minimum:

```text
POST /api/roadmaps
GET /api/roadmaps/current
GET /api/roadmaps/{id}
PUT /api/roadmaps/{id}
```

Follow existing ownership rules.

## Frontend

Create `/roadmap`.

Prioritize clarity:

```text
Current phase
Next action
This week
Later
Completed
Why this order?
```

Do not display every possible technology.

## Tests

Cover ownership, generation order, prerequisite sequencing, existing-skill skipping, and task-state transitions.

---

# Phase 4 — Weekly Progress and Life-Aware Check-In

**Goal:** Make MentorAI work for students whose real lives do not follow a perfect schedule.

This is a signature feature, not a generic checkbox tracker.

## Data model

Add concepts such as:

```text
WeeklyPlan
WeeklyCheckIn
WeeklyTaskProgress
TemporaryConstraint
```

Suggested captured state:

```text
weekStart
plannedHours
actualHours
availableHoursNextWeek
completedTasks
partialTasks
missedTasks
blockers
difficultyRating
confidenceRating
energyOrCapacityBand
notes
constraintType
constraintStart
constraintEnd
```

Constraint examples:

```text
EXAMS
ASSIGNMENTS
INTERNSHIP
HEALTH_OR_PERSONAL
TRAVEL
PLACEMENT_PREP
OTHER
```

Keep sensitive/free-text collection minimal; a student should be able to use generic categories without explaining private circumstances.

## Weekly check-in questions

At minimum:

- What did you complete?
- What is partially complete?
- What did you miss?
- How many hours did you actually spend?
- What blocked you?
- How confident do you feel about the current topic?
- How many hours can you realistically spend next week?
- Do you have a temporary constraint such as exams?

## API

Suggested endpoints:

```text
GET /api/check-ins/current
POST /api/check-ins
GET /api/check-ins/history
GET /api/weekly-plan/current
```

## Frontend

Add a weekly check-in flow that takes roughly 1–2 minutes.

Do not shame users with streak-loss language.

Show:

```text
Planned vs actual
Completed
Carried forward
Removed/deferred
Next week's realistic plan
Why the plan changed
```

## Tests

Cover:

- partial completion
- missed week
- low available time
- high available time
- temporary constraint
- ownership
- invalid negative hours
- check-in submitted twice for same period according to chosen policy

---

# Phase 5 — Adaptive Roadmap Engine

**Goal:** Automatically adjust pacing and sequencing after weekly check-ins without changing the user's career goal unnecessarily.

## Core principle

> Keep the goal stable where reasonable; adapt the path and pace to reality.

## Deterministic adaptation rules

Examples:

### Fell behind

```text
planned = 10h
actual = 4h
```

Result:

- preserve highest-priority unfinished prerequisite
- reduce next-week task count
- push non-critical tasks later
- do not stack all missed work on top of the new week

### Ahead of plan

- mark genuinely completed tasks
- unlock prerequisite-dependent tasks
- move the next relevant task forward

### Repeated postponement

After a defined number of deferrals, surface a blocker question instead of repeatedly reassigning the same task.

### Exams / temporary constraint

Introduce **Maintenance Mode**:

```text
Normal capacity: 10h/week
Temporary capacity: 2h/week
New major learning: paused
Maintenance/revision: lightweight
Resume point: preserved
```

The user should be able to accept or edit the revised plan.

## Revision history

Persist enough history to explain:

```text
what changed
why it changed
when it changed
what triggered it
```

Do not silently rewrite the roadmap.

## UI

Show a concise change summary:

```text
REST moved to next week
Testing moved to Week 6
Docker remains Later
Reason: exam-week capacity reduced from 10h to 3h
```

## Tests

Use scenario-based tests for behind/ahead/exam/repeated-deferral/skill-already-known cases.

---

# Phase 6 — Opportunity Unlock Simulator

**Goal:** Let the student ask: **“What happens if I learn this skill?”**

This is one of the main hackathon demo moments.

## Initial version — no fake live market numbers

Before market ingestion exists, calculate opportunity impact only from the verified internal career/skill graph.

Example:

```text
If you learn Spring Boot:
- Backend path readiness increases
- REST API learning becomes eligible
- X internal career requirements move from missing to satisfied
```

Do not say `+126 jobs` until actual job evidence supports it.

## API

Suggested:

```text
POST /api/simulator/skill
```

Input:

```json
{
  "skillId": "...",
  "targetCareerId": "..."
}
```

Return before/after deterministic state.

## Frontend

Create a side-by-side or before/after interaction:

```text
Current state
   ↓
Simulate learning Spring Boot
   ↓
Updated skill coverage
Unlocked dependencies
Changed priority list
```

Clearly label simulated results.

## Tests

Simulator must be read-only: it must not mutate the real student profile or roadmap.

---

# Phase 7 — Market Intelligence and Evidence Layer

**Goal:** Add real, source-traceable market evidence and only then incorporate market compatibility into decision scoring.

## Architecture

Create replaceable provider interfaces rather than coupling the application to one scraper/source.

```text
MarketDataProvider
MarketIngestionService
MarketNormalizationService
MarketAnalyticsService
```

Pipeline:

```text
Public/allowed source
-> raw observation
-> validation
-> normalization
-> deduplication
-> skill extraction
-> stored observation
-> aggregated snapshot
```

## Provenance

Every market observation should retain fields equivalent to:

```text
source
sourceUrl
collectedAt
region
career/job title
required/preferred skills
processingVersion
```

## Ethics/safety

Do not bypass authentication, CAPTCHAs, paywalls, access controls, or site restrictions. Respect rate limits and source terms. Prefer legitimate APIs/public sources where possible.

## Freshness

Every market UI must show:

- last updated
- data window
- sample size
- source context

If sample size is inadequate, say so.

## Integrate with decision engine

Only after validated market data exists:

- enable the reserved market compatibility component of career/skill decisions
- store scoring version
- ensure results remain reproducible

## Tests

Include aggregation math, deduplication, stale data behavior, insufficient sample size, and source provenance.

---

# Phase 8 — Job Description Analysis and Job Matching

**Goal:** Compare a real job description against the student's current state.

## Job analysis

Input: pasted job description.

Extract or store:

- title
- responsibilities
- required skills
- preferred skills
- experience expectations
- location
- technologies

Output:

- matched skills
- missing required skills
- missing preferred skills
- partial skills
- deterministic job match indicator
- recommended preparation priorities

Required and preferred skills must not receive equal penalties.

## AI usage

If AI is used for extraction, require structured validated output. The deterministic matcher calculates the score.

## Frontend

Add `/jobs/analyze` and later `/jobs` if real job records exist.

---

# Phase 9 — Responsible AI Mentor

**Goal:** Add a conversational mentor that explains and works with the intelligence already produced by the system.

Only begin this phase after the decision engine, roadmap, weekly adaptation, and evidence contracts are stable.

## Architecture

Use Spring AI with an abstraction such as:

```text
AiProvider
OllamaAiProvider
MentorContextService
MentorService
```

Do not scatter model calls across controllers/services.

## Mentor context

Retrieve only relevant information:

- profile
- target career
- current learning priorities
- current roadmap phase
- weekly state
- progress
- relevant market evidence
- relevant job analysis

Do not send the entire database or all conversation history.

## Mentor examples

Question:

> Should I learn Kubernetes now?

The model should receive the deterministic decision result and explain it. It must not override the priority engine with an invented score.

Question:

> I have exams for two weeks. What should I do?

The mentor may summarize the student's request and propose a constraint/check-in update, but the deterministic adaptation engine should produce the actual revised plan. Require explicit user confirmation before persisting consequential plan changes where appropriate.

## Memory

Store conversations, but use recent context + summarized memory + relevant profile state rather than full history.

## Hallucination control

Market claims require evidence. If absent, say `Insufficient market evidence available.`

Treat retrieved job/market text as untrusted data and defend against prompt injection.

---

# Phase 10 — Career Pivoting

**Goal:** Allow the student's interests to change without restarting from zero.

Example:

```text
Backend Developer -> Data Engineer
```

Calculate:

- transferable skills
- newly required skills
- already satisfied prerequisites
- skills that can be skipped
- changed learning priorities
- approximate transition effort band
- revised roadmap

Do not delete the old roadmap/history; preserve revision/audit context.

---

# Phase 11 — Demo Hardening, Final Quality, Security, Documentation and Deployment

**Goal:** Deliver a memorable 3–5 minute judge experience and a reproducible, verified project ready for deployment.

This final phase combines the original demo-hardening and final-quality phases.
Treat the demo, quality/security checks, documentation and deployment preparation
as one implementation phase with one completion report and verified checkpoint.

## Locked demo story

Use one realistic student persona, clearly marked synthetic/demo.

Example:

```text
Second-year BCA student
Knows: Java, SQL, Git
Target: Backend Development
Available: 8h/week
```

Demo sequence:

1. Show profile/context.
2. Show career fit/reality check.
3. Show `LEARN_NOW / LATER / NOT_YET`.
4. Ask why Spring Boot is `NOT_YET` in the delivered Backend graph. Kubernetes is outside this modeled career graph; explain that catalog limit rather than inventing its priority.
5. Simulate learning Spring Boot and show what it unlocks.
6. Show weekly plan.
7. Simulate an exam week / reduced capacity.
8. Show the roadmap adapt without losing completed work.
9. Show market evidence if validated data is available.
10. Ask MentorAI a contextual question and show it explaining the deterministic result.

## UI priorities

- Dashboard first.
- One primary action per section.
- Strong typography and hierarchy.
- Evidence visible but not overwhelming.
- Clear labels for `Observed`, `Calculated`, `AI explanation`, `Simulation`, and `Demo data`.
- Responsive and accessible.

## Demo reliability

Provide deterministic seeded demo state so the live demo does not depend on an external service being available.

If Ollama/AI is unavailable, core dashboard/decision/roadmap/simulation should still work and the UI should show a controlled AI-unavailable state.

The same phase must also complete the following quality, security, documentation
and deployment checks so the result is reproducible beyond the demo.

## Backend

- full test suite
- validation
- authorization/ownership tests
- migration-from-clean-database test
- safe error handling
- no stack traces to clients
- no secrets committed
- prompt-injection tests once AI/RAG exists

## Frontend

- `npm run lint`
- `npm run build`
- loading/error/empty states
- no React key warnings
- no hydration warnings
- keyboard navigation
- mobile sanity check

## Documentation

Update:

```text
README.md
docs/IMPLEMENTATION_PLAN.md
docs/architecture/ARCHITECTURE.md
docs/database/DATABASE.md
docs/api/API.md
docs/ai/AI_ARCHITECTURE.md
```

Document decision formulas/scoring versions and demo-data boundaries.

## Deployment

Deploy only after local verification. Keep environment-specific secrets outside Git. Preserve the ability to run locally.

## Completion gate

- The synthetic demo story is reproducible, including controlled unavailable states.
- Backend tests, clean-database migrations, authorization and prompt-injection checks pass.
- Frontend lint/build and the agreed UI verification scope are complete; explicitly record any unverified browser, keyboard or mobile behavior.
- Architecture, API, database, AI and setup documentation match the delivered application.
- Deployment configuration and local-run instructions are verified. Perform deployment only when authorized and local verification has passed; otherwise report deployment readiness and remaining external steps.
- Report demo readiness and final verification together before checkpointing this final phase.

---

# Hackathon Product Definition — Locked

MentorAI is **not** primarily an AI chatbot and not merely a roadmap generator.

> **MentorAI is an evidence-backed, life-aware career decision engine for students. It combines a student's current abilities, skill dependencies, actual weekly progress, available time, career goals, and validated market signals to determine the highest-value realistic action they can take next — showing what to learn now, what to postpone, what each choice unlocks, and automatically adapting the plan when real life changes.**

The long-term mentor experience sits around this engine.

## Core intelligence loop

```text
STUDENT STATE
+ SKILL GRAPH
+ CAREER REQUIREMENTS
+ ACTUAL WEEKLY PROGRESS
+ REAL-LIFE CAPACITY
+ VALIDATED MARKET EVIDENCE
        ↓
DETERMINISTIC DECISION ENGINE
        ↓
LEARN NOW / NEXT / LATER / NOT YET
        ↓
ROADMAP + WEEKLY PLAN
        ↓
ACTUAL PROGRESS
        ↓
ADAPTATION
        ↓
AI EXPLANATION / MENTORING
```

## Product principle

> The best next action is not simply the most valuable skill in the market; it is the highest-value action this particular student can realistically complete next.

---

# Codex Master Continuation Command

Paste the following into Codex from the repository root after placing this document in the repo (recommended path: `docs/MENTORAI_HACKATHON_PHASES.md`).

```text
You are continuing implementation of the existing MentorAI repository.

DO NOT rebuild the project from scratch.
DO NOT replace the current architecture.
DO NOT discard working code.
DO NOT jump directly to AI/Ollama.
DO NOT claim anything works until you have actually verified it.

SOURCE OF TRUTH
1. CONTEXT.md
2. DESIGN.md
3. docs/MENTORAI_HACKATHON_PHASES.md
4. The current working code and tests

CURRENT ARCHITECTURE TO PRESERVE
- Java 25 + Spring Boot modular monolith backend
- Maven
- Spring Security/JWT
- Spring Data JPA
- Flyway
- PostgreSQL
- Next.js + React + TypeScript + Tailwind frontend
- Existing auth, profile, skills, career catalog, career detail, career-fit scoring, skill-gap and alternative-career behavior

IMPORTANT CURRENT STATE
The repository already contains Phase 1 foundation and substantial Phase 2 career intelligence work. Inspect `git status`, current branch, current diffs, migrations, tests and docs before editing anything. Current uncommitted work must never be lost.

FIRST TASK — BASELINE GATE
1. Read CONTEXT.md completely.
2. Read DESIGN.md completely.
3. Read docs/MENTORAI_HACKATHON_PHASES.md completely.
4. Read the existing docs/IMPLEMENTATION_PLAN.md and architecture/API/database docs.
5. Inspect the entire relevant source tree before proposing changes.
6. Run `git status` and `git diff` and understand all uncommitted work.
7. Run the existing backend test suite.
8. Run frontend lint and production build.
9. Fix existing React `key` warnings in the career candidate rendering if still present.
10. Verify current auth/profile/onboarding/careers behavior.
11. If baseline verification fails, fix the baseline with the smallest safe changes before adding features.
12. Do not delete or reset uncommitted work.
13. Do not advance until the repository is in a verified checkpoint state.

IMPLEMENTATION METHOD
Implement docs/MENTORAI_HACKATHON_PHASES.md sequentially, one phase at a time.

For EACH phase:
A. Inspect relevant existing code first.
B. State the exact files you intend to create/modify and why.
C. Write/extend tests first where practical.
D. Implement the smallest compatible change.
E. Run targeted tests.
F. Run the full backend test suite.
G. Run frontend lint and production build when frontend code changes.
H. Run `git diff --check`.
I. Check for migration issues, React warnings, TypeScript errors, security regressions and API incompatibility.
J. Update only the documentation affected by the phase.
K. Report exactly what was implemented, what was tested, commands/results, and any remaining limitation.
L. Commit the phase only after verification if repository permissions/workflow allow commits.
M. Stop if a phase cannot be made green; fix it before starting the next phase.

NON-NEGOTIABLE PRODUCT RULES
- Deterministic code owns career fit, skill priority, prerequisites, progress, roadmap adaptation, opportunity simulation, job matching and readiness metrics.
- AI may explain deterministic results but must not invent or silently override them.
- Never fabricate salaries, market percentages, job counts, demand, trends or opportunities.
- Label demo/synthetic data explicitly.
- Preserve provenance and freshness when real market data is introduced.
- `NOT_YET` means lower current priority or unmet prerequisites, not that a skill is useless.
- Weekly adaptation must account for imperfect student lives: planned vs actual hours, partial work, blockers, reduced/increased next-week availability, exams/temporary constraints and repeated postponement.
- When students fall behind, reduce/resequence the plan instead of stacking all missed tasks onto the next week.
- When students are ahead, unlock the next valid dependency.
- Preserve completed work and history during roadmap revisions and career pivots.
- Do not introduce microservices, Kafka, Redis, Kubernetes or another database unless a documented requirement makes them necessary.
- Do not introduce Python for the core application.
- Do not add a separate vector database; if/when RAG is needed, follow the existing PostgreSQL/pgvector direction.
- Never weaken backend authorization because the frontend hides a control.

PHASE ORDER
0. Verify/checkpoint existing Phase 1 + Phase 2 work
1. Skill Dependency Graph
2. Career Decision Engine — Learn Now / Next / Later / Not Yet
3. Personalized Roadmap domain/generation
4. Weekly Progress + Life-Aware Check-In
5. Adaptive Roadmap Engine + Maintenance Mode
6. Opportunity Unlock Simulator
7. Market Intelligence + Evidence Layer
8. Job Description Analysis + Job Matching
9. Responsible AI Mentor using deterministic/evidence context
10. Career Pivoting
11. Demo Hardening + Final Quality, Security, Documentation and Deployment (one combined final phase)

DO NOT skip directly to later phases because they look more impressive.

BEGIN NOW WITH THE BASELINE GATE. After baseline verification, continue into Phase 1 only. At the end of Phase 1, provide a verification report before proceeding to Phase 2. Continue phase-by-phase only while the repository remains green.
```

---

# Recommended Git Workflow

Before Codex begins new feature work, verify and checkpoint the current Phase 2 work.

```powershell
cd C:\Users\Admin\Desktop\Mentor-AI

git status

cd backend
.\mvnw.cmd test

cd ..\frontend
npm ci
npm run lint
npm run build

cd ..
git diff --check
```

If all current work is correct:

```powershell
git add .
git commit -m "feat: complete deterministic career intelligence baseline"
```

Then create the next branch:

```powershell
git switch -c feature/adaptive-career-decision-engine
```

Then place this file at:

```text
docs/MENTORAI_HACKATHON_PHASES.md
```

and use the Codex Master Continuation Command above.

---

# Final Definition of Done

The hackathon-ready MentorAI should let a student:

1. Register and create a profile.
2. Enter education, interests, goals, skills and realistic weekly availability.
3. Compare plausible career directions.
4. See a career reality check.
5. Understand skill gaps and prerequisites.
6. See `Learn Now`, `Learn Next`, `Learn Later`, and `Not Yet` with reasons.
7. Generate an ordered roadmap.
8. Record actual weekly progress and constraints.
9. Have the roadmap adapt to exams, missed work, faster progress and changing capacity.
10. Simulate what learning a skill would unlock without mutating real progress.
11. See real market evidence only when validated evidence exists.
12. Analyze a real job and compare required/preferred skills.
13. Ask MentorAI contextual questions and receive explanations grounded in deterministic state/evidence.
14. Pivot careers without losing transferable progress.
15. Continue using the core product even if the AI model is temporarily unavailable.

That is the version of MentorAI to build.
