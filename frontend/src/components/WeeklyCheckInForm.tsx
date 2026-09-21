"use client";

import { useActionState, useState } from "react";
import { startWeeklyPlanAction, submitCheckInAction } from "@/app/actions/progress";
import { SubmitButton } from "@/components/ui/SubmitButton";
import { initialActionState } from "@/lib/forms";
import type { WeeklyPlan, WeeklyOutcome, ConstraintType, Blocker } from "@/types/progress";

const blockerChoices: { value: Blocker; label: string }[] = [
  { value: "NO_TIME", label: "Limited time" }, { value: "TOO_DIFFICULT", label: "Topic felt difficult" },
  { value: "UNCLEAR_NEXT_STEP", label: "Unsure what to do next" }, { value: "RESOURCE_ACCESS", label: "Access to resources" },
  { value: "OTHER", label: "Something else" },
];
const constraints: { value: ConstraintType; label: string }[] = [
  { value: "EXAMS", label: "Exams" }, { value: "ASSIGNMENTS", label: "Assignments" },
  { value: "INTERNSHIP", label: "Internship" }, { value: "HEALTH_OR_PERSONAL", label: "Health or personal" },
  { value: "TRAVEL", label: "Travel" }, { value: "PLACEMENT_PREP", label: "Placement preparation" }, { value: "OTHER", label: "Other" },
];

export function StartWeeklyPlanForm({ roadmapId }: { roadmapId: string }) {
  const [state, action] = useActionState(startWeeklyPlanAction.bind(null, roadmapId), initialActionState);
  return <form action={action} className="form-stack">
    {state.error ? <p role="alert" className="form-error">{state.error} <a href="/progress" className="text-link">Refresh week</a></p> : null}
    <div><SubmitButton idle="Start this week" pending="Saving your week…" /></div>
  </form>;
}

export function WeeklyCheckInForm({ plan }: { plan: WeeklyPlan }) {
  const [state, action] = useActionState(submitCheckInAction.bind(null, plan.id, plan.roadmapRevision, plan.tasks.map((task) => task.taskId)), initialActionState);
  const [outcomes, setOutcomes] = useState<Record<string, WeeklyOutcome | "">>({});
  const [actual, setActual] = useState("");
  const [nextHours, setNextHours] = useState(String(plan.capacityHours));
  const [energy, setEnergy] = useState("");
  const [difficulty, setDifficulty] = useState("");
  const [confidence, setConfidence] = useState("");
  const [blockers, setBlockers] = useState<Blocker[]>([]);
  const [constraint, setConstraint] = useState("");
  const [start, setStart] = useState(plan.weekStart);
  const [end, setEnd] = useState(plan.weekStart);
  const [notes, setNotes] = useState("");
  function markMissed() {
    setActual("0");
    setOutcomes(Object.fromEntries(plan.tasks.map((task) => [task.taskId, "MISSED"])));
  }
  return <form action={action} className="form-stack check-in-form">
    <fieldset><legend>1. How did your week go?</legend>
      <p>Partial work counts. A week with no study is useful information too.</p>
      {plan.tasks.length ? <>
        <button type="button" className="text-button" onClick={markMissed}>I couldn’t study this week</button>
        <div className="weekly-outcomes">{plan.tasks.map((task) => <label className="field" key={task.taskId}>
          <span>{task.title} · {task.plannedHours} planned hours</span>
          <select name={`outcome-${task.taskId}`} required value={outcomes[task.taskId] ?? ""} onChange={(event) => setOutcomes({ ...outcomes, [task.taskId]: event.target.value as WeeklyOutcome })}>
            <option value="" disabled>Choose an outcome</option><option value="COMPLETED">Completed</option>
            <option value="PARTIAL">Partially complete</option><option value="MISSED">Not worked on</option><option value="DEFERRED">Defer for next week</option>
          </select>
        </label>)}</div>
      </> : <p>No tasks were allocated. You can still record your time and availability.</p>}
      <p className="field-hint">Completed and partial outcomes update the roadmap. Deferring holds a task out of next week’s allocation; it stays in your roadmap.</p>
      <label className="field"><span>Hours actually spent this week</span><input name="actualHours" type="number" min={0} max={168} step={1} required value={actual} onChange={(event) => setActual(event.target.value)} /></label>
    </fieldset>
    <fieldset><legend>2. What affected your capacity?</legend>
      <label className="field"><span>Energy or capacity this week</span><select name="energyOrCapacityBand" required value={energy} onChange={(event) => setEnergy(event.target.value)}>
        <option value="" disabled>Choose a level</option><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option>
      </select></label>
      <div className="form-grid">
        <label className="field"><span>Topic difficulty (optional)</span><select name="difficultyRating" value={difficulty} onChange={(event) => setDifficulty(event.target.value)}>
          <option value="">Not rated</option><option value="1">1 — Easy</option><option value="2">2</option><option value="3">3 — Moderate</option><option value="4">4</option><option value="5">5 — Very difficult</option>
        </select></label>
        <label className="field"><span>Topic confidence (optional)</span><select name="confidenceRating" value={confidence} onChange={(event) => setConfidence(event.target.value)}>
          <option value="">Not rated</option><option value="1">1 — Low</option><option value="2">2</option><option value="3">3 — Moderate</option><option value="4">4</option><option value="5">5 — High</option>
        </select></label>
      </div>
      <fieldset className="blocker-options"><legend>What got in the way? (optional)</legend>{blockerChoices.map((item) => <label key={item.value}>
        <input type="checkbox" name="blockers" value={item.value} checked={blockers.includes(item.value)} onChange={(event) => setBlockers(event.target.checked ? [...blockers, item.value] : blockers.filter((value) => value !== item.value))} /><span>{item.label}</span>
      </label>)}</fieldset>
    </fieldset>
    <fieldset><legend>3. Make room for next week</legend>
      <label className="field"><span>Realistic hours available next week</span><input name="availableHoursNextWeek" type="number" min={0} max={168} step={1} required value={nextHours} onChange={(event) => setNextHours(event.target.value)} /></label>
      <p>Zero is okay. We’ll use the time you choose, without guessing from your energy or circumstances.</p>
      <label className="field"><span>Temporary constraint (optional)</span><select name="constraintType" value={constraint} onChange={(event) => setConstraint(event.target.value)}>
        <option value="">None to record</option>{constraints.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}
      </select></label>
      {constraint ? <div className="form-grid">
        <label className="field"><span>Constraint starts</span><input name="constraintStart" type="date" required value={start} onChange={(event) => setStart(event.target.value)} /></label>
        <label className="field"><span>Constraint ends</span><input name="constraintEnd" type="date" min={start} required value={end} onChange={(event) => setEnd(event.target.value)} /></label>
      </div> : null}
      <p>Categories are enough. You don’t need to explain private circumstances.</p>
      <details><summary>Add a brief note (optional)</summary><label className="field"><span>Note — avoid private details</span><textarea name="notes" maxLength={500} rows={3} value={notes} onChange={(event) => setNotes(event.target.value)} /></label></details>
    </fieldset>
    <p>Review your answers before saving. Each week has one final check-in. Your profile proficiency and career direction stay as recorded.</p>
    {state.error ? <p role="alert" className="form-error">{state.error} <a className="text-link" href={`/progress?weekStart=${plan.weekStart}`}>Refresh this week</a></p> : null}
    <div><SubmitButton idle="Save weekly check-in" pending="Saving your check-in…" /></div>
  </form>;
}
