"use client";

import { useActionState, useState } from "react";
import { acceptAdaptationAction } from "@/app/actions/progress";
import { Card } from "@/components/ui/Card";
import { SubmitButton } from "@/components/ui/SubmitButton";
import { initialActionState } from "@/lib/forms";
import type { Adaptation, AllocationSnapshot } from "@/types/progress";

function Allocation({ title, plan }: { title: string; plan: AllocationSnapshot }) {
  return <div><h3>{title}</h3><p>{plan.capacityHours} hours capacity · {plan.mode === "MAINTENANCE" ? "Maintenance / review" : "Learning"}</p>
    {plan.tasks.length ? <ul>{plan.tasks.map((task) => <li key={task.taskId}>{task.title} · {task.plannedHours} hours</li>)}</ul>
      : <p>No tasks allocated. Space for rest or other commitments.</p>}</div>;
}

export function AdaptationReview({ adaptation: revision, sourceWeek }: { adaptation: Adaptation; sourceWeek: string }) {
  const [state, action, pending] = useActionState(acceptAdaptationAction.bind(null, revision.id, revision.roadmapRevision, revision.planRevision, sourceWeek), initialActionState);
  const [editing, setEditing] = useState(false);
  const [capacity, setCapacity] = useState(String(revision.proposed.capacityHours));
  const [selected, setSelected] = useState(() => revision.proposed.tasks.map(t => t.taskId));
  const [hours, setHours] = useState<Record<string, string>>(() => Object.fromEntries(revision.candidates.map(t => [t.taskId,
    String(revision.proposed.tasks.find(p => p.taskId === t.taskId)?.plannedHours ?? Math.min(t.maxHours, revision.proposed.capacityHours || 1))])));
  const final = revision.accepted ?? revision.proposed;
  const postponed = revision.before.tasks.filter(t => !final.tasks.some(p => p.taskId === t.taskId));
  const total = selected.reduce((sum, id) => sum + Number(hours[id] || 0), 0);
  return <Card className="adaptation-review">
    <p className="eyebrow">Keep your goal. Adjust the pace.</p>
    <h2>{revision.status === "ACCEPTED" ? "Your accepted revision" : "Suggested revision"} · week of {revision.weekStart}</h2>
    <p>{revision.reason}</p>
    <p>{revision.status === "ACCEPTED" ? "You accepted this change. The original and suggested allocations remain recorded below." : "This suggestion has not changed your saved plan. You can accept it, edit it, or keep the saved allocation."}</p>
    <div className="roadmap-overview"><Allocation title="Before this revision" plan={revision.before} /><Allocation title="Suggested allocation" plan={revision.proposed} /></div>
    {revision.accepted ? <Allocation title="Allocation you accepted" plan={revision.accepted} /> : null}
    {postponed.length ? <p>Outside this {revision.status === "ACCEPTED" ? "accepted" : "suggested"} week: {postponed.map(t => t.title).join(", ")}. Unfinished work stays in the roadmap for later.</p> : null}
    {revision.resumeTitle ? <p><strong>Learning resume point:</strong> {revision.resumeTitle}. Prerequisites and your career goal stay in place.</p> : null}
    {revision.blockerQuestions.map(q => <div className="callout" key={q.taskId}><strong>{q.title}</strong><p>{q.question}</p><p>No private explanation is required.</p></div>)}
    <p className="field-hint">Proposed {revision.createdAt.slice(0, 10)} (UTC), after your weekly check-in.{revision.acceptedAt ? ` Accepted ${revision.acceptedAt.slice(0, 10)} (UTC).` : ""}</p>
    {revision.canAccept ? <form action={action} className="form-stack">
      <fieldset disabled={pending}><legend>Your choice</legend>
        <label className="adaptation-toggle"><input type="checkbox" checked={editing} onChange={e => setEditing(e.target.checked)} /> Edit the suggested allocation</label>
        <input type="hidden" name="editing" value={editing ? "yes" : "no"} />
        {editing ? <>
          <label className="field"><span>Capacity for this week (hours)</span><input type="number" name="capacityHours" min={0} max={revision.proposed.mode === "MAINTENANCE" ? 2 : 168} step={1} required value={capacity} onChange={e => setCapacity(e.target.value)} /></label>
          <p>Select ready tasks and their hours. Leave all tasks unchecked for an empty week.{revision.proposed.mode === "MAINTENANCE" ? " Maintenance allows one review task, up to two hours." : ""}</p>
          {revision.candidates.map(task => <div className="adaptation-task" key={task.taskId}>
            <label><input name="selectedTask" type="checkbox" value={task.taskId} checked={selected.includes(task.taskId)} onChange={e => setSelected(previous => e.target.checked ? [...previous, task.taskId] : previous.filter(id => id !== task.taskId))} /> {task.title}</label>
            <label className="field"><span>Hours for {task.title}</span><input name={`hours-${task.taskId}`} type="number" min={1} max={task.maxHours} step={1} required={selected.includes(task.taskId)} disabled={!selected.includes(task.taskId)} value={hours[task.taskId] ?? ""} onChange={e => setHours(previous => ({ ...previous, [task.taskId]: e.target.value }))} /></label>
          </div>)}
          <p aria-live="polite">Selected: {total} hours of {capacity || 0} available.{total > Number(capacity) ? " Reduce task hours or increase capacity before saving." : ""}</p>
        </> : null}
      </fieldset>
      {state.error ? <p role="alert" className="form-error">{state.error} <a href={`/progress?weekStart=${sourceWeek}`} className="text-link">Refresh this week</a></p> : null}
      <div><SubmitButton idle={editing ? "Accept my edited plan" : "Accept suggested plan"} pending="Saving revision…" /></div>
    </form> : revision.status === "PENDING" ? <p>This proposal is historical or the plan has changed. The saved allocation is preserved; a future check-in can suggest a new revision.</p> : null}
  </Card>;
}
