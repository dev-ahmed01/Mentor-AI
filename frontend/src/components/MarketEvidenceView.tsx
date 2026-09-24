import { Card } from "@/components/ui/Card";
import type { MarketEvidence } from "@/types/market";

const date = (value: string) => `${value.slice(0, 19).replace("T", " ")} UTC`;

export function MarketEvidenceView({ evidence, historical = false }: { evidence: MarketEvidence; historical?: boolean }) {
  const snapshot = evidence.snapshot;
  return <>
    <Card><p className="eyebrow">{historical ? "Evidence status when evaluated" : "Evidence status"}</p><h2>{evidence.status.replaceAll("_", " ")}</h2><p>{evidence.message}</p>
      {!snapshot ? <p>No collection timestamp, data window or sample size is available yet. There is no market weight in profile-only decisions.</p> : null}
    </Card>
    {snapshot ? <>
      <Card><h2>{snapshot.careerName}: source sample</h2>
        <p>Source: <a href={snapshot.sourceUrl} target="_blank" rel="noreferrer">Arbeitnow</a>. {snapshot.sourceContext}</p>
        <dl className="simulation-metrics">
          <div><dt>Distinct listings</dt><dd>{snapshot.sampleSize}</dd></div><div><dt>Distinct employers</dt><dd>{snapshot.employerCount}</dd></div>
          <div><dt>Listings with catalog skills</dt><dd>{snapshot.listingsWithSkills} / {snapshot.sampleSize}</dd></div>
        </dl>
        <p>Last collected: {date(snapshot.collectedAt)}. Fresh until: {date(snapshot.freshUntil)}.</p>
        <p>Publication window: {date(snapshot.windowStart)} – {date(snapshot.windowEnd)}.</p>
        <p>Minimum scoring sample: {snapshot.minimumSampleSize} listings from 3 employers, with catalog skills in at least 60% of listings.</p>
        <details><summary>Scope and limitations</summary><ul>{snapshot.limitations.map(item => <li key={item}>{item}</li>)}</ul>
          <p>Title phrases used: {snapshot.matchedTitles.join(", ")}. Catalog career definitions are DEMO DATA.</p>
          <p>Snapshot: {snapshot.id}. Processing policy: {snapshot.processingVersion}.</p>
        </details>
      </Card>
      <Card><h2>Skills mentioned in this sample</h2><p>Counts describe observed mentions, not the number of jobs you qualify for. Unspecified mentions have no verified required/preferred classification.</p>
        {snapshot.skills.length ? <div className="simulation-table-wrap"><table className="simulation-table"><caption>Unique listing counts per skill</caption>
          <thead><tr><th scope="col">Skill</th><th scope="col">Mentioned</th><th scope="col">Required wording</th><th scope="col">Preferred wording</th><th scope="col">Unspecified</th></tr></thead>
          <tbody>{snapshot.skills.map(skill => <tr key={skill.skillId}><th scope="row">{skill.name}</th><td>{skill.mentions} / {snapshot.sampleSize}</td><td>{skill.required}</td><td>{skill.preferred}</td><td>{skill.unspecified}</td></tr>)}</tbody>
        </table></div> : <p>No catalog skills were extracted from matching listings.</p>}
      </Card>
      <Card><h2>Source observations</h2><p>Original listings may have changed or closed. Remote work still has location restrictions.</p>
        {snapshot.observations.length ? <ul className="market-observations">{snapshot.observations.map(item => <li key={item.id}>
          <a href={item.sourceUrl} target="_blank" rel="noreferrer">{item.title}</a><p>{item.company} · {item.location}{item.remote ? " · Remote flag supplied" : ""}</p>
          <p className="field-hint">Published {date(item.publishedAt)} · First collected for this revision {date(item.collectedAt)}</p>
          <details><summary>Extracted evidence</summary><p>{item.skills.length ? item.skills.map(s => `${s.name} (${s.requirement.toLowerCase()})`).join(", ") : "No catalog skill mentions."}</p><p className="field-hint">Evidence {item.id} · {item.processingVersion}</p></details>
        </li>)}</ul> : <p>No matching observations in this snapshot.</p>}
      </Card>
    </> : null}
  </>;
}
