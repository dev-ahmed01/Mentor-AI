# Learning priorities v1

`learning-priorities-v1` is an illustrative deterministic ordering policy, not
a market score, probability of employment, or promise of skill completion.
The UI labels it **Calculated / DEMO DATA**. The same profile, career catalog,
graph and policy version produce the same ordered response.

## Candidates and targets

Only the explicitly selected active career's skills and their modeled ancestors
are candidates. Unrelated skills, including other students' custom skill names,
are not enumerated. The target is a query parameter, not a persisted commitment.

Direct career skills have an INTERMEDIATE learning target. Foundation-only
skills have a BEGINNER target. These are self-report thresholds, not assessments.
All modeled prerequisites must be at least BEGINNER, using Phase 1's transitive
readiness calculation. Missing graph coverage is explicitly disclosed.

Learning distance counts proficiency steps to the target: absent precedes
AWARENESS, BEGINNER, INTERMEDIATE, ADVANCED. It is bounded below by zero.
Effort bands are FOUNDATIONS (absent/awareness), DEVELOPING (beginner needing
intermediate), or TARGET_MET. No effort hours or market data are invented.

## Priority points

```text
min(100, relevance + 4 * importance + min(20, 5 * bottleneckCount) + closeness)
```

- Relevance is 50 for REQUIRED or REQUIRED_FOUNDATION; 25 for PREFERRED or
  PREFERRED_FOUNDATION.
- Career importance is the existing catalog value from 1 to 5. A foundation-only
  skill inherits the maximum importance and strongest requirement among career
  skills that depend on it. A preferred career skill that is still an unmet
  foundation for a missing required skill inherits required-foundation relevance
  and the greater importance while it blocks that learning.
- Bottleneck count is the number of dependent career skills below INTERMEDIATE
  for which this candidate is an ancestor. It counts only while the candidate
  itself is below BEGINNER. Shared ancestors are counted once per career skill.
- Closeness is 10 for BEGINNER, 5 for AWARENESS, 0 for absent.
- A skill already meeting its learning target receives zero points.

Order by descending points, then skill name, then UUID. Ties reflect stable
ordering, not an evidence-backed claim that one tied skill has greater value.
Scores of blocked skills may be high: readiness gates override their points.

## Group assignment

First, a target already met receives NOT_YET with ALREADY_PROFICIENT. Otherwise,
missing prerequisites force NOT_YET. Neither consumes an immediate focus slot.

| Recorded weekly hours | Maximum Learn now focuses |
| --- | --- |
| Missing (or zero defensively) | 0 |
| 1–4 | 1 |
| 5–9 | 2 |
| 10+ | 3 |

The profile API currently accepts 1–168 hours or null. Sort the remaining eligible
skills by points. Fill LEARN_NOW up to the limit, then LEARN_NEXT with at most two
skills, and put the rest in LEARN_LATER. No concurrency or completion-time promise
is made. Unknown weekly time produces WEEKLY_TIME_UNAVAILABLE, with no Learn now.

NOT_YET does not mean useless. The response distinguishes missing prerequisites
from already-met targets. Skills outside the chosen career and its foundations
are out of scope rather than assigned arbitrary low-relevance scores.

## Evidence and reasons

Market weight is explicitly zero, market status is UNAVAILABLE, and there is no
market score field. Every decision includes MARKET_EVIDENCE_UNAVAILABLE plus
reason codes for career relevance, prerequisite state, existing proficiency,
bottlenecks and applicable time constraints. The UI maps these to ordinary text
without an LLM. No profile, roadmap, target selection or decision is persisted.

This first version uses illustrative catalog/graph data and coarse self-reported
proficiency. It does not yet use assessments, actual weekly progress, maintenance
tasks, market observations or personalized time estimates. Later policy changes
must receive a new calculation version.
