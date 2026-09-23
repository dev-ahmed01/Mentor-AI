# MentorAI API

Base URL: `http://localhost:8080`. Request and response bodies use JSON. Private
routes require `Authorization: Bearer <token>`.

## Authentication

### `POST /api/auth/register`

```json
{
  "displayName": "Demo Student",
  "email": "student@example.com",
  "password": "at-least-12-characters"
}
```

Returns `201` with `accessToken`, `tokenType`, `expiresAt`, and a safe user DTO.
The password is BCrypt-hashed and never returned.

### `POST /api/auth/login`

Accepts `email` and `password`; returns the same authentication response. Invalid
credentials return `401 INVALID_CREDENTIALS` without identifying which field was
wrong.

### `GET /api/auth/me`

Returns the authenticated user’s ID, email, and display name.

## Profile

### `GET /api/profile`

Returns only the authenticated user’s profile, normalized skill relations, and
last update time.

### `PUT /api/profile`

Replaces the authenticated user’s editable profile. Collections are arrays;
skills are structured objects:

```json
{
  "degree": "BCA",
  "year": 2,
  "interests": ["Backend development"],
  "goals": ["Get a software internship"],
  "timeAvailablePerWeek": 12,
  "skills": [{
    "name": "Java",
    "category": "Programming language",
    "proficiency": "INTERMEDIATE",
    "confidence": "MEDIUM",
    "source": "SELF_REPORTED"
  }]
}
```

Supported remote preferences are `REMOTE`, `HYBRID`, `ON_SITE`, and `FLEXIBLE`.
Skill proficiency, confidence, and source use the enum values defined in the API
types. Skill names are trimmed, case-normalized for identity, and deduplicated.

## Careers

All career routes require authentication.

- `GET /api/careers` returns the active controlled catalog in name order.
- `GET /api/careers/{id}` returns a career reality record by UUID.
- `GET /api/careers/by-slug/{slug}` returns the same detail by stable slug.
- `POST /api/careers/analyze?limit=5` calculates three to ten ranked candidates
  from the authenticated student's saved profile.

The analysis response includes `calculationVersion`, configured weights,
available evidence coverage, factor scores, explanations, strengths, prioritized
skill gaps, risks, alternatives, uncertainties, and the next suggested action.
`marketCompatibility` is absent and `marketEvidenceStatus` is
`INSUFFICIENT_MARKET_EVIDENCE` until validated market observations exist. The
number is named `Career Fit Indicator`; it is not an employment probability.

Analysis without any recorded interest, preferred domain, goal, or skill returns
`422 PROFILE_INCOMPLETE`. The optional `limit` must be between 3 and 10.

The exact, versioned calculation is documented in
[Career scoring](../career/SCORING.md).

## Skill prerequisites (hackathon Phase 1)

All endpoints require authentication:

- `GET /api/skills/{id}/dependencies` returns direct prerequisite edges with
  target/prerequisite IDs, prerequisite name, importance (1–5), and `DEMO DATA`.
- `GET /api/skills/{id}/prerequisites` returns the authenticated student's
  readiness against all direct and transitive prerequisites.
- `GET /api/skills/prerequisites?careerId={uuid}` returns readiness for all skills
  of an active career in one request. It does not select a career or change data.

Readiness returns `skillId`, `name`, `eligible`, `coverage`, `minimumProficiency`,
`calculationVersion`, `dataLabel`, and `prerequisites`. Each prerequisite includes
`skillId`, `name`, `direct`, optional `currentProficiency`, and `satisfied`.

`skill-prerequisites-v1` requires every modeled ancestor at `BEGINNER` or higher.
`AWARENESS` and absent skills are insufficient. This is recorded preparation,
not assessed competence. `NO_RECORDED_PREREQUISITES` returns an empty list and
`eligible=true` meaning only that this starter graph imposes no restriction.
`MODELED_PREREQUISITES` means at least one prerequisite is recorded. Graph data
is illustrative `DEMO DATA`; no market claim or career score is calculated here.

Missing/invalid UUID parameters return `400 VALIDATION_FAILED`; unknown skills
or inactive/unknown careers return `404 RESOURCE_NOT_FOUND`. There is no public
graph-write endpoint. A caller cannot supply another student's profile ID.

## Learning priorities (hackathon Phase 2)

