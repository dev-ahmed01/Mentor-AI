import Link from "next/link";
import { notFound } from "next/navigation";
import type { Metadata } from "next";
import { Card } from "@/components/ui/Card";
import { AcceptPivotForm } from "@/components/PivotForms";
import { getToken } from "@/lib/auth";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import type { PivotDetail, PivotSkill } from "@/types/pivot";

export const metadata: Metadata = { title: "Career pivot comparison" };
const label = (value: string) => value.toLowerCase().replaceAll("_", " ");
function SkillList({ title, skills, explanation }: { title: string; skills: PivotSkill[]; explanation: string }) {
  return <Card><h2>{title}</h2><p>{explanation}</p>{skills.length ? <ul>{skills.map(s => <li key={s.skillId}>{s.name} · recorded credit: {s.currentProficiency ? label(s.currentProficiency) : "none"} · target: {label(s.targetProficiency)}</li>)}</ul> : <p>None in this comparison.</p>}</Card>;
}
export default async function PivotDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id)) notFound();
  let result: PivotDetail;
  try { result = await apiRequest<PivotDetail>(`/api/pivots/${id}`, { token: await getToken() }); }
  catch (error) { if (error instanceof ApiClientError && error.detail.status === 404) notFound(); return <Card><h1>Comparison unavailable</h1><p>This comparison could not be loaded.</p><Link href="/pivot">Return to career pivots</Link></Card>; }
  const c = result.comparison;
  return <div className="progress-stack"><div className="page-heading"><div><p className="eyebrow">Calculated · DEMO DATA</p><h1>{c.sourceRoadmap.careerName} → {c.after.careerName}</h1><p>Saved {result.createdAt.slice(0, 10)} · {c.policyVersion} · {label(result.status)}</p></div></div>
    <Card><h2>Transition effort: {label(c.effort.band)}</h2><p>{c.effort.illustrativeHours} illustrative learning hours · {c.effort.capacityWeeks} capacity-weeks at {c.after.weeklyHours} hours/week.</p><p>{c.effort.explanation}</p>
      <p>Source roadmap revision {c.sourceRoadmap.revision}; profile recorded {c.profileUpdatedAt.slice(0, 10)}. This comparison stays frozen when later records change.</p><Link href={`/roadmap?id=${c.sourceRoadmap.id}`}>Open source roadmap</Link>
      <details><summary>Source snapshot and planning credit</summary><p>Completed tasks count at their saved learning target. Previously satisfied targets retain that credit unless reopened. Neither credit changes your profile or certifies mastery.</p>
        {c.credits.length ? <ul>{c.credits.map(t => <li key={t.taskId}>{t.name}: {label(t.proficiency)} · {label(t.source)}</li>)}</ul> : <p>No completed or retained task credit.</p>}
        <ul>{c.sourceRoadmap.phases.flatMap(p => p.tasks).map(t => <li key={t.id}>{t.skillName}: {label(t.state)}</li>)}</ul></details>
    </Card>
    <SkillList title="Transferable skills" skills={c.transferableSkills} explanation="Your recorded knowledge used by the destination, including partial knowledge and shared foundations." />
    <SkillList title="Newly required skills" skills={c.newlyRequiredSkills} explanation="Destination requirements or required foundations that were not required in the source career. Some may already be known." />
    <SkillList title="Prerequisites already satisfied" skills={c.satisfiedPrerequisites} explanation="Recorded credit meets the starter graph's prerequisite threshold." />
    <SkillList title="Learning targets you can skip" skills={c.skippableSkills} explanation="These targets are already met by planning credit. They remain visible in the revised roadmap as satisfied, skipped tasks." />
    <Card><h2>Changed learning priorities</h2><p>Both careers use the same captured skills and weekly availability. Points order learning work; they are not employment probabilities.</p>
      {c.changedPriorities.length ? <ul>{c.changedPriorities.map(d => <li key={d.skillId}><strong>{d.name}</strong>: {label(d.beforePriority)} ({d.beforePoints ?? "—"} points; {d.beforeRelevance ? label(d.beforeRelevance) : "outside career"}) → {label(d.afterPriority)} ({d.afterPoints ?? "—"} points; {d.afterRelevance ? label(d.afterRelevance) : "outside career"})</li>)}</ul> : <p>No priority changes.</p>}
      <details><summary>Destination decisions and prerequisite gates</summary><ul>{c.after.decisions.map(d => <li key={d.skillId}><strong>{d.name}</strong>: {label(d.priority)} · {d.deterministicScore} points. {d.prerequisiteReadiness.eligible ? "Prerequisites met or none recorded." : `Missing foundations: ${d.prerequisiteReadiness.prerequisites.filter(p => !p.satisfied).map(p => p.name).join(", ")}.`}</li>)}</ul></details>
    </Card>
    <Card><h2>Proposed revised roadmap</h2><p>Stages show dependency order, not calendar weeks. Task IDs are assigned when you accept.</p>{c.proposedStages.map((stage, i) => <section key={i}><h3>{stage.title}</h3><ol>{stage.tasks.map(t => <li key={t.skillId}>{t.name} · {t.targetMet ? "target met; skip" : `${t.estimatedHours} estimated hours`}{t.prerequisites.length ? ` · foundations: ${t.prerequisites.join(", ")}` : ""}</li>)}</ol></section>)}</Card>
    <Card>{result.acceptedRoadmapId ? <><h2>Pivot accepted</h2><p>The revised roadmap was created. Your source roadmap, completed tasks, check-ins and weekly plans were preserved.</p><Link href={`/roadmap?id=${result.acceptedRoadmapId}`}>Open revised roadmap</Link></> : <><h2>Review before switching</h2><p>Accepting creates a new current roadmap. Your profile skills and goals remain unchanged. Existing weekly plans and check-ins stay attached to their original roadmap; accepting does not move existing weekly commitments.</p><p>If your profile or source roadmap changed, create a fresh preview before accepting.</p><AcceptPivotForm id={id} revision={c.sourceRoadmap.revision} /></>}
      <p><Link href="/pivot">Start another comparison</Link> · <Link href="/progress">Review weekly commitments</Link></p>
    </Card>
  </div>;
}
