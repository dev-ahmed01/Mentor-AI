"use client";
import { useActionState, useState } from "react";
import { createMentorAction, sendMentorAction } from "@/app/actions/mentor";
import { Card } from "@/components/ui/Card";
import { SubmitButton } from "@/components/ui/SubmitButton";

export function CreateMentorForm({ careers, jobAnalysisId = "" }: { careers: { id: string; name: string }[]; jobAnalysisId?: string }) {
  const [state, action, pending] = useActionState(createMentorAction, {});
  const [career, setCareer] = useState(careers[0]?.id ?? ""); const [job, setJob] = useState(jobAnalysisId);
  return <Card><h2>Start a conversation</h2><form action={action} className="progress-stack">
    <label className="field"><span>Career context</span><select name="careerId" required value={career} onChange={e => setCareer(e.target.value)} disabled={pending}>
      <option value="" disabled>Choose a career</option>{careers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select></label>
    <label className="field"><span>Saved job analysis ID (optional)</span><input name="jobAnalysisId" value={job} onChange={e => setJob(e.target.value)} maxLength={36} disabled={pending} /></label>
    <p>Use the ID at the end of a saved job comparison URL. Only your own comparison can be attached.</p>
    <SubmitButton idle="Start conversation" pending="Creating…" />{state.error ? <p className="form-error" role="alert">{state.error}</p> : null}
  </form></Card>;
}
export function MentorComposer({ id, revision, initialRequestId }: { id: string; revision: number; initialRequestId: string }) {
  const [state, action, pending] = useActionState(sendMentorAction.bind(null, id), {});
  const [question, setQuestion] = useState(""); const [requestId, setRequestId] = useState(initialRequestId);
  return <Card><h2>Ask about your next step</h2><p>For example: “Should I learn Kubernetes now?” or “I have exams for two weeks. What should I do?”</p>
    <form action={action} className="progress-stack"><input type="hidden" name="expectedRevision" value={revision} /><input type="hidden" name="requestId" value={requestId} />
      <label className="field"><span>Your question (up to 2,000 characters)</span><textarea name="question" rows={5} maxLength={2000} required value={question} disabled={pending} onChange={e => { setQuestion(e.target.value); setRequestId(crypto.randomUUID()); }} /></label>
      <SubmitButton idle="Ask mentor" pending="Checking your evidence…" />
      {state.error ? <p role="alert" className="form-error">{state.error} Reload this page if the conversation changed.</p> : null}
    </form><p>Your messages and cited context are saved privately. The mentor cannot update your profile or plans. Plan changes require the existing check-in and confirmation flow.</p>
  </Card>;
}