`GET /api/decisions/learning-priorities?careerId={uuid}` requires bearer
authentication. The service uses the authenticated profile; there is no student
ID input. It returns the active career's skills plus prerequisite foundations.
It does not persist a target career, change the profile, or create a roadmap.

The response contains `careerId`, `careerName`, `calculationVersion`, `dataLabel`,
optional `weeklyHours`, `immediateFocusLimit`, `marketEvidenceStatus`,
`marketWeight`, and `decisions`. `calculationVersion` is `learning-priorities-v1`,
`dataLabel` is `DEMO DATA`, `marketEvidenceStatus` is `UNAVAILABLE`, and
`marketWeight` is zero. There is no `marketScore`.

Each decision contains `skillId`, `name`, `priority`, `deterministicScore`,
`prerequisiteReadiness` (the existing Phase 1 contract), `careerRelevance`,
`importance`, optional `currentProficiency`, `targetProficiency`,
`learningDistance`, `estimatedEffortBand`, `bottleneckCount`, `reasonCodes`,
and `evidenceStatus=MARKET_EVIDENCE_UNAVAILABLE`.

Priority is LEARN_NOW, LEARN_NEXT, LEARN_LATER, or NOT_YET. Relevance is REQUIRED,
PREFERRED, REQUIRED_FOUNDATION, or PREFERRED_FOUNDATION. Effort bands are
FOUNDATIONS, DEVELOPING, or TARGET_MET; they are not completion-time estimates.
See the [exact policy and reason semantics](../decision/SCORING.md).

Missing/invalid careerId is `400 VALIDATION_FAILED`; unknown/inactive career is
`404 RESOURCE_NOT_FOUND`; unauthenticated requests are 401. Empty profiles are
valid: missing skills and time are explained rather than guessed.

## Roadmaps (hackathon Phase 3)

All routes require bearer authentication and use the authenticated owner. There
is no owner/profile ID input. Unknown or another user's roadmap returns
`404 RESOURCE_NOT_FOUND`.

- `POST /api/roadmaps` accepts `{ "careerId": "<active-career-uuid>" }` and
  returns 201, a Location header and the saved roadmap. Missing weekly
  availability returns `422 PROFILE_INCOMPLETE`. Every call creates a new plan.
- `GET /api/roadmaps/current` returns the latest created plan, or 404 if absent.
- `GET /api/roadmaps/{id}` retrieves a current or older owned plan.
- `PUT /api/roadmaps/{id}` atomically updates optional title and/or task edits,
  returning 200 with the updated roadmap. Example:

```json
{
  "expectedRevision": 0,
  "title": "My backend learning plan",
  "tasks": [{
    "id": "<task-uuid-from-this-roadmap>",
    "title": "Practice Java foundations",
    "estimatedHours": 6,
    "state": "COMPLETED"
  }]
}
```

`expectedRevision` is required and nonnegative. Each supplied task edit requires
all four fields; a request can edit at most 100 distinct tasks. Nonblank titles
are limited to 200 characters. Effort is an integer from 0 to 168; unfinished
tasks require positive hours. There is no delete, reordering or skill-change API.

The response contains `id`, `careerId`, `careerName`, `title`, `revision`,
`generationVersion`, `decisionVersion`, `dataLabel`, audit/profile timestamps,
`previousRoadmapId`, `weeklyHours`, `currentPhaseId`, `currentPriority`,
`nextAction`, `thisWeek` and ordered `phases`. A phase has `id`, `position`,
`title` and `tasks`. Tasks include skill identity/name, title/state/target/effort,
`satisfiedAtGeneration`, `ready`, initial priority, points, ordering reason and
prerequisite records. Weekly entries contain `taskId`, `title`, `plannedHours`.
`nextAction` is a task or null; `currentPhaseId` is null when no work is unfinished.

Malformed bodies, invalid fields and duplicate/foreign task IDs return
`400 VALIDATION_FAILED`. Stale revisions, prohibited transitions and unmet
prerequisites return `409 RESOURCE_CONFLICT`, with no partial writes. Completed tasks can
reopen as NEEDS_REVIEW. Skipping unknown foundations does not unlock dependents.
See [generation/state policy](../roadmap/GENERATION.md) for all transitions and
capacity rules. Completion does not update profile proficiency.

## Weekly progress (hackathon Phase 4)

