"use client";

import { useActionState, useState, type ReactNode } from "react";
import { evaluateMarketAction } from "@/app/actions/market";
import { Card } from "@/components/ui/Card";
import { SubmitButton } from "@/components/ui/SubmitButton";

export function MarketCareerPicker({ careers, selectedId, children }: {
  careers: { id: string; name: string }[]; selectedId: string; children: ReactNode;
}) {
  const [choice, setChoice] = useState(selectedId);
  return <>
    <Card><form action="/market" method="get" className="decision-selector">
      <label className="field"><span>Career context</span><select name="careerId" value={choice} onChange={event => setChoice(event.target.value)} required>
        <option value="" disabled>Choose a career</option>{careers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
      </select></label><button className="button button-primary" type="submit" disabled={!careers.length}>View evidence</button>
    </form></Card>
    {choice === selectedId ? children : <Card><p role="status">Select “View evidence” to load the chosen career.</p></Card>}
  </>;
}

export function EvaluateMarket({ snapshotId, eligible }: { snapshotId: string; eligible: boolean }) {
  const [state, action] = useActionState(evaluateMarketAction.bind(null, snapshotId), {});
  return <Card><h2>Compare with your profile</h2>
    <p>{eligible ? "Use this exact source sample if its locations and role coverage suit your goals. The comparison includes the reserved market factor." : "This evidence cannot activate market scoring. You can still save a profile-only comparison that records why it was excluded."}</p>
    <p>This saves a private comparison. Your profile, career goal and roadmap stay unchanged.</p>
    <form action={action}><SubmitButton idle="Evaluate this snapshot" pending="Saving comparison…" />{state.error ? <p role="alert" className="form-error">{state.error}</p> : null}</form>
  </Card>;
}
