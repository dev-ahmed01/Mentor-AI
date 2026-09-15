# Architecture

## Shape

MentorAI is a modular monolith. The Next.js application renders the product UI
and delegates domain operations to a single Spring Boot API. PostgreSQL is the
system of record and will also host pgvector. Ollama is a replaceable local AI
provider behind Spring AI service interfaces.

Backend packages are organized by domain (`auth`, `profile`, `skills`, `career`,
and later `market`, `roadmap`, `progress`, `jobs`, `mentor`, `ai`) with shared
configuration, errors, and security under `common`. Controllers accept DTOs,
application services own transactions and business rules, repositories only
handle persistence, and external providers sit behind ports.

## Phase 1 request flow

```text
Browser form
  -> Next.js Server Action
  -> typed centralized API client
  -> Spring Security bearer authentication
  -> validated controller DTO
  -> ownership-aware service
  -> JPA repository
  -> PostgreSQL
```

The backend issues a signed JWT after password authentication. The Next.js
server stores that token in an HttpOnly, Secure-in-production, SameSite=Lax
cookie. Client JavaScript cannot read it. Server Components and Actions exchange
it for backend responses; every private backend route independently verifies it.

## Reliability and trust boundaries

- The browser is untrusted; frontend visibility is never authorization.
- User inputs are validated at the REST boundary and constrained in PostgreSQL.
- Request IDs correlate safe errors and logs; credentials and tokens are not
  intentionally logged.
- Missing AI does not affect auth/profile features.
- Missing future market data produces an explicit unavailable/stale state, never
  synthetic current-looking data.
- Retrieved documents and job descriptions will be isolated as untrusted prompt
  content before AI integration.

## Phase 2 career flow

```text
Authenticated profile + controlled career catalog
  -> deterministic factor calculations
  -> normalization across available profile evidence
  -> ranked candidates + gaps + alternatives + uncertainties
  -> Next.js career explorer and reality pages
```

Career scoring is a pure, versioned service. It has no model or network
dependency. Phase 2 reserves the configured market weight but excludes it from
the numerator and normalization because no validated observations exist. Future
market or AI modules must cross explicit service boundaries and cannot silently
change `career-fit-v1` results.

## Decisions

- Modular monolith over microservices keeps local development understandable.
- Stateless bearer authentication supports a separate Next.js client while the
  HttpOnly server cookie avoids browser token storage.
- Flyway owns production DDL; Hibernate validates it.
- Shared normalized skills support student, career, and job relations.
- Server Components perform authenticated reads and Server Actions perform UI
  mutations, minimizing client bundles and avoiding duplicated browser fetches.
