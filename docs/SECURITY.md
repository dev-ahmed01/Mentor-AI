# Security verification and operating boundaries

All student resources, including demo preparation, require bearer authentication.
Next.js keeps the token in an HttpOnly cookie and sends it server-side. Production
cookies require HTTPS. Backend ownership checks are authoritative; hidden controls
are not authorization. CORS permits the configured frontend origin only.

Demo preparation defaults off and refuses populated accounts. Every seeded record
belongs to the authenticated account; synthetic status persists when preparation
is disabled. Exam shortcuts invoke ordinary check-in/adaptation policies and
explicit acceptance remains required. There is no reset route or shared password.

The regression suite covers ownership, stale revisions, concurrent operations,
safe errors, limited health details, model failures and untrusted mentor content.
Frontend API waits have a 60-second default bound. Images exclude local environment
files and build caches; production Compose publishes only the loopback frontend.

Run `npm audit --omit=dev` in frontend and `node scripts/check-secrets.mjs` in the
root before release. The latter inspects tracked files for environment files,
private keys and common token signatures, reporting locations only. It is not a
comprehensive credential or Java dependency audit. Never log tokens/passwords or
commit local environment files. Repository history and deployment secrets need
separate operational review before a public launch.

The final phase updates Next.js to 16.3.6 and Sharp to 0.35.4 following published
[Next.js](https://github.com/advisories/GHSA-p293-qw3h-jr36) and
[Sharp](https://github.com/advisories/GHSA-rgj7-g3m4-5g8c) advisories. Audit results
are a point-in-time check; rerun them before deployment.

Use a TLS reverse proxy, resource limits, registration/login abuse controls,
restricted operational access, backups and monitoring for a public service.
This project is a verified hackathon application, not a penetration-tested service.
