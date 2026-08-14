# MentorAI

MentorAI is an evidence-aware career navigation platform for students. It is
designed to connect a student’s profile, interests, goals, skills, constraints,
and progress with deterministic analysis, traceable market evidence, and
responsible local AI. It recommends and explains; the student decides.

> Current status: Phase 1 foundation. Authentication, normalized student
> profiles, and the responsive profile dashboard are implemented. Career,
> market, roadmap, and mentor features are intentionally shown as not yet
> implemented rather than backed by fake data.

## Implemented in Phase 1

- Spring Boot modular-monolith foundation on Java 21
- PostgreSQL schema managed by Flyway
- Stateless JWT authentication with BCrypt password hashing
- Register, login, and current-user APIs
- User-owned student profiles with normalized skills
- Bean Validation, safe JSON errors, request IDs, CORS, and health endpoint
- Next.js App Router frontend with HttpOnly server-side auth cookie handling
- Seven-step onboarding, dashboard, and editable profile
- Loading, error, empty, mobile, keyboard-focus, and reduced-motion states
- Backend integration tests, frontend lint/build checks, and pinned Maven wrapper

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

- Java 21+
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
   .\mvnw.cmd spring-boot:run
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
fast API/security checks. PostgreSQL migration tests will be added with
Testcontainers as the database domain expands.

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

The next verified phase adds the normalized career catalog and documented
deterministic Career Fit Indicator. Spring AI/Ollama follows only after career
and skill-gap calculations are testable without an LLM. Market data, RAG,
roadmaps, jobs, and adaptive mentoring then build on those foundations.

## License

MIT — see [LICENSE](LICENSE).
