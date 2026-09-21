# Weekly Check-ins Implementation Plan

> **For agentic workers:** Use superpowers:subagent-driven-development or executing-plans. Track steps below.

**Goal:** Persist realistic weekly plans and life-aware check-ins without changing career goals.
**Architecture:** A progress module snapshots weekly allocations from an owned roadmap. Atomic check-in submission records outcomes, applies explicit completion/partial updates with roadmap revision protection, and saves next week's capacity allocation. Server-rendered progress UI uses authenticated Server Actions.
**Tech Stack:** Java 25, Spring Boot/JPA, Flyway/PostgreSQL, Next.js 16/React.
**Spec:** Phase 4 in `docs/MentorAI_Hackathon_Phases_and_Codex_Prompt.md`.

## Global constraints and decisions

- Preserve existing career/profile/roadmap APIs and data. Add V5 only. No AI or market claims.
- Continue in the existing clean feature checkout; baseline is `b1ee26c` with 34 passing backend tests on H2 and PostgreSQL.
- Weeks start Monday in UTC; inject a Clock for deterministic rollover tests.
- One plan per authenticated user/week; explicit POST creates only this week's plan, using owned roadmap ID. GET never creates data. Duplicate plan/check-in returns 409. Database uniqueness backs this policy.
- Plans contain ordered task/title/hour snapshots, capacityHours and summed plannedHours. Allocation uses ready unfinished roadmap tasks, in-progress first, existing order otherwise, same focus limits as Phase 2. Zero next-week capacity is valid and produces an empty restful plan. Do not inflate hours or infer proficiency.
- Check-ins accept exactly one outcome for every planned task: COMPLETED, PARTIAL, MISSED or DEFERRED. COMPLETED and PARTIAL are explicit roadmap edits to COMPLETED/IN_PROGRESS through existing transition/prerequisite validation, atomically with check-in persistence and expectedRoadmapRevision. MISSED/DEFERRED never change roadmap state. All require owned task membership. Negative/fractional/>168 hours invalid.
- Partial/missed work remains eligible for next week; deferred tasks are excluded only from that next allocation, not deleted/skipped in the roadmap. Completed work is excluded. No estimated-effort deduction or automatic sequencing changes.
- Optional difficulty/confidence ratings 1–5, required energy LOW/MEDIUM/HIGH. Blockers are categories NO_TIME, TOO_DIFFICULT, UNCLEAR_NEXT_STEP, RESOURCE_ACCESS, OTHER. Optional notes max 500, no private detail required.
- Optional constraint object type EXAMS/ASSIGNMENTS/INTERNSHIP/HEALTH_OR_PERSONAL/TRAVEL/PLACEMENT_PREP/OTHER and ordered start/end dates, maximum 366 days. Store generic categories only; capacity is the student's explicit next-week number, never inferred from health/energy.
- Current/past persisted plans may be checked in, never future plans. History exposes unsubmitted plans so missed weeks can be recorded. An existing next-week plan is preserved unchanged on late submission, with an explanation; never rewrite another snapshot.
- Full check-in immutable; duplicate returns safe 409 without roadmap changes. Next week's plan and check-in are saved in the same transaction. Existing roadmap optimistic lock protects concurrent changes. Pagination bounds history to 20 plans per page.

## API contract shared by backend and frontend

`POST /api/weekly-plan` request `{roadmapId}` -> 201 WeeklyPlan.
`GET /api/weekly-plan/current?weekStart=YYYY-MM-DD` -> 200 WeeklyPlan; absent 404. Optional date must be Monday and <= current week. Omitted means current week.
`GET /api/check-ins/current?weekStart=YYYY-MM-DD` -> 200 CheckIn or 404, same date rules.
`GET /api/check-ins/history?page=0` -> `{items: [{plan: WeeklyPlan, checkIn: CheckIn|null}], page, hasNext}` newest week first, 20 items, user owned.
`POST /api/check-ins` request below -> 201 CheckIn. GET responses and writes require bearer auth.

```typescript
type WeeklyTask = {taskId:string; title:string; plannedHours:number};
type WeeklyPlan = {id:string; roadmapId:string; roadmapTitle:string; weekStart:string;
  capacityHours:number; plannedHours:number; roadmapRevision:number; tasks:WeeklyTask[];
  reason:string; createdAt:string};
type Constraint = {type:string; startDate:string; endDate:string};
type CheckInRequest = {planId:string; expectedRoadmapRevision:number; actualHours:number;
  availableHoursNextWeek:number; difficultyRating:number|null; confidenceRating:number|null;
  energyOrCapacityBand:string; blockers:string[]; notes:string|null; constraint:Constraint|null;
  tasks:{taskId:string; outcome:string}[]};
type CheckIn = {id:string; planId:string; roadmapId:string; weekStart:string;
  plannedHours:number; actualHours:number; availableHoursNextWeek:number;
  difficultyRating:number|null; confidenceRating:number|null; energyOrCapacityBand:string;
  blockers:string[]; notes:string|null; constraint:Constraint|null;
  tasks:{taskId:string; title:string; outcome:string}[]; nextPlan:WeeklyPlan;
  explanation:string; createdAt:string};
```

