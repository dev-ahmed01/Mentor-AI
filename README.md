# MentorAI

MentorAI is an evidence-aware career navigation platform for students. It is
designed to connect a student’s profile, interests, goals, skills, constraints,
and progress with deterministic analysis, traceable market evidence, and
responsible local AI. It recommends and explains; the student decides.

> Current status: hackathon Phase 5 adaptive roadmap proposals. Authentication, normalized
> student profiles, a controlled ten-path career catalog, deterministic career
> comparison, skill gaps, prerequisites, learning priorities, saved roadmaps, task
> progress, weekly plans, life-aware check-ins, and career-reality pages are implemented. Market and mentor features
> remain explicitly unavailable rather than being
> backed by invented evidence.

## Implemented in Phase 1

- Spring Boot modular-monolith foundation on Java 25
- PostgreSQL schema managed by Flyway
- Stateless JWT authentication with BCrypt password hashing
- Register, login, and current-user APIs
- User-owned student profiles with normalized skills
- Bean Validation, safe JSON errors, request IDs, CORS, and health endpoint
- Next.js App Router frontend with HttpOnly server-side auth cookie handling
- Seven-step onboarding, dashboard, and editable profile
- Loading, error, empty, mobile, keyboard-focus, and reduced-motion states
- Backend integration tests, frontend lint/build checks, and pinned Maven wrapper

## Implemented in Phase 2

- Normalized career and career-skill schema with a controlled ten-path catalog
- Career browse and detail APIs with responsibilities, expectations, risks, and
  explicit market-evidence boundaries
- Versioned, deterministic `Career Fit Indicator` based on profile evidence
- Factor explanations, strengths, prioritized gaps, alternatives, and next steps
- Responsive career explorer, analysis results, and career-reality UI
- Flyway migration validation and end-to-end career API/scoring tests

## Implemented in hackathon Phase 1

- Reusable skill prerequisite graph and additive V3 migration
- Cycle/duplicate/self-edge safeguards and authenticated readiness APIs
- Deterministic direct/transitive prerequisite checks using recorded proficiency
- Expandable prerequisite context on career detail pages, labeled `DEMO DATA`

See [verification progress](docs/HACKATHON_PROGRESS.md) for phase gates and
remaining verification limitations. Hackathon phase numbers differ from the
original foundation/career phase numbers above.

## Implemented in hackathon Phase 2

- Authenticated, read-only learning-priority API for an explicit target career
- Required/preferred skill ranking, prerequisite foundations and bottlenecks
- Learn now / Learn next / Later / Not yet with deterministic reasons
- Weekly focus limits and explicit handling of missing availability
- Dashboard career selector, expandable explanations and career-detail links
- Versioned [decision policy](docs/decision/SCORING.md); no market score or AI dependency

## Implemented in hackathon Phase 3

- Saved, user-owned roadmaps with ordered learning stages and prerequisite tasks
- Deterministic generation from career priorities, recorded skills and weekly time
- Next action, weekly focus, later work, completion and ordering explanations
- Editable titles, effort and task state with prerequisite and revision safeguards
- Preserved previous plans and additive V4 migration
- Versioned [generation policy](docs/roadmap/GENERATION.md); no automatic adaptation

## Implemented in hackathon Phase 4

- Saved weekly plans with planned hours, actual hours and task outcomes
- Short check-in with partial/missed/deferred work, capacity and optional constraints
- Explicit roadmap progress updates with atomic saves and stale-revision protection
- Next-week allocation, including zero-hour weeks, plus private paginated history
- Additive V5 migration and [weekly check-in policy](docs/progress/WEEKLY_CHECK_INS.md)
- Existing plans remain saved until an explicit accepted revision

## Implemented in hackathon Phase 5

- Deterministic pacing and sequencing proposals after weekly check-ins
- Accept or edit revised allocations, with before/proposed/accepted history
- Maintenance mode for temporary constraints and a preserved learning resume point
- Repeated-deferral blocker questions and safe capacity limits
- Ownership, stale-form protection and serialized check-in/acceptance operations
- Additive V6 migration and [adaptive roadmap policy](docs/progress/ADAPTIVE_ROADMAPS.md)

## Architecture

