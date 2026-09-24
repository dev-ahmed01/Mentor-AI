"use client";

import Link from "next/link";
import { useActionState, useState } from "react";
import { createRoadmapAction, updateRoadmapAction } from "@/app/actions/roadmaps";
import { SubmitButton } from "@/components/ui/SubmitButton";
import { initialActionState } from "@/lib/forms";
import type { RoadmapTaskState } from "@/types/api";

const transitions: Record<RoadmapTaskState, RoadmapTaskState[]> = {
  NOT_STARTED: ["NOT_STARTED", "IN_PROGRESS", "COMPLETED", "SKIPPED", "NEEDS_REVIEW"],
  IN_PROGRESS: ["IN_PROGRESS", "NOT_STARTED", "COMPLETED", "SKIPPED", "NEEDS_REVIEW"],
  COMPLETED: ["COMPLETED", "NEEDS_REVIEW"],
  SKIPPED: ["SKIPPED", "NOT_STARTED", "NEEDS_REVIEW"],
  NEEDS_REVIEW: ["NEEDS_REVIEW", "NOT_STARTED", "IN_PROGRESS", "COMPLETED", "SKIPPED"],
};
const label = (value: string) => value.toLowerCase().replaceAll("_", " ");

export function RoadmapGeneratorForm({ careers, careerId }: { careers: Array<{ id: string; name: string }>; careerId?: string }) {
  const [state, action] = useActionState(createRoadmapAction, initialActionState);
  const [selected, setSelected] = useState(careers.some((item) => item.id === careerId) ? careerId : "");
  return <form action={action} className="form-stack">
    <label className="field"><span>Target career for this roadmap</span><select name="careerId" value={selected} onChange={(event) => setSelected(event.target.value)} required>
      <option value="" disabled>Choose a career</option>{careers.map((career) => <option key={career.id} value={career.id}>{career.name}</option>)}
    </select></label>
    <p>Generation uses your saved skills and weekly time. It creates a new roadmap and preserves earlier roadmaps.</p>
    {state.error ? <p className="form-error" role="alert">{state.error} <Link href="/profile" className="text-link">Review profile</Link></p> : null}
    <div><SubmitButton idle="Generate roadmap" pending="Generating roadmap…" /></div>
  </form>;
}

export function RoadmapTitleEditor({ id, revision, title }: { id: string; revision: number; title: string }) {
  const [value, setValue] = useState(title);
  const [state, action] = useActionState(updateRoadmapAction.bind(null, id, revision, null), initialActionState);
  return <form action={action} className="form-stack">
    <label className="field"><span>Roadmap title</span><input name="title" value={value} onChange={(event) => setValue(event.target.value)} required maxLength={200} /></label>
    {state.error ? <p className="form-error" role="alert">{state.error} <a href={`/roadmap?id=${id}`} className="text-link">Refresh roadmap</a></p> : null}
    <div><SubmitButton idle="Save title" pending="Saving…" /></div>
  </form>;
}

export function RoadmapTaskEditor({ roadmapId, revision, task }: {
  roadmapId: string; revision: number;
  task: { id: string; title: string; estimatedHours: number; state: RoadmapTaskState; ready: boolean };
}) {
  const [title, setTitle] = useState(task.title);
  const [hours, setHours] = useState(String(task.estimatedHours));
  const [taskState, setTaskState] = useState(task.state);
  const [result, action] = useActionState(updateRoadmapAction.bind(null, roadmapId, revision, task.id), initialActionState);
  return <form action={action} className="form-stack roadmap-task-form">
    <label className="field"><span>Task title</span><input name="title" value={title} onChange={(event) => setTitle(event.target.value)} maxLength={200} required /></label>
    <div className="form-grid">
      <label className="field"><span>Task state</span><select name="state" value={taskState} onChange={(event) => setTaskState(event.target.value as RoadmapTaskState)}>
        {transitions[task.state].map((value) => <option key={value} value={value} disabled={!task.ready && value !== task.state && (value === "IN_PROGRESS" || value === "COMPLETED")}>{label(value)}</option>)}
      </select></label>
      <label className="field"><span>Estimated total hours</span><input name="estimatedHours" type="number" min={taskState === "COMPLETED" || taskState === "SKIPPED" ? 0 : 1} max={168} step={1} value={hours} onChange={(event) => setHours(event.target.value)} required /></label>
    </div>
    <p>Completion is your own check of the task. It does not change your recorded profile proficiency. Skipping an unknown foundation keeps dependent tasks blocked.</p>
    {result.error ? <p className="form-error" role="alert">{result.error} <a href={`/roadmap?id=${roadmapId}`} className="text-link">Refresh roadmap</a></p> : null}
    <div><SubmitButton idle="Save task" pending="Saving task…" /></div>
  </form>;
}
