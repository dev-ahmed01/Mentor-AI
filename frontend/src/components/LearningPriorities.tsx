import Link from "next/link";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { SkillPrerequisiteContext } from "@/components/SkillPrerequisiteContext";
import { getCareers } from "@/lib/careers";
import { getLearningPriorities } from "@/lib/decisions";
import type { CareerSummary, LearningDecision, LearningPrioritiesResponse, LearningPriority } from "@/types/api";

const groups: Array<{ key: LearningPriority; title: string; description: string; empty: string }> = [
  { key: "LEARN_NOW", title: "Learn now", description: "Your immediate learning focus.", empty: "No immediate focus assigned. Check your weekly time and the prerequisites below." },
  { key: "LEARN_NEXT", title: "Learn next", description: "Skills ready to consider after your current focus.", empty: "No additional ready skills in this group." },
  { key: "LEARN_LATER", title: "Later", description: "Relevant skills outside your current focus limit.", empty: "No later skills in this selection." },
  { key: "NOT_YET", title: "Not yet", description: "Blocked by prerequisites, or already at the recorded learning target. These skills still have value.", empty: "No blocked or already-met learning targets." },
];

const reasonText: Record<string, string> = {
  TARGET_CAREER_REQUIRED_SKILL: "Required in this career's starter catalog.",
  TARGET_CAREER_PREFERRED_SKILL: "Preferred in this career's starter catalog.",
  FOUNDATION_FOR_REQUIRED_SKILL: "Builds a foundation for a required career skill.",
  FOUNDATION_FOR_PREFERRED_SKILL: "Builds a foundation for a preferred career skill.",
  PREREQUISITES_MET: "Your recorded foundations meet all modeled prerequisites.",
  PREREQUISITES_MISSING: "Build the missing foundations before starting this skill.",
  NO_RECORDED_PREREQUISITES: "No prerequisites are recorded in this small starter graph.",
  PREREQUISITE_BOTTLENECK: "This missing foundation blocks other career learning.",
  ALREADY_PROFICIENT: "Your recorded proficiency already meets this learning target; it is not recommended again.",
  TIME_BUDGET_CONSTRAINT: "Your current weekly focus limit keeps this outside Learn now.",
  WEEKLY_TIME_UNAVAILABLE: "Add weekly availability to choose an immediate learning focus.",
  HIGHER_PRIORITY_FOCUS_FIRST: "Eligible skills are ordered by relevance, foundations they unlock, and current learning distance.",
  MARKET_EVIDENCE_UNAVAILABLE: "No validated market evidence is available; it does not contribute to this ranking.",
};

function DecisionItem({ decision }: { decision: LearningDecision }) {
  const met = decision.reasonCodes.includes("ALREADY_PROFICIENT");
  return (
    <li className="learning-decision">
      <div className="card-heading"><h4>{decision.name}</h4>{met ? <Badge>Target already met</Badge> : null}</div>
      <p className="decision-record">Recorded: {decision.currentProficiency?.toLowerCase() ?? "not provided"} · Learning target: {decision.targetProficiency.toLowerCase()}</p>
      <SkillPrerequisiteContext readiness={decision.prerequisiteReadiness} />
      <details className="decision-why">
        <summary>Why this priority<span className="sr-only"> for {decision.name}</span></summary>
        <ul>{decision.reasonCodes.map((reason) => <li key={reason}>{reasonText[reason] ?? "Review your recorded profile and career context."}</li>)}</ul>
        <p>Priority points: {decision.deterministicScore}/100. An ordering rule, not a market or employment percentage.</p>
        {!met ? <p>Learning stage: {decision.estimatedEffortBand === "DEVELOPING" ? "develop your existing basics" : "build foundations"}. No completion-time estimate is established.</p> : null}
      </details>
    </li>
  );
}

export async function LearningPriorities({ careerId }: { careerId?: string | string[] }) {
  let careers: CareerSummary[];
  try {
    careers = await getCareers();
  } catch {
    return <Card><h2>Next Best Action</h2><p role="status">Career choices are temporarily unavailable.</p><Link href="/dashboard" className="text-link">Retry dashboard</Link></Card>;
  }
  const selected = typeof careerId === "string" ? careers.find((career) => career.id === careerId) : undefined;
  let result: LearningPrioritiesResponse | undefined;
  let unavailable = false;
  if (selected) {
    try { result = await getLearningPriorities(selected.id); } catch { unavailable = true; }
  }
  return (
    <section className="learning-priorities" id="learning-priorities" aria-labelledby="learning-priorities-title">
      <Card>
        <div className="card-heading"><span className="eyebrow">Your next learning focus</span><Badge>Calculated · DEMO DATA</Badge></div>
        <h2 id="learning-priorities-title">Next Best Action</h2>
        <p>Choose a career to compare its skills with your current foundations and weekly availability. You can explore another direction at any time.</p>
        <form action="/dashboard#learning-priorities" method="get" className="decision-selector">
          <div><label htmlFor="decision-career">Target career</label><select id="decision-career" name="careerId" defaultValue={selected?.id ?? ""} required>
            <option value="" disabled>Choose a career</option>
            {careers.map((career) => <option key={career.id} value={career.id}>{career.name}</option>)}
          </select></div>
          <button className="button button-primary" type="submit" disabled={careers.length === 0}>Show learning priorities</button>
        </form>
        {!selected ? <p className="empty-state" role="status">{careerId ? "That career selection is unavailable. Choose a career from the list." : "Select a target career to see Learn now, Learn next, Later, and Not yet."}</p> : null}
        {unavailable ? <p role="status">Learning priorities are temporarily unavailable. Use “Show learning priorities” to try again.</p> : null}
        {result ? <>
          <h3>{result.careerName}</h3>
          <Link href={`/roadmap?careerId=${result.careerId}`} className="button button-primary">Create or view roadmap</Link>
          <p>{result.weeklyHours != null ? `${result.weeklyHours} hours/week recorded · Immediate focus limit: ${result.immediateFocusLimit}.` : "Weekly time is not recorded. Add availability in your profile before choosing an immediate focus."} <Link href="/profile" className="text-link">Edit profile</Link></p>
          <p>Focus limits help avoid overload; they do not promise completion this week. Career skills use an intermediate learning target, while prerequisite-only foundations use beginner. These are illustrative self-report thresholds, not assessed competence.</p>
          <p className="decision-evidence">Market evidence unavailable. This ranking uses your profile and an illustrative skill graph; it contains no hiring or demand estimates.</p>
        </> : null}
      </Card>
      {result ? <div className="learning-priority-grid">{groups.map((group) => {
        const decisions = result.decisions.filter((decision) => decision.priority === group.key);
        return <Card key={group.key}><h3>{group.title} <span className="decision-count">({decisions.length})</span></h3><p>{group.description}</p>
          {decisions.length ? <ul className="learning-decision-list">{decisions.map((decision) => <DecisionItem key={decision.skillId} decision={decision} />)}</ul> : <p className="empty-state">{group.empty}</p>}
        </Card>;
      })}</div> : null}
    </section>
  );
}