All routes require authentication and resolve the caller's owned records. Weeks
start Monday in UTC. GET requests do not create plans.

- `POST /api/weekly-plan`, body `{ "roadmapId": "<owned-roadmap-uuid>" }`, saves
  this week's allocation and returns 201 with a Location header and WeeklyPlan.
  Only one plan per user/week; repeat creation returns 409.
- `GET /api/weekly-plan/current` returns WeeklyPlan, or 404 when absent.
- `GET /api/check-ins/current` returns the week's CheckIn, or 404 when absent.
- Both current GET routes accept optional `weekStart=YYYY-MM-DD` for a saved
  earlier week. Dates must be Mondays no later than the current week.
- `GET /api/check-ins/history?page=0` returns `{items, page, hasNext}`. Each item
  contains `plan` and optional `checkIn`; absent check-ins mean unsubmitted plans.
  Pages contain up to 20 plans ordered newest week first, including upcoming
  plans. Page is a nonnegative integer.
- `POST /api/check-ins` returns 201, a Location header and CheckIn. Example:

```json
{
  "planId": "<weekly-plan-uuid>",
  "expectedRoadmapRevision": 0,
  "actualHours": 3,
  "availableHoursNextWeek": 2,
  "difficultyRating": 3,
  "confidenceRating": null,
  "energyOrCapacityBand": "LOW",
  "blockers": ["NO_TIME"],
  "notes": null,
  "constraint": {
    "type": "EXAMS",
    "startDate": "2026-09-21",
    "endDate": "2026-09-27"
  },
  "tasks": [{ "taskId": "<planned-task-uuid>", "outcome": "PARTIAL" }]
}
```

Submit exactly one outcome per planned task: COMPLETED, PARTIAL, MISSED or
DEFERRED. Hours must be whole numbers from 0 through 168, including zero next-week
capacity. Energy is LOW/MEDIUM/HIGH. Optional difficulty/confidence are 1–5.
Blockers accept NO_TIME, TOO_DIFFICULT, UNCLEAR_NEXT_STEP, RESOURCE_ACCESS or OTHER
(up to five). Optional notes have a 500-character limit. Constraint categories:
EXAMS, ASSIGNMENTS, INTERNSHIP, HEALTH_OR_PERSONAL, TRAVEL, PLACEMENT_PREP, OTHER.
Dates must be ordered and span at most 366 days; no private explanation required.

WeeklyPlan contains `id`, `roadmapId`, snapshot `roadmapTitle`, `weekStart`,
`capacityHours`, summed `plannedHours`, live `roadmapRevision`, `tasks`, `reason`,
and `createdAt`. Each task has `taskId`, snapshot `title`, and `plannedHours`.
CheckIn contains identity/week, planned/actual/next-availability hours, the optional
ratings/notes/constraint, energy/blockers, task outcomes with snapshot titles,
`nextPlan`, `explanation`, and `createdAt`. Nullable response fields may be omitted.

Completed/partial outcomes use existing roadmap transition and prerequisite
validation and save atomically with the reflection and next plan. Profile skills
are unchanged. Missed/deferred outcomes retain roadmap states. The next allocation
excludes completed/deferred work and respects reported capacity. An existing
next-week snapshot is preserved, with an explanation, even if the late check-in
reports different availability. The two values remain distinct in the response.

Invalid fields, dates, task membership/outcome coverage, or future submissions
return `400 VALIDATION_FAILED`. Another owner's plan/roadmap returns
`404 RESOURCE_NOT_FOUND`. Duplicate check-ins and stale roadmap revisions return
`409 RESOURCE_CONFLICT` without partial writes. One final check-in per saved
week is supported; there is no edit/delete endpoint. See
[weekly policy](../progress/WEEKLY_CHECK_INS.md).

## Error response format

```json
{
  "timestamp": "2026-08-14T15:00:00Z",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "One or more fields are invalid.",
  "path": "/api/profile",
  "requestId": "c04d5f6d-...",
  "fieldErrors": { "year": "must be less than or equal to 8" }
}
```

Stack traces and internal exception details are never returned. Authentication
and access-denied failures use this same contract.
## Phase 5: adaptive revisions

