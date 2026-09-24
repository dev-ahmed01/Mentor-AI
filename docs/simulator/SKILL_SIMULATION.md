# Opportunity unlock simulator

The Phase 6 simulator answers “What happens if I learn this skill?” using the
internal career and prerequisite catalog. Results are labeled **SIMULATION** and
**DEMO DATA**. There are no job counts, market forecasts or hiring probabilities.

## Assumptions and calculations

The service reads the authenticated student's profile once and builds a map of
recorded skill proficiencies. It copies that map and raises exactly one selected
skill to at least INTERMEDIATE. Existing ADVANCED proficiency stays ADVANCED.
Every other skill, weekly availability and profile field stays unchanged.

The original and hypothetical maps run through the same `learning-priorities-v1`
policy and `skill-prerequisites-v1` graph used by normal learning priorities.
The selected skill's prerequisites are not inferred. For example, simulating
Spring Fundamentals without recorded Java does not make Spring Boot eligible:
the graph still requires the missing Java ancestor. Simulating Java unlocks
Spring Fundamentals, while Spring Boot remains blocked until both foundations
are present.

Each state shows:

- Direct REQUIRED career skills at or above INTERMEDIATE, out of total required.
- Direct PREFERRED career skills at or above INTERMEDIATE, out of total preferred.
- Eligible unfinished learning skills, including foundation-only nodes. A skill
  at its target leaves this count, so learning one skill can reduce it.
- The existing full priority response: ordering, categories, target levels,
  prerequisite readiness, reason codes and focus limit.

The response also identifies newly satisfied direct requirements and newly
eligible unfinished skills. No change in the full calculated state produces
`noChange: true`, for example for an already-advanced selected skill or one
outside the selected career's modeled path. Missing weekly availability leaves
the immediate focus limit at zero but does not prevent coverage/eligibility
comparisons.

This is not a recalculation of the Career Fit Indicator. Required/preferred
coverage uses the existing learning target, not the confidence-weighted career-fit
formula. The `career-fit-v1` calculation remains unchanged. The simulator uses
recorded profile skills; roadmap completion alone does not alter those skills.
No statement here establishes assessed competence or a complete curriculum.

## Read-only boundary

`POST /api/simulator/skill` is an authenticated calculation endpoint. It accepts
skill and target-career UUIDs; there is no user ID or mutation flag. It loads an
active career and an existing canonical skill, then calculates with immutable
copies. No hypothetical profile/entity is attached to JPA. It does not save a
profile, roadmap, weekly plan, check-in, adaptation or simulation record.

No schema migration is needed; Flyway remains at V6. Repeated requests for the
same saved profile and catalog produce identical results. The response carries
`skill-simulation-v1`, the underlying policy versions and the source profile's
update timestamp. Results are transient and clearly separate from saved progress.

## Frontend

`/simulator` lets students choose an active career and one of its direct skills
or modeled foundations. Career/roadmap links preserve the target career context.
An authenticated Server Action requests the calculation and displays a comparison.
Changing the skill remounts the preview; changing the career immediately hides
the previous scenario until the new career is loaded. Pending/error states are
local to the selected scenario. Nothing is applied or accepted into real progress.

The API also permits existing skills unrelated to a selected career, returning a
valid no-effect result. The UI narrows choices to the relevant graph for clarity.