```text
Browser
  -> Next.js 16 (Server Components + Server Actions)
  -> Spring Boot REST API
  -> PostgreSQL / pgvector

Future AI path:
Spring service ports -> Spring AI -> local Ollama
```

The browser never stores the JWT in local storage. Next.js stores it in an
HttpOnly, SameSite cookie and sends it to the backend as a bearer token from
server-side code. The backend remains the authorization boundary.

See [the implementation plan](docs/IMPLEMENTATION_PLAN.md),
[architecture](docs/architecture/ARCHITECTURE.md), [API](docs/api/API.md),
[database](docs/database/DATABASE.md), and [AI architecture](docs/ai/AI_ARCHITECTURE.md).

## Prerequisites

- Java 25+
- Node.js 22+ (Node.js 24 is also supported)
- Docker with Docker Compose, or a local PostgreSQL 17 installation
- Git

Maven does not need to be installed globally; the repository includes a wrapper
pinned to Maven 3.9.16.

## Local setup

1. Copy `.env.example` to `.env` and replace every placeholder. Spring and
   Next.js read environment variables from their process environment; `.env` is
   not committed.
2. Start PostgreSQL and Ollama:

   ```powershell
   docker compose --env-file .env up -d postgres ollama
   ```

3. Export the backend variables in your shell, then run the API:

   ```powershell
   $env:DATABASE_URL='jdbc:postgresql://localhost:5432/mentorai'
   $env:DATABASE_USERNAME='mentorai'
   $env:DATABASE_PASSWORD='your-local-password'
   $env:JWT_SECRET='replace-with-at-least-32-random-characters'
   cd backend
   .\mvnw.cmd spring-boot:run '-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC'
   ```

4. In another shell, run the frontend:

   ```powershell
   cd frontend
   npm install
   $env:API_URL='http://localhost:8080'
   npm run dev
   ```

5. Open `http://localhost:3000`. The backend health check is available at
   `http://localhost:8080/actuator/health`.

## Ollama setup

Ollama is included in Compose for the later AI phase. Pull the configured chat
and embedding models before enabling AI features:

```powershell
docker compose exec ollama ollama pull qwen3:8b
docker compose exec ollama ollama pull nomic-embed-text
```

No Phase 1 endpoint calls Ollama, so profile management remains available while
the model is stopped.

## Tests and checks

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm run lint
npm run build
npm audit
```

Backend tests use an isolated H2 database in PostgreSQL compatibility mode for
fast API/security checks. Flyway applies and Hibernate validates all current
migrations in the test suite; a real PostgreSQL Testcontainers suite remains a
future hardening step.

## Environment variables

| Variable | Purpose |
| --- | --- |
| `DATABASE_URL` | PostgreSQL JDBC URL |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Database credentials |
| `JWT_SECRET` | HMAC secret, minimum 32 UTF-8 bytes |
| `JWT_EXPIRATION` | ISO-8601 duration; defaults to `PT8H` |
| `FRONTEND_ORIGIN` | Exact browser origin allowed by backend CORS |
| `API_URL` | Server-side Next.js backend URL |
| `NEXT_PUBLIC_API_URL` | Development fallback backend URL |
| `OLLAMA_BASE_URL` | Ollama endpoint for the AI phase |
| `OLLAMA_MODEL` | Replaceable chat model identifier |
| `OLLAMA_EMBEDDING_MODEL` | Replaceable embedding model identifier |

## Roadmap

Follow [the hackathon phases and Codex prompt](docs/MentorAI_Hackathon_Phases_and_Codex_Prompt.md).
First verify the existing baseline, then add skill dependencies, deterministic
learning priorities, roadmaps, weekly check-ins, adaptation, and simulation.
Market evidence and job matching follow; AI explains the established decisions
and evidence only after those contracts are stable.

On Windows, use `npm.cmd` if PowerShell blocks `npm.ps1`. The UTC JVM option
above avoids PostgreSQL rejecting the legacy Windows `Asia/Calcutta` timezone
alias; it changes only the backend process. If Maven selects an inaccessible
cache, pass `-Dmaven.repo.local=<your-existing-Maven-repository>` explicitly.

## License

MIT — see [LICENSE](LICENSE).
