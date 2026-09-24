import Link from "next/link";
import type { Metadata } from "next";
import { Card } from "@/components/ui/Card";
import { StartDemoForm, ExamDemoForm } from "@/components/DemoForms";
import { getDemoStatus } from "@/lib/demo";
export const metadata: Metadata = { title: "Demo walkthrough" };
const career = "20000000-0000-0000-0000-000000000001";
export default async function DemoPage() {
  const status = await getDemoStatus();
  if (!status) return <Card><h1>Demo guide unavailable</h1><p>We could not load your demo state.</p><Link href="/demo">Try again</Link></Card>;
  const run = status.run;
  return <div className="progress-stack"><div className="page-heading"><div><p className="eyebrow">Synthetic story · 3–5 minutes</p><h1>A learning plan that makes room for life</h1><p>Follow one student from a realistic next step to an exam-week adjustment, while keeping their completed work.</p></div></div>
    {!run ? <Card><h2>Prepare a fresh demo account</h2><p>The example is a second-year BCA student with Java basics, intermediate SQL, Git basics and eight hours per week. It includes one explicitly synthetic completed Git task.</p><p>Use a new account. Preparation never replaces an existing profile or history, and retries reuse the same saved demo.</p>{status.enabled ? <StartDemoForm /> : <p role="status">Demo preparation is disabled on this installation. An operator can enable it for a local rehearsal. Your regular learning tools remain available.</p>}</Card> : <>
      <Card><h2>Your rehearsal is ready</h2><p>Week of {run.weekStart} · {run.scenarioVersion}. Rehearse a fresh week with a new empty account; existing records are kept.</p><Link href={`/dashboard?careerId=${career}`} className="button button-primary">Start at the dashboard</Link></Card>
      <Card><h2>1. A realistic next step <small>· about 60 seconds</small></h2><p>Show the BCA profile and eight-hour budget. Open career fit, then compare Learn now, Later and Not yet. Ask why Spring Boot is blocked: Spring Fundamentals comes first.</p><p>Kubernetes is outside this career&apos;s starter graph; no priority or market claim is invented for it.</p><Link href="/careers/backend-developer" className="text-link">Explore career fit and reality check</Link></Card>
      <Card><h2>2. Explore before changing anything <small>· about 45 seconds</small></h2><p>Simulate learning Spring Boot. Explain that simulation changes hypothetical inputs only; it does not edit the profile or mark work complete.</p><Link href={`/simulator?careerId=${career}`} className="text-link">Open the skill simulator</Link></Card>
      <Card><h2>3. Real life changes capacity <small>· about 90 seconds</small></h2><p>The saved roadmap includes completed Git work. The synthetic exam scenario records two actual study hours and a two-week exam constraint. Review the maintenance proposal and explicitly accept it in the weekly check-in screen.</p><p>The proposal is separate from acceptance. Show the smaller next-week allocation and the completed task that remains intact.</p>
        {run.examCheckInId ? <Link href="/progress" className="button button-primary">Review exam check-in and adaptation</Link> : status.enabled ? <ExamDemoForm run={run} /> : <Link href="/progress">Open regular weekly check-in</Link>}
      </Card>
      <Card><h2>4. Explain with honest evidence <small>· about 45 seconds</small></h2><p>Open market evidence only as a dated, limited sample. If it is missing or stale, show the insufficient-evidence state. Ask the mentor why Spring Boot comes later or how exams affect the plan.</p><p>AI selects saved facts and cannot edit the plan. If local AI is unavailable, show that state and use the deterministic explanations above.</p><Link href="/mentor" className="text-link">Open the mentor</Link><p><Link href="/market">Inspect market provenance</Link></p></Card>
      <Card><h2>Optional: a new direction</h2><p>Compare a career pivot and its transferable skills. A preview changes nothing; accepting preserves the old roadmap and history.</p><Link href="/pivot" className="text-link">Compare another career</Link></Card>
    </>}
    <Card><h2>What the labels mean</h2><dl className="profile-summary"><div><dt>Observed / self-reported</dt><dd>Recorded inputs or source observations, with their provenance.</dd></div><div><dt>Calculated</dt><dd>Versioned deterministic rules, not hiring probabilities.</dd></div><div><dt>Simulation</dt><dd>Hypothetical inputs; saved learning records stay unchanged.</dd></div><div><dt>AI explanation</dt><dd>Selected recorded facts; model availability and scope are explicit.</dd></div><div><dt>Demo data</dt><dd>Synthetic persona or illustrative starter catalog, not validated market evidence.</dd></div></dl></Card>
  </div>;
}
