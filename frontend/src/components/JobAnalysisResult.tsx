import { Card } from "@/components/ui/Card";
import type { JobAnalysis, JobSkillMatch } from "@/types/jobs";

export function JobAnalysisResult({ result }: { result: JobAnalysis }) {
  const groups: { title: string; filter: (skill: JobSkillMatch) => boolean }[] = [
    { title: "Matched skills", filter: s => s.status === "MATCHED" },
    { title: "Partial skills", filter: s => s.status === "PARTIAL" },
    { title: "Missing required skills", filter: s => s.status === "MISSING" && s.requirement === "REQUIRED" },
    { title: "Missing preferred skills", filter: s => s.status === "MISSING" && s.requirement === "PREFERRED" },
    { title: "Unassessed requirements", filter: s => s.status === "UNASSESSED" },
  ];
  const job = result.reviewedJob;
  return <>
    <Card><p className="eyebrow">Private historical comparison</p><h2>Job match indicator: {result.matchIndicator == null ? "Unavailable" : `${result.matchIndicator} / 100`}</h2>
      <p>Catalog skill coverage only. This is not hiring probability or full job readiness.</p>
      <p>{result.status === "INSUFFICIENT_REQUIREMENTS" ? "No classified catalog skills could be assessed. No score or preparation order can be calculated." : result.status === "PARTIAL_ANALYSIS" ? "Some requirements are unassessed and excluded from the score. Review these separately, even when catalog coverage is high." : "The reviewed catalog skills were assessed; experience, location and other job conditions still need your review."}</p>
      <p>Saved {result.calculatedAt.slice(0, 19).replace("T", " ")} UTC using the profile updated {result.profileInputs.updatedAt.slice(0, 19).replace("T", " ")} UTC. Later profile edits do not change this result. Keep this page URL to revisit it.</p>
      <details><summary>How this comparison was calculated</summary><p>{result.methodology}</p><p>Matching: {result.calculationVersion}. Preparation: {result.preparationVersion}.</p></details>
    </Card>
    <div className="learning-priority-grid">{groups.map(group => {
      const skills = result.skills.filter(group.filter);
      return <Card key={group.title}><h2>{group.title} ({skills.length})</h2>{skills.length ? <ul>{skills.map((s, index) => <li key={s.skillId ?? `${s.name}-${index}`}>
        <strong>{s.name}</strong> · {s.requirement.toLowerCase()} · {s.currentProficiency?.toLowerCase() ?? "not recorded"}
        {s.comparisonTarget ? ` → ${s.comparisonTarget.toLowerCase()} comparison target` : " · excluded from scoring"}
      </li>)}</ul> : <p>None in this comparison.</p>}</Card>;
    })}</div>
    <Card><h2>Recommended preparation priorities</h2><p>Weekly availability: {result.profileInputs.weeklyHours == null ? "not recorded; no immediate focus assigned" : `${result.profileInputs.weeklyHours} hours`}. Prerequisites use an illustrative DEMO graph. These suggestions do not change your saved roadmap.</p>
      {result.priorities.length ? <ol className="learning-decision-list">{result.priorities.map(item => <li key={item.skillId}>
        <strong>{item.name}</strong> · {item.reasonCodes.includes("ALREADY_PROFICIENT") ? "Comparison target met" : item.priority.replaceAll("_", " ").toLowerCase()}
        <p>{item.careerRelevance.replaceAll("_", " ").toLowerCase()} · target {item.targetProficiency.toLowerCase()} · ordering score {item.deterministicScore} (not a job-match percentage).</p>
        {!item.prerequisiteReadiness.eligible ? <p>First build: {item.prerequisiteReadiness.prerequisites.filter(p => !p.satisfied).map(p => p.name).join(", ")}.</p> : null}
      </li>)}</ol> : <p>No preparation order is available without classified catalog requirements.</p>}
    </Card>
    <Card><h2>Reviewed job details</h2><p>These details are stored for context and are not scored.</p>
      <dl>{[["Title", job.title], ["Responsibilities", job.responsibilities], ["Experience expectations", job.experience], ["Location", job.location], ["Technologies", job.technologies]].map(([name, value]) => <div key={name}><dt><strong>{name}</strong></dt><dd className="job-source-text">{value || "Not recorded"}</dd></div>)}</dl>
      <details><summary>Original pasted description</summary><p className="job-source-text">{job.description}</p></details>
      <details><summary>Original extraction suggestions</summary><p>{result.originalExtraction.limitation}</p><p>Required: {result.originalExtraction.requiredSkills.join(", ") || "none identified"}</p><p>Preferred: {result.originalExtraction.preferredSkills.join(", ") || "none identified"}</p><p>Unclassified: {result.originalExtraction.unclassifiedSkills.join(", ") || "none identified"}</p></details>
    </Card>
  </>;
}
