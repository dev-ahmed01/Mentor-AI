import Link from "next/link";
import { Card } from "@/components/ui/Card";
import { MarketEvidenceView } from "@/components/MarketEvidenceView";
import { apiRequest } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { MarketDecision } from "@/types/market";

export default async function MarketDecisionPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  let result: MarketDecision;
  try { result = await apiRequest<MarketDecision>(`/api/market/decisions/${encodeURIComponent(id)}`, { token: await getToken() }); }
  catch { return <Card><h1>Evidence comparison unavailable</h1><p>This saved comparison may be unavailable or belong to another account.</p><Link href="/market">Back to market evidence</Link></Card>; }
  return <div className="progress-stack">
    <div className="page-heading"><h1>{result.profileOnlyCareer.careerName}: saved comparison</h1><Link href={`/market?careerId=${result.profileOnlyCareer.careerId}`}>View latest evidence</Link></div>
    <Card><p className="eyebrow">Historical result · Catalog factors are DEMO DATA</p><h2>Career Fit Indicator: {result.careerFitIndicator} / 100</h2>
      <p>Profile-only indicator: {result.profileOnlyCareer.careerFitIndicator} / 100. Market weight: {result.marketWeight} / 100.</p>
      <p>{result.marketCompatibility == null ? "Market compatibility was excluded because the snapshot did not meet the evidence guardrails." : `Skill coverage of this source sample: ${result.marketCompatibility} / 100. This is not an employment probability.`}</p>
      <p>Saved {result.calculatedAt.slice(0, 19).replace("T", " ")} UTC from the profile updated {result.profileUpdatedAt.slice(0, 19).replace("T", " ")} UTC.</p>
      <p>This result stays unchanged when your profile or market evidence changes. Your goal and roadmap were not changed. Keep this page URL to revisit the comparison.</p>
      <details><summary>Calculation method</summary><p>{result.methodology}</p><p>Career policy: {result.calculationVersion}. Learning policy: {result.priorities.calculationVersion}.</p></details>
    </Card>
    <Card><h2>Learning priorities for this comparison</h2><p>Weekly availability: {result.weeklyHours == null ? "not recorded" : `${result.weeklyHours} hours`}. Sample contribution: up to {result.priorities.marketWeight} ordering points.</p>
      <div className="simulation-table-wrap"><table className="simulation-table"><caption>Saved priority order; prerequisite and time limits still apply</caption><thead><tr><th scope="col">Skill</th><th scope="col">Priority</th><th scope="col">Ordering score</th></tr></thead>
        <tbody>{result.priorities.decisions.map(item => <tr key={item.skillId}><th scope="row">{item.name}</th><td>{item.reasonCodes.includes("ALREADY_PROFICIENT") ? "Target met" : item.priority.replaceAll("_", " ")}</td><td>{item.deterministicScore}</td></tr>)}</tbody>
      </table></div>
    </Card>
    <MarketEvidenceView evidence={result.evidence} historical />
  </div>;
}
