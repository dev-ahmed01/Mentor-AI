# Weekly plans and check-ins

Phase 4 saves a record of the student's week and a realistic allocation for the
next week. It does not assess mastery or change the student's career direction.

## Week and snapshot policy

Weeks start on Monday in UTC. Each authenticated user can have one plan per week,
regardless of how many roadmaps they create. Starting a week explicitly snapshots
an owned roadmap's title, ready task titles, allocated hours and saved availability.
GET requests never create records. New roadmap generations and edits do not
silently replace an existing weekly plan.

Allocation considers unfinished tasks whose prerequisites are satisfied, with
in-progress tasks first and roadmap order otherwise. The focus limits remain
one task for 1–4 available hours, two for 5–9, and three for 10 or more. Hours
allocated to a task cannot exceed its current estimate or the remaining capacity.
Capacity is a ceiling: there may be fewer eligible tasks than hours available.
These are illustrative allocations, not promises of mastery or completion.

## One final reflection per week

Submit exactly one outcome for each task in the saved weekly plan:

| Outcome | Meaning and effect |
| --- | --- |
| COMPLETED | Student explicitly marks the roadmap task completed; existing prerequisite and state rules apply. |
| PARTIAL | Student explicitly marks the roadmap task in progress; it remains eligible for another allocation. |
| MISSED | Records no work on that task without changing its roadmap state. |
| DEFERRED | Holds it out of the new next-week allocation, while retaining it in the roadmap. |

Actual hours are reported for the whole week. They do not automatically determine
outcomes, subtract task estimates, or update profile proficiency. A missed week
with zero hours is valid. Previously saved weeks can be recorded later; future
weeks cannot be checked in.

Each week permits one final check-in. Duplicate submissions return 409 rather
than adding another record or applying progress twice. All outcomes, roadmap
changes, the check-in and the next plan are saved atomically. The supplied
roadmap revision protects against stale edits; refresh after a conflict.

## Capacity and privacy

The student's next-week availability can be any whole number from 0 to 168.
Zero produces an empty plan with room for other commitments. Energy/capacity is
LOW, MEDIUM or HIGH; optional topic confidence and difficulty are rated 1–5.
None of these ratings secretly changes the reported number of available hours.

Optional blockers use categories: limited time, difficulty, unclear next step,
resource access or other. Temporary constraints use a category and a start/end
date, spanning at most 366 days: exams, assignments, internship, health or
personal, travel, placement preparation or other. No explanation is required.
An optional note is limited to 500 characters and the UI asks students to avoid
private details. The service does not log check-in bodies or notes.

## Next week and history

On submission, the service saves next week's allocation from ready unfinished
work using the student's stated capacity. Completed and explicitly deferred tasks
are excluded; partial and missed tasks remain eligible, subject to capacity.
The result explains what was recorded and why the allocation changed.

If a next-week plan already exists, such as when recording an earlier missed
week, it is preserved unchanged. The summary explicitly explains this; the
submitted availability is retained in the reflection but does not rewrite that
plan. Historical outcomes describe the week they were submitted for, while
roadmap states can subsequently change.

History is ordered newest week first in pages of 20, including unsubmitted and
upcoming plans. Only the owner can retrieve plans and reflections. A current or
past unsubmitted week can be opened from history; upcoming allocations are shown
in the prior check-in's summary.

Phase 5 adds [reviewable adaptation proposals](ADAPTIVE_ROADMAPS.md) after the
baseline allocation above. Explicit acceptance can revise an unsubmitted current
or future allocation; before/proposed/accepted snapshots remain in history.
Maintenance-mode outcomes describe review activities and do not change roadmap
completion. Check-ins include the expected weekly-plan revision to reject stale
forms after accepted edits. There are no streak penalties, market claims or
AI-generated judgments in this flow.
