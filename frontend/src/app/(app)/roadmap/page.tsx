import Link from "next/link";
import type { Metadata } from "next";
import { Card } from "@/components/ui/Card";
import { RoadmapGeneratorForm } from "@/components/RoadmapForms";
import { RoadmapView } from "@/components/RoadmapView";
import { getCareers } from "@/lib/careers";
import { getRoadmap } from "@/lib/roadmaps";

export const metadata: Metadata = { title: "Learning roadmap" };

export default async function RoadmapPage({ searchParams }: { searchParams: Promise<{ id?: string; careerId?: string; saved?: string }> }) {
  const query = await searchParams;
  const validId = query.id === undefined || (typeof query.id === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(query.id));
  const [roadmap, careers] = await Promise.all([validId ? getRoadmap(query.id) : Promise.resolve(null), getCareers()]);
  const choices = careers.map(({ id, name }) => ({ id, name }));
  return <div className="roadmap-page">
    <div className="page-heading"><div><p className="eyebrow">A realistic learning path</p><h1>Your roadmap</h1></div><Link href="/dashboard" className="text-link">Back to dashboard</Link></div>
    {query.saved === "1" && roadmap ? <p role="status" className="callout">Your roadmap changes were saved.</p> : null}
    {roadmap ? <RoadmapView roadmap={roadmap} /> : <Card><h2>{query.id ? "Roadmap unavailable" : "Start with a learning direction"}</h2><p>{query.id ? "This roadmap could not be found. Open your current roadmap or create a new one." : "Choose a career and save an ordered plan based on your current skills and available time."}</p>{query.id ? <Link href="/roadmap" className="text-link">Open current roadmap</Link> : null}</Card>}
    <Card>{roadmap ? <details><summary>Generate another roadmap</summary><RoadmapGeneratorForm careers={choices} careerId={query.careerId ?? roadmap.careerId} /></details>
      : <><h2>Create a roadmap</h2><RoadmapGeneratorForm careers={choices} careerId={query.careerId} /></>}
    </Card>
  </div>;
}
