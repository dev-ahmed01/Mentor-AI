"use client";

import { useActionState, useState } from "react";
import { extractJobAction, saveJobAnalysisAction } from "@/app/actions/jobs";
import { Card } from "@/components/ui/Card";
import { SubmitButton } from "@/components/ui/SubmitButton";
import type { JobDraft } from "@/types/jobs";

function ReviewJob({ draft }: { draft: JobDraft }) {
  const [state, action, pending] = useActionState(saveJobAnalysisAction, {});
  const [fields, setFields] = useState({ title: draft.title, responsibilities: draft.responsibilities, experience: draft.experience,
    location: draft.location, technologies: draft.technologies, requiredSkills: draft.requiredSkills.join("\n"),
    preferredSkills: draft.preferredSkills.join("\n"), unclassifiedSkills: draft.unclassifiedSkills.join("\n") });
  const [reviewed, setReviewed] = useState(false);
  const metadata = [
    ["title", "Job title", 300], ["responsibilities", "Responsibilities", 10000], ["experience", "Experience expectations", 3000],
    ["location", "Location / remote restrictions", 1000], ["technologies", "Technologies mentioned", 3000],
    ["requiredSkills", "Required skills", 10000], ["preferredSkills", "Preferred skills", 10000], ["unclassifiedSkills", "Unclassified skills / uncertain mentions", 10000],
  ] as const;
  return <Card><h2>2. Review the job requirements</h2><p>{draft.limitation}</p>
    <p>Enter skill names one per line or separated by commas, including skills the draft missed. Unknown names stay visible as unassessed. Keep ambiguous mentions unclassified. Do not infer proficiency from a mention.</p>
    <form action={action} className="progress-stack">
      <input type="hidden" name="description" value={draft.description} />
      <fieldset disabled={pending} className="job-review-fields"><legend>Review and correct the draft</legend>
        {metadata.map(([name, label, max]) => <label className="field" key={name}><span>{label}</span>
          <textarea name={name} value={fields[name]} maxLength={max} rows={name === "title" || name === "location" ? 2 : 4}
            onChange={event => { setFields({ ...fields, [name]: event.target.value }); setReviewed(false); }} />
        </label>)}
        <label className="job-review-confirm"><input type="checkbox" name="reviewed" required checked={reviewed} onChange={event => setReviewed(event.target.checked)} />
          I reviewed the full description, corrected the categories and added missing requirements.</label>
      </fieldset>
      <p>This saves a private historical comparison with your current profile. Your profile, career goal and learning plans stay unchanged.</p>
      <SubmitButton idle="Save job comparison" pending="Comparing and saving…" />
      {state.error ? <p role="alert" className="form-error">{state.error}</p> : null}
    </form>
  </Card>;
}

export function JobAnalysisForm() {
  const [description, setDescription] = useState("");
  const [state, action, pending] = useActionState(extractJobAction, {});
  const currentDraft = state.draft?.description === description ? state.draft : undefined;
  return <>
    <Card><h2>1. Paste a job description</h2><p>Use text from a listing you want to inspect. No URL is fetched, and pasted text is not verified as an active vacancy.</p>
      <form action={action} className="progress-stack">
        <label className="field"><span>Full job description (up to 20,000 characters)</span><textarea name="description" rows={14} required maxLength={20000}
          value={description} disabled={pending} onChange={event => setDescription(event.target.value)} /></label>
        <SubmitButton idle="Extract a review draft" pending="Reading description…" />
        {state.error ? <p role="alert" className="form-error">{state.error}</p> : null}
      </form>
    </Card>
    {currentDraft ? <ReviewJob key={JSON.stringify(currentDraft)} draft={currentDraft} /> : state.draft ? <Card><p role="status">The description changed. Extract a new draft before reviewing and saving.</p></Card> : null}
  </>;
}
