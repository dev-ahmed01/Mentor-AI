# Synthetic demo rehearsal

Start the backend with `DEMO_ENABLED=true` for a private rehearsal. Registration
and normal authentication are still required. Use a new empty account and visit
`/demo`; explicitly confirm preparation. Existing personal profiles or feature
history are rejected. Repeated preparation returns the same run; there is no
destructive reset. Create a new account for a fresh rehearsal, especially after
the UTC week changes or after editing the seeded roadmap.

Alternatively, set `API_URL`, `DEMO_EMAIL` and `DEMO_PASSWORD` (at least 12
characters) in your shell, then run `node scripts/demo-seed.mjs` from the project
root. It registers or signs in and calls the same authenticated preparation API.
It prints no password or token. Remove those credential variables afterward.
Only point these scripts at an environment where creating synthetic accounts is
intended. They create persistent records and do not delete them.

## Three to five minute story

1. Open `/demo`, then the dashboard: BCA year 2, Java beginner, SQL intermediate,
   Git beginner, eight hours per week. The synthetic banner stays visible even
   if preparation is later disabled. Git completion is synthetic past work.
2. Open Backend Developer career fit and explain a calculated factor and gap.
   The catalog is illustrative demo data, not a hiring probability.
3. Show learning priorities and why Spring Boot is blocked by foundations.
   Kubernetes is outside this career's modeled priority graph; never invent a
   priority for it. Simulate Spring Boot proficiency and explain the prerequisite
   caveat; simulation changes neither the profile nor the roadmap.
4. Show the weekly plan, return to `/demo`, and explicitly record the exam
   scenario: two hours spent, low energy, partial/missed tasks, two available
   hours next week and a two-week exam constraint.
5. Review the pending maintenance proposal in `/progress`, then accept it.
   Show the reduced allocation and preserved completed Git task/history.
6. If time permits, show a career pivot preview and market provenance. Missing
   or stale market evidence is disclosed. Mentor AI is optional: show its
   unavailable state honestly when no local model is running.

Observed/self-reported evidence, calculated decisions, simulation, synthetic
fixtures and AI explanations must remain distinguishable. No invented job count,
employment guarantee or model success is part of this story.

## Repeatable verification

With an enabled demo API and running frontend, set `API_URL` and `FRONTEND_URL`,
then run `node scripts/release-smoke.mjs`. It creates isolated synthetic accounts,
checks seed retries, exam proposal/acceptance, preserved profile/completion,
ownership, safe errors and authenticated server-rendered pages/static assets.
This is HTTP verification; it does not prove interactive browser behavior,
keyboard access, mobile layout, no-JavaScript forms or real-model quality.
