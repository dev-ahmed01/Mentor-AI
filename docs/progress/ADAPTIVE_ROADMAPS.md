# Adaptive roadmap policy

Phase 5 extends weekly progress with deterministic, reviewable revisions. The
policy version is `weekly-adaptation-v1`. It changes weekly pacing and ready-task
sequencing; career goals, profile proficiency, task estimates and roadmap phases
remain as recorded. No model, market data or inferred skill completion is used.

## Proposal and acceptance

Submitting a check-in still records explicit learning outcomes and creates (or
preserves) the Phase 4 next-week allocation. In the same transaction it records a
pending adaptation for that allocation. The student sees the saved allocation,
the suggestion, its reason and any tasks left outside the suggested week.
Nothing replaces the saved allocation until the student accepts or edits it.

Acceptance may use the suggestion unchanged or supply a complete replacement
capacity and ordered task/hour list. Zero capacity and empty weeks are valid.
Normal edits may select ready unfinished tasks, including an explicitly resumed
deferred task, within estimates and total capacity. Maintenance edits allow at
most one review task and two hours. Each proposal can be accepted once.

The owned roadmap is locked for both check-in submission and acceptance. The
proposal's roadmap revision and target-plan revision must still match; callers
also submit both expected revisions. A checked-in or past target week cannot
be revised. Stale forms fail with 409 and keep the saved data intact. A late
check-in never generates a revision for a target on another roadmap.

## Rules

- **Behind:** actual hours below 60% of planned hours, with some work unfinished,
  suggests `min(reported availability, max(1, actual hours))`. Reported zero stays
  zero. Reduce the focus limit to at most one fewer task than the source week,
  with a minimum of one when capacity permits. Ready unfinished prerequisites
  come first, then work in progress, then existing roadmap order. Remaining work
  stays available for later; it is not stacked into a catch-up week.
- **Ahead:** all allocated learning tasks explicitly completed unlock their
  dependents through existing prerequisite rules. Suggest the next ready work
  within reported capacity. Hours alone never complete a task.
- **Repeated deferral:** three DEFERRED records for a task since its latest
  PARTIAL/COMPLETED record pause automatic reassignment and show a blocker
  question. Counts follow planned-week order through the triggering week, so
  late submissions do not use future reflections. The student can deliberately
  include the task using the edit form; no private explanation is required.
- **Maintenance:** a recorded temporary constraint overlapping any day of the
  target Monday–Sunday suggests at most two hours and one review activity.
  Only in-progress, needs-review, completed or already-known tasks are reviewable.
  With no such task, suggest an empty week and preserve the next ready learning
  task as a resume point. Review outcomes never change roadmap completion.
- **Resume:** once no recorded constraint overlaps the target week, a maintenance
  source week returns to normal learning at the latest reported capacity,
  including when the review activity was missed. The student still accepts the
  proposal. Capacity is not silently restored from an older profile value.

The automatic normal focus limit reuses the existing one/two/three-task policy.
Students can choose up to 20 ready tasks in a normal edited allocation, always
within the weekly budget and each task's estimate. Maintenance limits stay fixed.
Constraints remain active through their recorded end date; this phase does not
add an early-cancellation editor. Skill readiness remains the saved roadmap's
prerequisite/proficiency snapshot plus explicit completion, as in Phase 3.

## History and compatibility

Each revision records its trigger check-in, policy version, creation/acceptance
time, original allocation, proposed allocation, accepted allocation, reason,
resume point and blocker questions. Snapshots retain task titles and hours even
after later roadmap edits. Owner-only history is paginated in groups of 20.
Weekly history also exposes each reflection's revision and accepted snapshot.

V6 adds weekly-plan `revision` (initially 0), `mode` (initially NORMAL), and the
revision audit table. V1–V5 are unchanged. Check-in clients should send
`expectedPlanRevision`; omission means 0 for compatibility with original plans.
An older client must refresh/update before submitting an accepted, revised plan.
Revision history uses immutable text JSON snapshots for policy-versioned values
and relational owner/plan/check-in/roadmap foreign keys for identity.

Interactive browser verification is outside the user-selected API/build scope.
