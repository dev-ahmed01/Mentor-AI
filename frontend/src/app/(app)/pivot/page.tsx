import Link from "next/link";
import type { Metadata } from "next";
import { Card } from "@/components/ui/Card";
import { CreatePivotForm } from "@/components/PivotForms";
import { getCareers } from "@/lib/careers";
import { getRoadmap } from "@/lib/roadmaps";
import { getToken } from "@/lib/auth";
import { apiRequest } from "@/lib/api/client";
import type { PivotSummary } from "@/types/pivot";

export const metadata: Metadata = { title: "Career pivot" };
export default async function PivotPage() {
  const token = await getToken();
  let data: [Awaited<ReturnType<typeof getRoadmap>>, Awaited<ReturnType<typeof getCareers>>, PivotSummary[]];
  try { data = await Promise.all([getRoadmap(), getCareers(), apiRequest<PivotSummary[]>("/api/pivots", { token })]); }
  catch { return <Card><h1>Career pivot</h1><p>Career comparison data is temporarily unavailable.</p><Link href="/pivot">Retry</Link></Card>; }
  const [source, careers, history] = data;
  return <div className="progress-stack"><div className="page-heading"><div><p className="eyebrow">Keep what you have learned</p><h1>Explore a career pivot</h1><p>Compare your current direction with another career before deciding to switch.</p></div></div>
    <Card>{source ? <><h2>From {source.careerName}</h2><p>Source: {source.title}, revision {source.revision}. Previewing keeps your current plan unchanged.</p>
      <p>Planning credit includes recorded profile skills and completed or previously satisfied roadmap targets. This is self-recorded evidence, not verified mastery.</p>
      <CreatePivotForm sourceId={source.id} careers={careers.filter(c => c.id !== source.careerId)} /></> : <><h2>Start with a roadmap</h2><p>A pivot compares your existing learning path with a different career.</p><Link href="/roadmap">Create your first roadmap</Link></>}</Card>
    <Card><h2>Saved comparisons</h2>{history.length ? <ul>{history.map(p => <li key={p.id}><Link href={`/pivot/${p.id}`}>{p.sourceCareerName} → {p.targetCareerName}</Link> · {p.status.toLowerCase()} · {p.createdAt.slice(0, 10)}</li>)}</ul> : <p>No comparisons yet.</p>}<p>The latest 20 comparisons are listed. Older saved URLs remain available.</p></Card>
  </div>;
}
