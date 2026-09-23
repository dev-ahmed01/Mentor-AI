"use client";
import { useActionState, useState } from "react";
import { createPivotAction, acceptPivotAction } from "@/app/actions/pivot";
import { SubmitButton } from "@/components/ui/SubmitButton";

export function CreatePivotForm({ sourceId, careers }: { sourceId: string; careers: { id: string; name: string }[] }) {
  const [state, action, pending] = useActionState(createPivotAction, {});
  const [target, setTarget] = useState("");
  return <form action={action} className="progress-stack"><input type="hidden" name="sourceRoadmapId" value={sourceId} />
    <label className="field"><span>Career to explore</span><select required name="targetCareerId" value={target} onChange={e => setTarget(e.target.value)} disabled={pending}>
      <option value="" disabled>Choose a different career</option>{careers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select></label>
    <SubmitButton idle="Preview career pivot" pending="Comparing learning paths…" />
    {state.error ? <p role="alert" className="form-error">{state.error}</p> : null}
  </form>;
}
export function AcceptPivotForm({ id, revision }: { id: string; revision: number }) {
  const [state, action, pending] = useActionState(acceptPivotAction.bind(null, id), {});
  const [confirmed, setConfirmed] = useState(false);
  return <form action={action} className="progress-stack"><input type="hidden" name="expectedSourceRevision" value={revision} />
    <label><input type="checkbox" name="confirm" value="yes" required checked={confirmed} onChange={e => setConfirmed(e.target.checked)} disabled={pending} /> I reviewed this comparison and want to make the revised roadmap my current plan.</label>
    <SubmitButton idle="Accept pivot and create roadmap" pending="Saving revised roadmap…" />
    {state.error ? <p role="alert" className="form-error">{state.error}</p> : null}
  </form>;
}