WeeklyPlan.roadmapRevision is the live revision on reads to protect the form; all allocation/title fields are snapshots. A saved check-in's nextPlan allocation stays fixed, while its live revision may advance. When next week exists already, return that owned plan and explain preservation. Safe errors use existing 400 VALIDATION_FAILED / 404 RESOURCE_NOT_FOUND / 409 RESOURCE_CONFLICT contracts.

## Task 1: Backend persistence, policy, APIs and tests

**Files:** Create `backend/src/main/java/com/mentorai/progress/{entity,dto,repository,service,controller}/`, `V5__weekly_progress.sql`, `backend/src/test/java/com/mentorai/progress/WeeklyProgressIntegrationTest.java`; extend GlobalExceptionHandler for progress validation.
**Consumes:** RoadmapService.get/update, RoadmapResponse tasks/readiness/revision, authenticated user, LearningPriorityPolicy.focusSlots.
**Produces:** Exact JSON contracts above; no frontend edits.

- [x] Write failing API tests and observe missing endpoints: snapshot persistence, partial, missed week with injected clock, 0/1/20/168 next-hour capacity, temporary constraint, history/ownership, negative hours, invalid dates/ratings/task IDs/duplicate outcomes, duplicate check-in, stale revision and atomic rollback, existing next-week plan preservation, auth and unchanged profile.
- [x] Add constrained normalized plan, plan-task, check-in, task-progress, blocker and temporary-constraint tables. Use UUID/FKs/unique owner-week and plan-check-in keys; persist created times at microsecond precision.
- [x] Implement pure allocation policy and transactional service/controller/DTOs per contract. Clock default UTC. No request or private-note logging.
- [x] Run targeted then full H2 suite. Root verifies PostgreSQL and upgrade. Report concrete tests/results and deviations in `backend/target/phase4-backend-report.md`; do not commit or spawn agents.

## Task 2: Progress frontend

**Files:** Create `frontend/src/types/progress.ts`, `lib/progress.ts`, `app/actions/progress.ts`, `components/WeeklyCheckInForm.tsx`, `components/WeeklyProgressView.tsx`, `app/(app)/progress/{page,loading}.tsx`; update AppNav, dashboard, RoadmapView and globals.css.
**Consumes:** Contract above; existing server bearer client/getRoadmap.
**Produces:** `/progress` with current/historical week selection, explicit Start this week, short check-in form, submitted summary and next-plan explanation.

- [x] Read installed Next forms/server-component guides. Use controlled inputs and useActionState preserving errors; bind plan ID/revision/task IDs, backend independently validates ownership.
- [x] Show planned versus actual, completed, carried forward, deferred, next-week capacity and why changed. Provide 0-hours/missed-week paths and generic constraints without sensitive explanation. No streak/shame language.
- [x] Include loading/empty/error states, accessible labels, mobile layout, keyboard support and pagination links. Unsubmitted history opens its week. No browser tokens or direct database access.
- [x] Run lint/build and verify the API flow and authenticated production server rendering. Interactive browser/mobile/keyboard checks were waived by the user in favor of API/build verification; they are not claimed as passed.

## Task 3: Review, documentation, verification and checkpoint

- [x] Independent code review for Phase 4 correctness, ownership, concurrency and UX; resolve findings.
- [x] Run full H2 and PostgreSQL suites, V4-to-V5 upgrade preserving existing plans/profiles, frontend lint/build and HTTP/server-rendering checks; record the user-approved browser-verification exclusion.
- [x] Update API/database/architecture/progress/README and add progress policy; mark Phase 5 next in implementation plan.
- [x] Check git diff whitespace and commit verified Phase 4 checkpoint on existing feature branch.

## Execution ledger

Ruling: explicit weekly-plan POST added alongside suggested GET to keep reads side-effect-free.
Ruling: basic next-week capacity allocation implements Phase 4's required realistic plan; adaptive sequence/effort policy remains Phase 5.
Preflight: Task 1 produces shared JSON consumed by Task 2, with names/types specified above. Their source files are disjoint. Task 3 validates both and documents actual behavior. No contradictory mutation or ownership contracts identified.

Completed: 44 tests passed on H2 and PostgreSQL, V4-to-V5 data preservation passed, frontend lint/build passed, live HTTP and authenticated SSR passed. Final independent review found no actionable issues. User explicitly selected API/build verification only; interactive browser coverage remains unverified.
