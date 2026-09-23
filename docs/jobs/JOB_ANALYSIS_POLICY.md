# Pasted job analysis

Phase 8 compares a user-reviewed job description with recorded skills. It is not
a job search service, verification of a vacancy, hiring probability or assessment
of professional competence. No model or remote fetch is used.

## Input and review

Authenticated users paste up to 20,000 characters at `/jobs/analyze`. Extraction
returns an unsaved draft. English section headings and local cues suggest required
and preferred categories; negation (including contractions), mixed cues or a
local cue with multiple named skills leave mentions unclassified. Unsupported
inline headings reset section context. Names match the controlled career/prerequisite catalog at Unicode
word boundaries, with aliases for REST APIs and Node.js (and PostgreSQL if it is
ever present in that controlled catalog). User-created profile skills do not
expand the shared vocabulary. Java does not match JavaScript.

This parser is deliberately limited. It does not discover arbitrary technologies,
understand every sentence, assess alternative requirements, infer proficiency or
verify years of experience. The review screen shows the full pasted description
and editable metadata and skill lists. Students must correct categories and add
missed names before explicitly confirming the review. Unknown names can be saved
and remain unassessed. Empty metadata means not recorded, not no requirement.
Explicit metadata labels/sections populate title, responsibilities, experience,
location and technologies; the review can correct or supply every field.

Server validation is authoritative. Up to 100 nonblank names of at most 100
characters are allowed per category. Title is limited to 300 characters,
responsibilities to 10,000, experience/technologies to 3,000 each, and location to
1,000. Original text is retained verbatim; preview markup is stripped and every
display renders text rather than HTML. Pasted instructions cannot change policy.

## Deterministic matching: job-match-v1

Resolve reviewed names against the controlled vocabulary. Deduplicate by resolved
ID, or normalized name for unknown skills. Recorded profile skills are matched by
catalog identity; custom profile aliases should be corrected in the profile if
they represent an existing catalog skill. Required takes precedence over
preferred, which takes precedence over unclassified. Unknown names are not
conflated with missing recorded skills.

| Input | Contribution |
| --- | --- |
| Mapped required skill | Weight 3 |
| Mapped preferred skill | Weight 1 |
| Missing recorded skill | Coverage 0; MISSING |
| Awareness | Coverage 1/3; PARTIAL |
| Beginner | Coverage 2/3; PARTIAL |
| Intermediate / Advanced | Coverage 1; MATCHED |
| Unknown or unclassified requirement | UNASSESSED; excluded from numerator and denominator |

The indicator is `round(100 * sum(weight * coverage) / sum(weight))`.
No assessed weight means `INSUFFICIENT_REQUIREMENTS` and no indicator (omitted
from JSON under the application's null-omission convention). Otherwise an
unassessed requirement produces `PARTIAL_ANALYSIS`; a fully mapped set produces
`CATALOG_SKILLS_ASSESSED`. Even 100 means only coverage of the assessed catalog
skills, not all job conditions. Intermediate is an illustrative comparison target,
never presented as extracted employer proficiency. Experience, responsibilities,
location, work authorization and other eligibility conditions are not scored.

## Preparation

Mapped required/preferred skills and their transitive prerequisites become
candidates for the existing `learning-priorities-v1` policy. Direct targets are
Intermediate; foundation-only targets are Beginner. Required foundations can
outrank a preferred direct skill when they block required learning. Importance is
fixed at 1 because no employer importance scale was extracted. Prerequisite gates,
already-proficient handling and weekly focus limits remain unchanged. Missing
weekly hours means no Learn now items. No market bonus is applied. Job-specific
reason codes replace career labels in the returned preparation list.

## Persistence and privacy

V8 adds `job_analyses` with owner, calculation time/version and immutable JSON.
Each save re-extracts the original description using the current extractor and
catalog, then freezes that extraction, reviewed fields, recorded skill
inputs and confidence/source, profile timestamp, weekly hours, skill results,
methodology and preparation decisions including prerequisite evidence.
Later profile or catalog changes never recompute that saved result. Its private
URL is `/jobs/analyses/{id}`; retain the URL to revisit it. Other accounts receive
404. Records persist until their owning account is removed; account deletion
cascades. No edit/list/delete API is introduced in this phase.

Saving does not modify a profile, career goal, roadmap, weekly plan or check-in.
No job inventory is created from pasted text, so `/jobs` remains deferred.
Phase 9 can consume the addressable frozen result after enforcing ownership.

## Verification and rollback

Integration tests exercise extraction, weighted arithmetic, duplicate aliases,
unknown/empty matches, partial skills, privacy, saved-input immutability, validation
and prerequisite/time gates. H2/PostgreSQL and API/production SSR checks validate
the implementation; interactive browser checks follow the user's separate scope.
V8 is additive: roll back application code while retaining its unused table.
Do not modify applied migrations or delete saved analyses as an application rollback.
