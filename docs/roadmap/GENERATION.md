# Roadmap generation policy

Version: `roadmap-generation-v1`, using `learning-priorities-v1`.
The starter catalog, graph and effort estimates are `DEMO DATA`.

## Generation and persistence

Generation requires an explicit active career and recorded weekly availability.
Candidates are the selected career's skills and their prerequisite foundations.
Repeatedly choose the highest priority score whose prerequisite candidates have
already been emitted; break ties by skill name then UUID. This guarantees
foundations precede dependents without including unrelated profile skills.
Group the result into learning stages of at most three tasks. Stages are ordered
learning groups, not calendar weeks.

Targets already met in the saved profile start SKIPPED, with
`satisfiedAtGeneration=true`. Other tasks start NOT_STARTED. Initial effort is
8 hours for FOUNDATIONS, 4 for DEVELOPING and 0 for TARGET_MET. These editable
estimates are starting points, not evidence of time needed for mastery.

Every generation saves a new aggregate and preserves previous roadmaps. It records
career identity/name, policy versions, profile update time and weekly hours.
Current means latest creation time (UUID tie-break); editing an older plan does
not make it current. Profile changes never silently regenerate existing plans.

## Prerequisites and task state

Each task snapshots all modeled ancestors and whether the saved profile satisfies
them. A prerequisite is satisfied by that snapshot or a COMPLETED prerequisite
task. Skipping an unknown foundation does not satisfy it. Reopening a foundation
does not retroactively change other task states; it blocks future start/complete
transitions unless the saved profile already supplies the evidence.

| Current state | Allowed new states |
| --- | --- |
| NOT_STARTED | IN_PROGRESS, COMPLETED, SKIPPED, NEEDS_REVIEW |
| IN_PROGRESS | NOT_STARTED, COMPLETED, SKIPPED, NEEDS_REVIEW |
| COMPLETED | NEEDS_REVIEW |
| SKIPPED | NOT_STARTED, NEEDS_REVIEW |
| NEEDS_REVIEW | NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED |

Keeping the same state permits title/effort edits. Starting or completing requires
satisfied prerequisites. Batch validation evaluates the complete proposed state,
independent of edit order, and failures leave the aggregate unchanged. Completion
is self-reported and does not change recorded profile proficiency.

Task effort accepts integer hours from 0 to 168; unfinished tasks need at least
one hour. Titles must be nonblank and at most 200 characters. Each update carries
`expectedRevision`; stale writes return 409, including child-only edits.

## Current focus

Current phase is the first stage with an unfinished task; COMPLETED and SKIPPED
are terminal for this calculation. Eligible unfinished tasks retain roadmap order,
with IN_PROGRESS tasks first. This week allocates at most saved weekly hours and
the decision policy's focus limit: 1 task for 1–4 hours, 2 for 5–9, 3 for 10+.
A task exceeding remaining capacity gets a partial allocation. Next action is
the first allocation, or null if no work is ready. Other work remains visible
through expandable stages and ordering explanations.

This is a capacity suggestion, not a calendar or hours-worked ledger. There are
no weekly check-ins, automatic adaptation, mastery assessment, AI calls or market
claims in Phase 3. Later phases add those capabilities behind separate contracts.
