# MentorAI Implementation Plan

## Original bootstrap context

Repository inspection on 14 August 2026 found only `CONTEXT.md` and
`DESIGN.md`. The linked GitHub repository (`dev-ahmed01/Mentor-AI`) is empty,
so there is no application code, schema, configuration, or Git history to
preserve. This plan therefore treats the repository as a clean bootstrap while
keeping those two documents as the product and engineering source of truth.

## Planned architecture

MentorAI will be a modular monolith:

```text
Next.js App Router UI
        |
        | JSON/HTTPS
        v
Spring Boot REST API
  |-- auth/profile/skills/career/roadmap/progress/jobs/market/mentor
  |-- deterministic scoring and matching services
  |-- AI and external-provider ports/adapters
        |
        +-- PostgreSQL + pgvector
        +-- Ollama through Spring AI
```

The backend owns authentication, authorization, validation, calculations, and
data access. The frontend is a typed client and never acts as the authorization
boundary. AI explains and interprets validated evidence; it does not create
market facts, scores, permissions, or percentages.

## Original delivery phases (superseded)

The sequence below records the original plan. Active delivery now follows
[the hackathon phases](MentorAI_Hackathon_Phases_and_Codex_Prompt.md), which
preserve the existing foundation and career features and place deterministic
decisions, roadmaps, and adaptation before AI integration.

1. **Foundation and student profile** — complete. Repository scaffolding, Spring Boot,
   PostgreSQL/Flyway, error contracts, security, JWT authentication, normalized
   profile/skills, Next.js shell, onboarding/profile UI, and tests.
2. **Skills and career catalog** — complete. Career and career-skill schema, controlled
   seed catalog, browse APIs/UI, deterministic Career Fit Indicator, gaps,
   alternatives, and scoring tests.
3. **AI career analysis and reality check** — Spring AI/Ollama provider port,
   versioned prompts, schema-validated structured responses, safe degradation,
   evidence labels, and evaluation fixtures.
4. **Roadmaps, projects, and progress** — dependency-aware roadmap generation,
   “not yet” priorities, skill-mapped projects, ownership rules, task progress,
   and dashboard state.
5. **Market intelligence** — provider abstraction, allowlisted collection,
   validation/deduplication, provenance, freshness, sample-size safeguards,
   aggregation, snapshots, and scheduled refresh.
6. **RAG and evidence** — pgvector schema, embedding/retrieval/evidence services,
   metadata filters, prompt-injection boundaries, and reproducibility metadata.
7. **Jobs and readiness** — untrusted job-description parsing, required versus
   preferred skills, deterministic matching/readiness, explanations, and job UI.
8. **Adaptive mentor** — context-aware conversations, safe memory boundaries,
   weekly check-ins, recalculation, roadmap change proposals, and user approval.
9. **Hardening and release** — end-to-end tests, accessibility audit, OWASP-based
   review, observability, privacy export/deletion, CI, performance checks, and
   production documentation.

Each phase must compile, pass its automated checks, expose loading/empty/error
states, and update the API and architecture documentation before the next phase.

## Dependencies

- Java 25 and Maven 3.9+
- Spring Boot 3.5.x, Spring Web, Spring Data JPA, Spring Security, Validation,
  OAuth2 Resource Server/Jose, Actuator, and Flyway
- PostgreSQL 17 with pgvector; H2 is test-only for fast isolated tests
- Node.js 22+ and Next.js 16 Active LTS with React, TypeScript, and Tailwind CSS
- Docker Compose for PostgreSQL/pgvector and Ollama
- Spring AI and Ollama are intentionally introduced in the AI phase so profile
  management remains useful when the model is unavailable

Dependencies are kept small and replaceable. No Python AI framework,
microservices, queue, cache server, or separate vector database is planned.

## Database plan

Flyway owns production schema changes; Hibernate validates rather than creates
production tables. Core relational entities are normalized around a shared
`skill` table and relation tables (`student_skill`, `career_skill`, `job_skill`).
User-owned records include an owner foreign key and all application lookups pair
the resource identifier with the authenticated user. UUID primary keys avoid
guessable sequences; database constraints enforce uniqueness and valid ranges.

Initial migrations cover users, student profiles, normalized profile collections,
skills, and student skills. Later migrations add careers/jobs, evidence and
market observations, roadmaps/projects/progress, conversations, resources, and
pgvector-backed document chunks. Seed records are explicitly labeled `DEMO DATA`
and never used as current market evidence.

## API plan

Phase 1 establishes:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/profile`
- `PUT /api/profile`
- `GET /actuator/health`

All non-public endpoints require a bearer token. Controllers use request/response
DTOs, Bean Validation, thin orchestration, ownership-aware services, and a
consistent error object containing timestamp, status, code, safe message, path,
and request ID. Later resource APIs follow predictable REST nouns and pagination.

## Frontend plan

The App Router frontend uses Server Components by default and Client Components
only for interactive forms/auth state. A centralized typed API client owns the
backend URL, JSON parsing, bearer token, and error mapping. Phase 1 supplies the
responsive public landing page, register/login, multi-step onboarding, dashboard
shell, and profile editing. Later routes add careers, roadmap, progress, market,
jobs, and contextual mentor chat. Components use semantic HTML, explicit labels,
visible focus, non-color status text, reduced-motion support, and responsive
layouts from their first implementation.

## AI plan

`AiProvider` isolates Spring AI/Ollama. `EmbeddingService`, `RetrievalService`,
and `EvidenceService` remain separate. Prompts live under versioned resource
files and place system policy, trusted application data, and untrusted retrieved
content in distinct sections. Structured outputs are schema-validated; bounded
retry is allowed only for recoverable format errors. Model unavailability returns
a clear feature-level error while deterministic and CRUD features continue.

AI responses label observed evidence, calculated metrics, inference,
recommendation, confidence, and insufficiency. Market claims require stored
evidence IDs, source, collection time, data window, and sample size.

## Testing strategy

- Unit tests: validation, normalization, scoring, skill gaps, matching, freshness,
  roadmap ordering, structured-output validation, and prompt-injection handling.
- Backend slice/integration tests: authentication, authorization/ownership, API
  contracts, migrations/repositories, safe errors, and graceful AI failure.
- Frontend tests: typed client behavior and critical interactive components.
- End-to-end tests: register -> profile -> career analysis -> roadmap -> progress
  -> mentor, plus keyboard-only and important mobile paths.
- AI evaluation fixtures: plausible alternatives, weak-foundation reality checks,
  deterministic job gaps, insufficient evidence, and malicious retrieved text.

## Immediate next step

The hackathon Phase 0 baseline is checkpointed. Phase 1 skill dependencies and
Phase 2 learning priorities, Phase 3 personalized roadmaps and Phase 4 weekly
progress/check-ins, Phase 5 adaptive roadmap proposals and Phase 6 opportunity
simulation are implemented.
See [verification results](HACKATHON_PROGRESS.md).
The next hackathon phase is Phase 7 market intelligence and evidence. The
existing `career-fit-v1` formula and public APIs remain compatible. Do not jump
to Spring AI/Ollama; the active hackathon plan introduces the mentor in Phase 9.