All routes require a bearer token and owner-scoped lookups. A saved check-in now
includes optional `adaptation`; weekly plans add `revision` and `mode`.
`POST /api/check-ins` accepts `expectedPlanRevision` (omission means 0 for original
Phase 4 plans). Revised plans require their current value. Maintenance check-ins
record review outcomes without modifying roadmap task states.

- `GET /api/adaptations/{id}`: one saved revision, or 404 for absent/foreign IDs.
- `GET /api/roadmaps/{id}/adaptations?page=0`: `{items, page, hasNext}`, 20 revisions
  per page, newest proposal first. Negative pages return 400.
- `POST /api/adaptations/{id}/accept`: accept or edit a pending proposal.

```json
{
  "expectedRoadmapRevision": 2,
  "expectedPlanRevision": 0,
  "edit": {
    "capacityHours": 3,
    "tasks": [{ "taskId": "<owned-ready-task-uuid>", "plannedHours": 3 }]
  }
}
```

Omit `edit` or send null to accept the proposal unchanged. An edit is a complete
replacement allocation, with an ordered task list (maximum 20). Integer capacity
is 0–168; task hours are positive, within the estimate and summed capacity.
Maintenance edits allow at most two hours and one eligible review task. Duplicate,
foreign, blocked or over-budget tasks and fractional hours return 400. Stale
roadmap/plan versions, an already accepted revision, a checked-in target or a past
target week return 409 without changing data.

The 200 response includes `id`, `checkInId`, `roadmapId`, `planId`, `weekStart`,
`status`, `policyVersion`, `trigger`, `reason`, `createdAt`, optional `acceptedAt`,
the original `roadmapRevision`/`planRevision`, `canAccept`, `before`, `proposed`,
optional `accepted`, optional `resumeTaskId`/`resumeTitle`, `blockerQuestions`
and editable `candidates`. Each allocation snapshot contains `capacityHours`,
NORMAL/MAINTENANCE `mode` and task ID/title/planned-hour records. Accepted snapshots
retain the exact student choice; the proposal is never overwritten.

See [adaptation policy](../progress/ADAPTIVE_ROADMAPS.md) for thresholds and dates.
## Phase 6: skill simulation

### `POST /api/simulator/skill`

Authenticated, read-only calculation. No profile, roadmap or progress record is
changed or created. The request has no user ID; it uses the bearer token's owner.

```json
{
  "skillId": "<existing-skill-uuid>",
  "targetCareerId": "<active-career-uuid>"
}
```

Returns 200 with `calculationVersion: skill-simulation-v1`,
`resultLabel: SIMULATION`, `dataLabel: DEMO DATA`,
`marketEvidenceStatus: UNAVAILABLE`, skill/career identities and names,
optional recorded proficiency, effective assumed proficiency, `profileUpdatedAt`,
`assumption`, selected-skill prerequisite context, `before`, `after`,
`newlySatisfiedRequirements`, `newlyEligibleSkills`, and `noChange`.

Before/after states contain `requiredSatisfied`, `requiredTotal`,
`preferredSatisfied`, `preferredTotal`, `eligibleUnfinishedSkills` and `priorities`
(the existing learning-priorities response). Direct career requirements are met
at INTERMEDIATE or above. Assumed proficiency is the higher of recorded and
INTERMEDIATE. Missing prerequisites remain missing; only the selected skill's
in-memory value changes. Counts are internal catalog skills, not jobs or overall
career-fit percentages.

Malformed/missing UUIDs return 400; missing skills and inactive/missing careers
return 404; missing authentication returns 401. A skill outside the selected
career's graph is valid and may produce `noChange: true`. Missing profile skills
or weekly availability do not block calculation. See the
[simulation policy](../simulator/SKILL_SIMULATION.md).

## Phase 7 market evidence

All routes require authentication. There is no public refresh, arbitrary URL,
provider selection or raw observation upload API. Collection is operator-enabled.

| Method and route | Result |
| --- | --- |
| `GET /api/market?careerId={uuid}` | Latest `{status,snapshot?,message}`, or UNAVAILABLE when no snapshot exists |
| `GET /api/market/sources` | Source ID, last attempt/success timestamps and last collection status |
| `GET /api/market/snapshots/{id}` | Immutable snapshot with freshness evaluated at request time |
| `GET /api/market/observations/{id}` | Normalized provenance and extracted skills; no raw description |
| `POST /api/market/snapshots/{id}/decisions` | 201: private immutable evidence/profile comparison, no request body |
| `GET /api/market/decisions/{id}` | Saved owner-only result, without recalculation |

