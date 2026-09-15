# Career Fit Indicator (`career-fit-v1`)

The Phase 2 Career Fit Indicator is a deterministic profile-to-catalog
comparison. It helps a student choose what to explore next. It is not a hiring
probability, employability percentage, prediction, or live-market score.

## Configured factors

| Factor | Weight | Phase 2 source |
| --- | ---: | --- |
| Interest alignment | 20 | Saved interests, preferred domains, and languages |
| Goal alignment | 15 | Saved goals plus short- and long-term goals |
| Skill alignment | 30 | Saved skill proficiency and confidence |
| Market compatibility | 15 | Unavailable; reserved for validated evidence |
| Entry accessibility | 15 | Controlled catalog entry difficulty |
| Learning-effort compatibility | 5 | Available versus suggested weekly hours |

Phase 2 has 85 available weight points. The market factor is `null` and excluded
from both numerator and denominator:

```text
indicator = round(sum(available factor score × factor weight) / 85)
```

The result is clamped to 0–100. Responses expose all configured weights, the 85
available points, `PROFILE_ONLY_NO_MARKET_EVIDENCE`, and
`INSUFFICIENT_MARKET_EVIDENCE` so consumers cannot mistake missing evidence for
a neutral market score.

## Factor rules

- Interest and goal phrases are normalized and compared with controlled career
  signals. One matching signal scores 60, two score 85, and three or more score
  100. No match scores 0.
- Skill contribution is weighted by catalog importance. Required skills use the
  full importance and preferred skills use 60%. Self-reported proficiency maps
  to 25/50/75/100% and confidence applies an 80/90/100% multiplier.
- Entry accessibility maps controlled difficulty to 90 (`LOW`), 70 (`MEDIUM`),
  45 (`HIGH`), or 25 (`VERY_HIGH`). This is an exploration heuristic, not a
  claim about a specific employer.
- Effort compatibility is available weekly hours divided by the catalog's
  suggested hours, capped at 100. Missing time scores 0 and is disclosed as a
  profile limitation.
- Skill gaps include absent skills and skills below intermediate proficiency.
  Required skills with importance 4–5 are `HIGH`; other required skills are
  `MEDIUM`; remaining gaps are `LATER`.

Ties are resolved by career name, which makes results reproducible for the same
profile and catalog version. Each candidate includes the factors, plain-language
reasons, recorded strengths, gaps, risks, two alternatives, uncertainties, and
one next step.

## Known limitations

- Catalog signal phrases are curated and do not understand every synonym.
- Skill evidence is self-reported unless its source states otherwise.
- Education relevance is displayed as career context but is not scored in v1.
- Region, job demand, salary, posting volume, competition, and recency are not
  scored because Phase 2 stores no validated market observations.
- A new formula must use a new calculation version; it must not reinterpret
  persisted or previously displayed `career-fit-v1` results.
