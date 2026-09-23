# Career pivot policy

Phase 10 uses `career-pivot-v1` to compare an owned current roadmap with another
active career. Calculations are deterministic and use the starter catalog, not
AI output or labor-market evidence. A preview saves a comparison without changing
the current roadmap. Explicit acceptance creates a linked revised roadmap.

## Planning credit

Start with current profile proficiency. Add source roadmap tasks that are
COMPLETED at their recorded target proficiency; retain targets that remain
SKIPPED and were satisfied at generation. Use the higher proficiency for a skill.
Unfinished, NEEDS_REVIEW and manually skipped unknown tasks add no credit.
Reopening a previously satisfied task removes its task credit; independent
profile evidence still applies. Retained generation credit allows a subsequent
pivot to reuse work already carried into its source roadmap.

V10 also stores task credit independently of each destination learning target.
Thus INTERMEDIATE credit stays INTERMEDIATE when an intermediate career uses
that skill only as a BEGINNER foundation. Reopening marks retained credit revoked
without rewriting the historical satisfied-at-generation flag. Manually skipping
the task again cannot restore that credit; a new completion earns its target.

These are self-recorded planning assumptions, not verified mastery. They never
write to profile skills. Only the current source roadmap contributes task credit;
unrelated historical roadmaps are not merged. Source task IDs and credit sources
are stored in the comparison for audit.

## Comparison

- Transferable skills: effective known skills used by the destination, including
  partial knowledge and foundation-only skills.
- Newly required skills: destination REQUIRED or REQUIRED_FOUNDATION skills that
  were not required in the source career. A newly required skill can already be known.
- Satisfied prerequisites: unique prerequisite skills whose recorded credit meets
  the existing graph's BEGINNER threshold.
- Skippable targets: destination learning targets met by effective proficiency.
  Generated tasks stay visible as satisfied/SKIPPED, not newly COMPLETED.
- Changed priorities: compare both careers using identical effective proficiencies
  and current weekly hours, including added/removed skills, points and relevance.
  Reuse `learning-priorities-v1`; market weight remains zero.
- Proposed stages: reuse `roadmap-generation-v1` prerequisite ordering and effort.
  Preview stages identify skills; roadmap/task IDs are assigned on acceptance.

## Approximate effort

Use the generator's illustrative effort: 0 hours when target met, 4 for a
developing skill, 8 otherwise. Sum all destination tasks, including preferred
skills and foundations. Bands: TARGETS_MET = 0, SMALL = 1–16, MODERATE = 17–40,
SUBSTANTIAL = more than 40 hours. Capacity-weeks are the ceiling of hours divided
by recorded weekly availability. Missing availability blocks preview with 422.

These estimates are editable starting points, not validated mastery times,
employment readiness or a promised completion date. Prerequisite sequencing,
interruptions and existing commitments can increase elapsed time.

## Acceptance and history

The owner-only V10 record freezes the source roadmap, profile timestamp/hash,
before/after priorities, credits, comparison and proposed stages. List shows the
latest 20; older URLs remain readable. Foreign-owner IDs return 404.

Acceptance takes the saved expected source revision. It locks the owning user
and source roadmap, verifies the source is still current and unchanged, verifies
the profile fingerprint and recalculates both priority results. Stale previews
return 409 and remain readable; create a fresh preview to proceed. A retry of an
already accepted preview returns its original accepted roadmap without duplication.
Profile edits and ordinary roadmap generation take the same owner lock.

The new roadmap links `previousRoadmapId` to the source. Acceptance does not edit
or delete source tasks, earlier roadmaps, profile skills/goals, weekly plans,
check-ins or adaptation proposals. Existing weekly commitments stay on their
original roadmap. A later check-in can still record that work through the existing
progress flow; the pivot snapshot remains frozen even if the old roadmap changes.
This phase does not reschedule existing weeks or rewrite old adaptation history.

## Verification scope

Use API/integration, migration-preservation, production SSR/server-action and
lint/build checks. Interactive browser, keyboard/mobile and no-JavaScript behavior
remain outside the user's requested verification scope. No real model is needed.
