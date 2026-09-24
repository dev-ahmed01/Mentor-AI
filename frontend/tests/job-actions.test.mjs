import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';
import ts from 'typescript';

// Exercise the actual server action with an unavailable API; no Next server or credentials needed.
function loadActions() {
  class ApiClientError extends Error {}
  const source = fs.readFileSync(new URL('../src/app/actions/jobs.ts', import.meta.url), 'utf8');
  const compiled = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 } });
  const exports = {};
  const requireStub = name => {
    if (name === 'next/navigation') return { redirect: () => { throw new Error('Unexpected redirect'); } };
    if (name === '@/lib/auth') return { getToken: async () => 'test-only-token' };
    if (name === '@/lib/api/client') return { ApiClientError, apiRequest: async () => { throw new Error('API offline'); } };
    throw new Error(`Unexpected import: ${name}`);
  };
  vm.runInNewContext(compiled.outputText, { exports, require: requireStub });
  return exports;
}

test('failed re-extraction retains the draft that keeps edited review fields mounted', async () => {
  const previous = { draft: { description: 'Java required', title: 'Developer' } };
  const form = new FormData(); form.set('description', previous.draft.description);
  const state = await loadActions().extractJobAction(previous, form);
  assert.equal(state.draft, previous.draft);
  assert.match(state.error, /unavailable/);
});
