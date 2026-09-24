import Link from "next/link";
import type { Metadata } from "next";
import { Card } from "@/components/ui/Card";
import { SkillSimulator, SimulatorCareerPicker } from "@/components/SkillSimulator";
import { getCareers } from "@/lib/careers";
import { getLearningPriorities } from "@/lib/decisions";

export const metadata: Metadata = { title: "Opportunity simulator" };

export default async function SimulatorPage({ searchParams }: { searchParams: Promise<{ careerId?: string | string[] }> }) {
  const query = await searchParams;
  let careers;
  try { careers = await getCareers(); } catch {
    return <Card><h1>Opportunity simulator</h1><p>Career choices are temporarily unavailable.</p><Link href="/simulator" className="text-link">Retry simulator</Link></Card>;
  }
  const selected = query.careerId === undefined ? careers[0] : careers.find(c => c.id === query.careerId);
  let priorities;
  if (selected) { try { priorities = await getLearningPriorities(selected.id); } catch { /* Render a retry state below. */ } }
  const skills = priorities?.decisions.map(d => ({ id: d.skillId, name: d.name })).sort((a, b) => a.name.localeCompare(b.name)) ?? [];
  return <div className="progress-stack">
    <div className="page-heading"><div><p className="eyebrow">Explore your next skill</p><h1>Opportunity simulator</h1><p>What happens if I learn this skill?</p></div><Link href="/dashboard" className="text-link">Back to dashboard</Link></div>
    <SimulatorCareerPicker key={selected?.id ?? "empty"} selectedId={selected?.id ?? ""} careers={careers.map(c => ({ id: c.id, name: c.name }))}>
    {!selected ? <Card><p>{careers.length ? "That career is unavailable. Choose a career from the list." : "No careers are available yet."}</p></Card>
      : priorities ? <><h2>{selected.name}</h2><SkillSimulator key={selected.id} careerId={selected.id} skills={skills} /></>
        : <Card><p>Learning context is temporarily unavailable.</p><Link href={`/simulator?careerId=${selected.id}`} className="text-link">Retry this career</Link></Card>}
    </SimulatorCareerPicker>
  </div>;
}
