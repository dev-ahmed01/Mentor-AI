// Creates synthetic data only after the authenticated API verifies an unused account.
const base = process.env.API_URL ?? 'http://127.0.0.1:8080';
const { DEMO_EMAIL: email, DEMO_PASSWORD: password } = process.env;
if (!email || !password || password.length < 12) throw new Error('Set DEMO_EMAIL and DEMO_PASSWORD (at least 12 characters).');
async function request(path, body, token) {
  return fetch(`${base}${path}`, { method: 'POST', signal: AbortSignal.timeout(60000),
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) }, body: JSON.stringify(body) });
}
let response = await request('/api/auth/register', { email, password, displayName: 'Synthetic demo student' });
if (response.status === 409) response = await request('/api/auth/login', { email, password });
if (!response.ok) throw new Error(`Authentication failed (${response.status}); no credentials are printed.`);
const { accessToken } = await response.json();
response = await request('/api/demo/start', { confirmSynthetic: true }, accessToken);
if (!response.ok) throw new Error(`Demo preparation failed (${response.status}). Enable DEMO_ENABLED and use an empty account.`);
const { run } = await response.json();
console.log(JSON.stringify({ synthetic: true, roadmapId: run.roadmapId, weekStart: run.weekStart, next: 'Sign in with the same account and open /demo.' }));
