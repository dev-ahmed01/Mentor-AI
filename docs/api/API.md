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

## Error contract

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
