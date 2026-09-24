import Link from "next/link";
import type { Metadata } from "next";
import { Card } from "@/components/ui/Card";
import { MarketCareerPicker, EvaluateMarket } from "@/components/MarketControls";
import { MarketEvidenceView } from "@/components/MarketEvidenceView";
import { getCareers } from "@/lib/careers";
import { getToken } from "@/lib/auth";
import { apiRequest } from "@/lib/api/client";
import type { MarketEvidence } from "@/types/market";

export const metadata: Metadata = { title: "Market evidence" };

export default async function MarketPage({ searchParams }: { searchParams: Promise<{ careerId?: string | string[] }> }) {
  const query = await searchParams;
  let careers;
  try { careers = await getCareers(); } catch { return <Card><h1>Market evidence</h1><p>Career choices are temporarily unavailable.</p><Link href="/market">Retry</Link></Card>; }
  const selected = query.careerId === undefined ? careers[0] : careers.find(c => c.id === query.careerId);
  let evidence: MarketEvidence | undefined;
  if (selected) { try { evidence = await apiRequest<MarketEvidence>(`/api/market?careerId=${selected.id}`, { token: await getToken() }); } catch { /* Retry state below. */ } }
  return <div className="progress-stack">
    <div className="page-heading"><div><p className="eyebrow">Inspect the evidence</p><h1>Market evidence</h1><p>Source-traceable observations with visible limits. A bounded sample cannot describe the entire job market.</p></div><Link href="/dashboard">Back to dashboard</Link></div>
    <MarketCareerPicker key={selected?.id ?? "empty"} selectedId={selected?.id ?? ""} careers={careers.map(c => ({ id: c.id, name: c.name }))}>
      {!selected ? <Card><p>{careers.length ? "That career is unavailable. Choose a career from the list." : "No careers are available yet."}</p></Card>
        : evidence ? <><MarketEvidenceView evidence={evidence} />{evidence.snapshot ? <EvaluateMarket snapshotId={evidence.snapshot.id} eligible={evidence.status === "AVAILABLE"} /> : null}</>
          : <Card><p>Market evidence is temporarily unavailable. Profile-only decisions still work.</p><Link href={`/market?careerId=${selected.id}`}>Retry evidence</Link></Card>}
    </MarketCareerPicker>
  </div>;
}
