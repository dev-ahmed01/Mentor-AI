// Deliberately reports locations, never matching secret values. This is a guard, not a full secret audit.
import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
const files = execFileSync('git', ['-c', `safe.directory=${process.cwd().replaceAll('\\', '/')}`, 'ls-files', '-z'], { encoding: 'utf8' }).split('\0').filter(Boolean);
const rules = [
  ['private key', /-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----/],
  ['AWS access key', /\bAKIA[0-9A-Z]{16}\b/],
  ['GitHub token', /\bgh[pousr]_[A-Za-z0-9]{36,}\b/],
  ['OpenAI token', /\bsk-(?:proj-)?[A-Za-z0-9_-]{40,}\b/],
];
const findings = [];
for (const file of files) {
  if (/(^|\/)\.env(?:\.|$)/.test(file) && !file.endsWith('.example')) findings.push(`${file}: tracked environment file`);
  let content; try { content = readFileSync(file, 'utf8'); } catch { continue; }
  if (content.includes('\0')) continue;
  for (const [index, line] of content.split('\n').entries()) for (const [name, pattern] of rules)
    if (pattern.test(line)) findings.push(`${file}:${index + 1}: ${name}`);
}
if (findings.length) { console.error(findings.join('\n')); process.exitCode = 1; }
else console.log(`PASS: scanned ${files.length} tracked files for environment files and common credential signatures.`);
