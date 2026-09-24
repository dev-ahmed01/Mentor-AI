"use client";

import { useActionState, useState, type ReactNode } from "react";
import { simulateSkillAction } from "@/app/actions/simulator";
import { Card } from "@/components/ui/Card";
import { SubmitButton } from "@/components/ui/SubmitButton";
import type { LearningDecision } from "@/types/api";
import type { SimulationActionState, SkillSimulation, SimulationState } from "@/types/simulator";

export function SimulatorCareerPicker({ careers, selectedId, children }: {
  careers: { id: string; name: string }[]; selectedId: string; children: ReactNode;
}) {
  const [choice, setChoice] = useState(selectedId);
  return <>
    <Card><h2>Choose a learning direction</h2><p>Compare a hypothetical skill with the internal career catalog. This preview uses recorded profile skills; completed roadmap tasks do not change recorded proficiency.</p>
      <form action="/simulator" method="get" className="decision-selector"><label className="field"><span>Target career</span><select name="careerId" value={choice} onChange={event => setChoice(event.target.value)} required>
        <option value="" disabled>Choose a career</option>{careers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
      </select></label><button type="submit" className="button button-primary" disabled={!careers.length}>Explore career skills</button></form>
      <p className="decision-evidence">SIMULATION · DEMO DATA. No live job counts or market evidence.</p>
    </Card>
    {choice === selectedId ? children : <Card><p role="status">Select “Explore career skills” to load this career. The previous scenario has been cleared.</p></Card>}
  </>;
}

function priority(task: LearningDecision) {
  return task.reasonCodes.includes("ALREADY_PROFICIENT") ? "Target met" : task.priority.toLowerCase().replaceAll("_", " ");
}
function StateSummary({ title, state }: { title: string; state: SimulationState }) {
  return <Card><h3>{title}</h3>
    <dl className="simulation-metrics">
      <div><dt>Required career skills at target</dt><dd>{state.requiredSatisfied} / {state.requiredTotal}</dd></div>
      <div><dt>Preferred career skills at target</dt><dd>{state.preferredSatisfied} / {state.preferredTotal}</dd></div>
      <div><dt>Eligible unfinished learning skills</dt><dd>{state.eligibleUnfinishedSkills}</dd></div>
    </dl>
  </Card>;
}

export function SimulationResult({ result }: { result: SkillSimulation }) {
  const before = new Map(result.before.priorities.decisions.map((task, index) => [task.skillId, { task, rank: index + 1 }]));
  const missing = result.selectedSkillPrerequisites.prerequisites.filter(p => !p.satisfied);
  return <section className="progress-stack" aria-label="Simulation result">
    <Card><p className="eyebrow">Simulation · DEMO DATA</p><h2>What changes if you learn {result.skillName}?</h2>
      <p>For {result.careerName}: recorded proficiency {result.recordedProficiency?.toLowerCase() ?? "not provided"} → assumed {result.assumedProficiency.toLowerCase()}.</p>
      <p>{result.assumption}</p>
      <p className="callout">Preview only. Your profile, career goal, roadmap and weekly plans have not changed. These counts describe internal catalog skills, not job openings or hiring odds.</p>
      {missing.length ? <p className="roadmap-blocker">Still missing foundations for {result.skillName}: {missing.map(p => p.name).join(", ")}. This hypothetical skill level does not supply those foundations or unlock dependents that still need them.</p> : null}
      {result.noChange ? <p role="status">No change for this career. The skill may already meet the assumed level or sit outside this career’s modeled learning path.</p> : null}
    </Card>
    <div className="roadmap-overview"><StateSummary title="Recorded profile" state={result.before} /><StateSummary title="Simulated profile" state={result.after} /></div>
    <Card><h3>Newly satisfied career requirements</h3>
      {result.newlySatisfiedRequirements.length ? <ul>{result.newlySatisfiedRequirements.map(t => <li key={t.skillId}>{t.name} ({t.requirement.toLowerCase()})</li>)}</ul> : <p>No additional direct career requirements reach the target in this scenario.</p>}
      <h3>Newly eligible learning</h3>
      {result.newlyEligibleSkills.length ? <ul>{result.newlyEligibleSkills.map(t => <li key={t.skillId}>{t.name}</li>)}</ul> : <p>No additional unfinished skills become eligible. Other prerequisites may still be missing.</p>}
      <p>Eligibility means the modeled foundations are met; it does not promise mastery or completion. A skill that reaches its target leaves the unfinished learning count.</p>
    </Card>
    <Card><h3>Learning priorities before and after</h3>
      <p>Ordered by the simulated priorities. Direct career skills use an Intermediate target; prerequisite-only foundations use Beginner. “Target met” is hypothetical in the simulated column.</p>
      {result.after.priorities.weeklyHours == null ? <p>No weekly availability is recorded, so no immediate “learn now” focus is assigned. You can still compare coverage and eligibility.</p> : null}
      <div className="simulation-table-wrap"><table className="simulation-table"><caption>Priority order for {result.careerName}</caption><thead><tr><th scope="col">Skill</th><th scope="col">Recorded</th><th scope="col">Simulated</th></tr></thead>
        <tbody>{result.after.priorities.decisions.map((task, index) => { const original = before.get(task.skillId); return <tr key={task.skillId}>
          <th scope="row">{task.name}</th><td>{original ? `${original.rank}. ${priority(original.task)}` : "Not in recorded list"}</td><td>{index + 1}. {priority(task)}</td>
        </tr>; })}</tbody></table></div>
      <p className="field-hint">Based on the profile saved {result.profileUpdatedAt.slice(0, 10)} (UTC). Market evidence is unavailable.</p>
    </Card>
  </section>;
}

function SimulationRun({ careerId, skillId, skillName }: { careerId: string; skillId: string; skillName: string }) {
  const [state, action, pending] = useActionState<SimulationActionState, FormData>(simulateSkillAction.bind(null, careerId, skillId), {});
  return <div className="progress-stack">
    <form action={action} className="form-stack"><div><SubmitButton idle={`Simulate learning ${skillName}`} pending="Calculating preview…" /></div>
      {state.error ? <p role="alert" className="form-error">{state.error}</p> : null}
    </form>
    <div aria-live="polite" aria-busy={pending}>{pending ? <p>Comparing your recorded profile with this scenario…</p> : state.result ? <SimulationResult result={state.result} /> : <p>Run the preview to compare your current and hypothetical learning path.</p>}</div>
  </div>;
}

export function SkillSimulator({ careerId, skills }: { careerId: string; skills: { id: string; name: string }[] }) {
  const [skillId, setSkillId] = useState(skills[0]?.id ?? "");
  const selected = skills.find(skill => skill.id === skillId);
  return <div className="progress-stack">
    <Card><label className="field"><span>Skill to explore</span><select value={skillId} onChange={event => setSkillId(event.target.value)} disabled={!skills.length}>
      {skills.map(skill => <option key={skill.id} value={skill.id}>{skill.name}</option>)}
    </select></label><p>Assume this skill reaches at least Intermediate. Higher recorded proficiency stays unchanged.</p></Card>
    {selected ? <SimulationRun key={`${careerId}-${skillId}`} careerId={careerId} skillId={skillId} skillName={selected.name} /> : <Card><p>No skills are modeled for this career yet. Choose another career.</p></Card>}
  </div>;
}
