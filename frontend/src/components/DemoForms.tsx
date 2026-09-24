"use client";
import { useActionState } from "react";
import { startDemoAction, examDemoAction } from "@/app/actions/demo";
import { SubmitButton } from "@/components/ui/SubmitButton";
import type { DemoRun } from "@/types/demo";
export function StartDemoForm() {
  const [state, action, pending] = useActionState(startDemoAction, {});
  return <form action={action} className="progress-stack"><label className="job-review-confirm"><input type="checkbox" name="confirmSynthetic" value="yes" required disabled={pending} /> This is an empty account. Add the clearly synthetic student profile and learning history.</label>
    <SubmitButton idle="Prepare synthetic demo" pending="Preparing demo…" />{state.error ? <p role="alert" className="form-error">{state.error}</p> : null}</form>;
}
export function ExamDemoForm({ run }: { run: DemoRun }) {
  const [state, action, pending] = useActionState(examDemoAction, {});
  return <form action={action} className="progress-stack"><input type="hidden" name="expectedRoadmapRevision" value={run.roadmapRevision} /><input type="hidden" name="expectedPlanRevision" value={run.planRevision} />
    <label className="job-review-confirm"><input type="checkbox" name="confirmExam" value="yes" required disabled={pending} /> Record this synthetic exam-week check-in: 2 actual hours, 2 hours available next week, partial first task and remaining tasks missed.</label>
    <SubmitButton idle="Record demo exam week" pending="Recording check-in…" />{state.error ? <p role="alert" className="form-error">{state.error}</p> : null}</form>;
}