Missing career/snapshot/observation or another user's decision returns 404;
malformed UUIDs return 400; absent authentication returns 401. Insufficient or
stale evidence is a successful evidence response with an explicit status. A
comparison against such evidence retains profile-only scoring and market weight 0.

Snapshots contain ID/career, source/backlink/context, collection/window/freshness
timestamps, sample/employer/skill-coverage counts, processing version, title
matching phrases, per-skill required/preferred/unspecified counts, observation
membership and limitations. Saved comparisons additionally retain input skill
levels/confidence, interests/domains/languages/goals, relevant catalog inputs,
base scoring version, weekly availability, profile timestamp, evaluation time/status, base
career factors, market compatibility/weight, final indicator, priorities and
versioned methodology. Raw source payloads are never exposed.

Existing `/api/careers/analyze`, `/api/decisions/learning-priorities` and
`/api/simulator/skill` retain their profile-only behavior. See
[evidence policy](../market/EVIDENCE_POLICY.md) for formulas and eligibility.
# Phase 8: pasted job descriptions

All job endpoints require a bearer token. Pasted text is untrusted input; no URL
is fetched and no AI is used. See [matching policy](../jobs/JOB_ANALYSIS_POLICY.md).

| Method | Path | Result |
| --- | --- | --- |
| POST | `/api/jobs/extract` | 200 unsaved draft from `{ "description": "..." }` |
| POST | `/api/jobs/analyses` | 201 private immutable comparison |
| GET | `/api/jobs/analyses/{id}` | 200 saved comparison; other owners/missing IDs receive 404 |

The draft includes original description, extraction version, editable title,
responsibilities, experience, location, technologies, required/preferred/unclassified
skill-name lists and parser limitations. Extraction does not assert employer proficiency.

Create an analysis with those editable fields (omit extraction version/limitation)
and `reviewed: true`. All text fields and all three lists are required, but empty
metadata strings/lists are permitted. Description must be nonblank and at most
20,000 characters. Each list allows at most 100 nonblank names of 100 characters.
Malformed/unreviewed/oversized requests receive 400; unauthenticated requests 401.

The saved result contains `id`, `calculatedAt`, `calculationVersion`,
`originalExtraction`, `reviewedJob`, `profileInputs`, `status`, optional
`matchIndicator`, `skills`, `priorities`, `preparationVersion` and `methodology`.
Skill entries distinguish MATCHED, PARTIAL, MISSING and UNASSESSED. Unknown and
unclassified requirements have weight 0. Required mapped skills weigh 3 and
preferred 1; no scored requirements means the indicator is omitted. Profile and
learning plans are never changed. GET returns the frozen result without rescoring.
# Phase 9: responsible mentor

All endpoints require authentication. Other owners' conversations/job attachments
return 404. See [mentor contract](../ai/MENTOR_CONTRACT.md).

| Method | Path | Result |
| --- | --- | --- |
| GET | `/api/mentor/status` | Configuration enabled flag/model/prompt version; not a live model-health guarantee |
| GET | `/api/mentor/conversations` | Latest 20 owned conversations |
| POST | `/api/mentor/conversations` | 201 with `{careerId, jobAnalysisId?}`; active career and owned optional job required |
| GET | `/api/mentor/conversations/{id}?page=0` | Conversation and 20 historical turns; pages 0–2, newest page first, turns chronological |
| POST | `/api/mentor/conversations/{id}/messages` | 201 saved turn for `{requestId, expectedRevision, question}` |

Question: nonblank, maximum 2,000 characters. Request ID is a client UUID; expected
revision is nonnegative. Identical completed retries return the saved turn.
Conflicting reuse, stale revision or the 60-turn limit returns 409. Validation
errors return 400. The API never accepts model settings, source URLs or write tools.

Turn statuses: ANSWERED, UNAVAILABLE, INVALID_OUTPUT. Provider failure is a saved
unavailable turn, not a fabricated answer. Results include citations/provenance,
frozen bounded context, timestamp, prompt version, model name, market notice and
optional fixed navigation suggestion. Disabled AI does not prevent other APIs.
No mentor endpoint changes profile, roadmap, weekly plan or adaptation state.
