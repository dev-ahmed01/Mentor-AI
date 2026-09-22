# Market evidence policy

Phase 7 separates collected evidence from the illustrative career catalog. It
introduces no AI, fabricated observations, market-wide demand estimates, job
qualification counts or hiring probabilities.

## Source and collection

`MarketDataProvider` is a replaceable adapter. The first implementation calls
only `https://www.arbeitnow.com/api/job-board-api`, with no redirects, a 10-second
connection timeout, a 20-second request timeout and a 5 MB response limit. It
processes only the first 100 records from one page (the source can return more).
It never follows pagination or listing URLs. No
student-controlled URL or network refresh endpoint exists.

Arbeitnow publishes the [API description](https://www.arbeitnow.com/blog/job-board-api)
and requires a backlink in [API terms, section 11](https://www.arbeitnow.com/terms).
These were checked on 22 September 2026. Every evidence screen attributes and
links to Arbeitnow. This is its Germany-focused feed; locations and remote flags
are retained verbatim after removing markup. Remote is not worldwide work
authorization. Seniority, language and visa eligibility are not established.

Collection is disabled by default. Operators can set
`MARKET_COLLECTION_ENABLED=true` and `MARKET_REFRESH_ON_START=true` to collect on
startup. While enabled, the collector checks every six hours. A database-backed
six-hour attempt cooldown, including failed attempts, prevents restart or
multi-instance request bursts. No automatic retries bypass 429 or other errors.
Provider failures retain previous snapshots and record FAILED source state.
An all-invalid nonempty response is REJECTED, not a fresh empty sample. A valid
empty page produces an explicit zero-sample snapshot. Keep collection disabled
when source permission is withdrawn; deployers should recheck terms before use.

## Normalization and immutable evidence

`market-normalization-v1` validates identifiers, bounded nonempty content, exact
allowlisted HTTPS listing host, and publication timestamps within the previous
30 days (no future timestamps). Canonical listing URLs remove queries/fragments.
Script/style bodies and markup are removed before deterministic skill extraction;
raw descriptions are stored as untrusted provenance and never rendered or
returned by the observation API.

Only skills referenced by controlled career requirements or prerequisite edges
participate in extraction; student-created profile skills cannot extend this
shared vocabulary. Controlled names and a few explicit aliases use Unicode word
boundaries. Local English required/preferred markers determine classification
only when one catalog skill occurs in a sentence. Multiple named skills, negated
or unclassified wording remain UNSPECIFIED. This does not establish that
an employer requires a skill. Non-English text, synonyms, ambiguous ordinary
words and broader sentence context can be missed or misclassified. No implied
proficiency or prerequisites are extracted.

Deduplication within each batch uses canonical URL and normalized
employer/title/location identity. Multiple seats with the same identity count
once. The first valid occurrence is retained. Reingestion stores no extra copy of
an identical source ID/content/version; changed content creates a new immutable
revision. Snapshots contain only one batch's accepted records, so cumulative
refreshes and old revisions never inflate its denominator. Membership rows and
observation UUIDs retain traceability. The observation collection timestamp is
the first collection of that revision; the snapshot timestamp records the latest
batch in which it was observed.

## Snapshot eligibility and aggregation

`market-snapshot-v1` matches whole career name or catalog job-title phrases in
the listing title. The snapshot stores the exact title phrases. Conservative
matching can exclude synonyms and non-English titles. Each skill contributes at
most one mention per listing; explicit required takes precedence over preferred,
then unspecified. Frequency counts sum across these mutually exclusive classes.

Every snapshot includes source context, collection time, publication window,
fresh-until time, distinct listing/employer counts, listings with extracted skills,
frequencies, observations, processing version and limitations.

Eligibility requires all of:

- At least 10 distinct career-matched listings.
- At least 3 normalized employers.
- Catalog skills in at least 60% of matched listings.
- Age strictly below 72 hours from snapshot collection.

These are product guardrails, not statistical confidence claims. Status is
AVAILABLE, STALE, INSUFFICIENT_SAMPLE, INSUFFICIENT_EMPLOYERS or LOW_SKILL_COVERAGE.
With no snapshot, status is UNAVAILABLE. Sparse or stale records remain readable
but contribute no market weight to a new comparison.

## Explicit, reproducible decisions

Existing career analysis, learning priorities, simulator and roadmap generation
remain profile-only. The student explicitly evaluates a named market snapshot
through the market screen. The resulting comparison is saved privately with its
snapshot, evaluation time/status, profile timestamp, recorded skills/confidence,
interests, domains, languages, goals, availability, relevant catalog scoring
inputs, base factor scores, result, priority list, methodology and calculation versions.
Retrieval enforces ownership and never recalculates old results.

`career-market-v1` reuses `career-fit-v1` profile factors: 20 interest + 15 goal +
30 skills + 15 entry accessibility + 5 effort. For eligible evidence, add 15 times
market compatibility and divide the exact weighted sum by 100, then round. With
ineligible evidence retain the original profile-only indicator (85 denominator).

Market compatibility is `round(100 * achieved / total)` across observed catalog
skill frequencies. A recorded skill contributes missing=0, Awareness=1/3,
Beginner=2/3, Intermediate/Advanced=1 of its frequency. All mention classes count
once; this measures source-sample skill coverage, not verified job readiness.

`learning-priorities-market-v1` adds `round(10 * mentionCount / sampleSize)` to
each unfinished skill's existing priority score, capped at 100. Proficiency,
prerequisites, tie ordering and weekly focus gates remain in force. Already-met
skills keep score 0. Ineligible evidence retains `learning-priorities-v1` and zero
market points. The wrapper records the exact evidence used; default priority
and simulator endpoints never silently acquire market influence.

Saved comparisons do not update profile, goal, roadmap, task progress, weekly
plans or adaptations. The illustrative catalog remains labeled DEMO DATA even
when a real market sample is present. A saved page shows historical evidence
status and links to current evidence so age is not mistaken for current validity.

## Migration and rollback

V7 adds `market_sources`, `market_observations`, `market_snapshots`,
`market_snapshot_observations` and `market_decisions`. Existing schemas and rows
are unchanged. Flyway remains the schema owner. Old application versions can run
against V7 because they ignore these tables. To roll back functionality, disable
collection and run the prior application version; retain evidence tables and
Flyway history. No destructive down migration is required or supplied.

Raw payloads and saved comparisons are retained without automatic pruning in this
hackathon phase. Collection volume is bounded per run, but production retention
policy and operational monitoring should be defined before prolonged deployment.
