// Node 22+: usable as a file or through stdin inside the frontend container.
import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';
const api = process.env.API_URL ?? 'http://127.0.0.1:8080';
const web = process.env.FRONTEND_URL ?? 'http://127.0.0.1:3000';
async function call(path, { token, body, status = 200 } = {}) {
  const response = await fetch(`${api}${path}`, { method: body ? 'POST' : 'GET', signal: AbortSignal.timeout(60000),
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) }, body: body ? JSON.stringify(body) : undefined });
  assert.equal(response.status, status, `${path}: unexpected status`);
  return response.json();
}
async function register() {
  return (await call('/api/auth/register', { status: 201, body: { displayName: 'Synthetic release verification', email: `release-${randomUUID()}@example.com`, password: randomUUID() } })).accessToken;
}
function gitTask(roadmap) { return roadmap.phases.flatMap(p => p.tasks).find(t => t.skillName === 'Git'); }
await call('/api/demo', { status: 401 });
assert.equal((await call('/actuator/health')).status, 'UP');
const token = await register();
const start = await call('/api/demo/start', { token, body: { confirmSynthetic: true } });
assert.equal(start.enabled, true);
assert.deepEqual(await call('/api/demo/start', { token, body: { confirmSynthetic: true } }), start);
const profile = await call('/api/profile', { token });
assert.equal(profile.degree, 'BCA'); assert.equal(profile.timeAvailablePerWeek, 8);
const roadmap = await call('/api/roadmaps/current', { token });
assert.equal(gitTask(roadmap).state, 'COMPLETED');
const priorities = await call(`/api/decisions/learning-priorities?careerId=${roadmap.careerId}`, { token });
const springBoot = priorities.decisions.find(d => d.name === 'Spring Boot');
assert.equal(springBoot.priority, 'NOT_YET');
const simulation = await call('/api/simulator/skill', { token, body: { skillId: springBoot.skillId, targetCareerId: roadmap.careerId } });
assert.equal(simulation.skillName, 'Spring Boot');
assert.deepEqual(await call('/api/profile', { token }), profile);
assert.deepEqual(await call('/api/roadmaps/current', { token }), roadmap);
assert.equal((await call('/api/mentor/status', { token })).enabled, false, 'Release rehearsal expects AI disabled');
const conversation = await call('/api/mentor/conversations', { token, status: 201, body: { careerId: roadmap.careerId } });
const turn = await call(`/api/mentor/conversations/${conversation.id}/messages`, { token, status: 201, body: { requestId: randomUUID(), expectedRevision: conversation.revision, question: 'Why should I learn Spring Boot later?' } });
assert.equal(turn.status, 'UNAVAILABLE');
const plan = await call('/api/weekly-plan/current', { token });
const body = { expectedRoadmapRevision: roadmap.revision, expectedPlanRevision: plan.revision };
const exam = await call('/api/demo/exam', { token, body });
assert.deepEqual(await call('/api/demo/exam', { token, body }), exam);
const proposal = exam.checkIn.adaptation;
assert.equal(proposal.status, 'PENDING'); assert.equal(proposal.proposed.mode, 'MAINTENANCE');
assert.equal(exam.checkIn.actualHours, 2);
const accepted = await call(`/api/adaptations/${proposal.id}/accept`, { token, body: { expectedRoadmapRevision: proposal.roadmapRevision, expectedPlanRevision: proposal.planRevision } });
assert.equal(accepted.status, 'ACCEPTED'); assert.equal(accepted.accepted.capacityHours, 2);
assert.deepEqual(gitTask(await call('/api/roadmaps/current', { token })), gitTask(roadmap));
assert.deepEqual(await call('/api/profile', { token }), profile);
const other = await register();
await call(`/api/adaptations/${proposal.id}`, { token: other, status: 404 });
assert.equal((await call('/api/demo', { token: other })).run, undefined);
await call('/api/unknown-release-route', { token, status: 404 });
let asset;
for (const path of ['/demo', '/dashboard', '/progress']) {
  const response = await fetch(`${web}${path}`, { headers: { Cookie: `mentorai_token=${token}` }, signal: AbortSignal.timeout(60000), redirect: 'manual' });
  assert.equal(response.status, 200, `${path}: SSR status`);
  const html = await response.text(); assert.match(html, /synthetic/i, `${path}: synthetic label`);
  asset ??= html.match(/src="([^\"]*\/_next\/static\/[^\"]+\.js)"/)?.[1];
}
assert.ok(asset, 'SSR exposes a JavaScript asset');
assert.equal((await fetch(new URL(asset, web), { signal: AbortSignal.timeout(30000) })).status, 200);
console.log('PASS: authentication, synthetic seed/retries, prerequisite priority, read-only simulation, honest AI-unavailable state, exam proposal/acceptance, preserved profile/completion, ownership, health/errors, production SSR and static asset.');
