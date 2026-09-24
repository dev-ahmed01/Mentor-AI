# Release and deployment

The repository supplies Java 25 and Node 24 non-root images, production Compose,
and a GitHub Actions workflow for both database suites, frontend checks, dependency
audit and a container smoke run. Local verification results and limitations are
in [the progress report](../HACKATHON_PROGRESS.md). Configuration is provided;
no public deployment is implied. A Docker-capable host and a chosen HTTPS domain
are external prerequisites. Docker image execution must be verified before release.

## Build and run

1. Copy `.env.production.example` to `.env.production.local` (ignored by Git).
   Replace both secret placeholders with independently generated random values;
   JWT_SECRET must be at least 32 UTF-8 bytes. Set FRONTEND_ORIGIN to the exact
   public HTTPS origin. Keep DEMO_ENABLED=false except for a private rehearsal.
2. On a Docker Compose v2 host, validate and build without publishing:

   ```sh
   docker compose --env-file .env.production.local -f compose.production.yml config --quiet
   docker compose --env-file .env.production.local -f compose.production.yml build
   docker compose --env-file .env.production.local -f compose.production.yml up -d --wait --wait-timeout 240
   ```

3. Terminate TLS at a reverse proxy on the host. Forward the public Host and
   X-Forwarded-Proto/Host consistently to `127.0.0.1:3000`. Production auth cookies
   are Secure, HttpOnly and SameSite=Lax; a plain HTTP browser login is not a
   production test. Keep the backend and PostgreSQL without published host ports.
   Leave WEB_BIND_ADDRESS at loopback when using a host proxy.
4. In an isolated rehearsal environment, set DEMO_ENABLED=true in the environment
   file, then recreate the backend with
   `docker compose --env-file .env.production.local -f compose.production.yml up -d --force-recreate --wait backend`.
   A plain `restart` does not load changed environment variables. Exercise the application network:

   ```sh
   docker compose --env-file .env.production.local -f compose.production.yml exec -T -e API_URL=http://backend:8080 -e FRONTEND_URL=http://frontend:3000 frontend node --input-type=module < scripts/release-smoke.mjs
   ```

   On Windows use PowerShell to pipe `Get-Content -Raw scripts/release-smoke.mjs`
   to the same `docker compose ... exec -T ...` command instead of `<`.
   This creates synthetic accounts; set DEMO_ENABLED=false and repeat the same
   recreation command after rehearsing. Existing
   synthetic banners remain. Verify HTTPS login, forms, mobile and keyboard use
   in the target browser before presenting a public release.

## Data and rollback

PostgreSQL persists in `production-postgres`. Back up the database with `pg_dump`
before every upgrade, keep an encrypted copy outside the host, and test restore
into a separate database. Flyway applies additive V1–V11 at startup and Hibernate
validates the schema. Never use `down -v`, reset migration history or drop an
existing database as an upgrade procedure. A normal `docker compose ... down`
preserves the volume. Pin tested image digests for a public release; the supplied
tags allow upstream runtime fixes and are not immutable deployment identifiers.

Rollback the application to a previously tested image only after checking schema
compatibility. Do not reverse migrations against live data; restore a backup into
a separate database if needed, accounting for writes since the backup. Changing
JWT_SECRET invalidates existing sessions. Changing the database password in an
environment file alone does not rotate an initialized PostgreSQL role.

AI and market collection remain disabled in production Compose. Enabling them
requires explicit endpoint/model/source configuration and separate verification;
no model is downloaded and no external collection is initiated by release smoke.

## CI and local checks

`.github/workflows/verify.yml` runs H2 and PostgreSQL 17 suites, a production
frontend build, frontend action tests, lint, a production npm audit, a tracked
secret-signature scan and the container smoke script. CI secrets are isolated
test fixtures, never production credentials. The workflow is provided but is not
claimed to have run until an actual remote CI result exists.

Local equivalents: `backend/mvnw verify` (Windows: `mvnw.cmd`), then in frontend
`npm ci`, `npm test`, `npm run lint`, `npm run build`, and `npm audit --omit=dev`.
Use a dedicated empty PostgreSQL test database by setting SPRING_DATASOURCE_URL,
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver,
SPRING_DATASOURCE_USERNAME and SPRING_DATASOURCE_PASSWORD. Tests create persistent
fixtures; never point them at production. Use JVM `-Duser.timezone=UTC` on Windows.
