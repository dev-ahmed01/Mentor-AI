import Link from "next/link";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import type { WeeklyCheckIn, WeeklyPlan, WeeklyOutcome } from "@/types/progress";

const readable = (value: string) => value.toLowerCase().replaceAll("_", " ");

export function WeeklyPlanView({ plan, next = false }: { plan: WeeklyPlan; next?: boolean }) {
  return <Card>
    <div className="card-heading"><h2>{next ? "Next week’s realistic plan" : "Your saved week"}</h2><Badge>Week of {plan.weekStart}</Badge></div>
    <p>{plan.roadmapTitle}</p>
    <p><strong>{plan.plannedHours} hours allocated</strong> within this plan’s saved capacity of {plan.capacityHours} hours.</p>
    {plan.tasks.length ? <ul className="weekly-plan-list">{plan.tasks.map((task) => <li key={task.taskId}><span>{task.title}</span><strong>{task.plannedHours} hours</strong></li>)}</ul>
      : <p>{plan.capacityHours === 0 ? "Space to rest or focus on other commitments. No learning tasks are allocated." : "No eligible tasks were allocated. Review your roadmap when you’re ready."}</p>}
    <details className="decision-why"><summary>Why this plan?</summary><p>{plan.reason}</p></details>
    <p className="field-hint">This is a saved allocation. Roadmap edits do not rewrite its planned hours.</p>
    <Link href={`/roadmap?id=${plan.roadmapId}`} className="text-link">View this roadmap</Link>
  </Card>;
}

function OutcomeList({ checkIn, outcomes, empty }: { checkIn: WeeklyCheckIn; outcomes: WeeklyOutcome[]; empty: string }) {
  const tasks = checkIn.tasks.filter((task) => outcomes.includes(task.outcome));
  return tasks.length ? <ul>{tasks.map((task) => <li key={task.taskId}>{task.title} <span className="decision-record">({readable(task.outcome)})</span></li>)}</ul> : <p>{empty}</p>;
}

export function WeeklyCheckInView({ checkIn }: { checkIn: WeeklyCheckIn }) {
  return <div className="progress-stack">
    <Card>
      <div className="card-heading"><h2>Your week, recorded</h2><Badge>Saved</Badge></div>
      <div className="weekly-metrics"><div><span>Planned</span><strong>{checkIn.plannedHours} hours</strong></div><div><span>Actual</span><strong>{checkIn.actualHours} hours</strong></div></div>
      <p>Next-week availability you reported: <strong>{checkIn.availableHoursNextWeek} hours</strong>.</p>
      {checkIn.availableHoursNextWeek !== checkIn.nextPlan.capacityHours ? <p>The preserved next-week plan has a different saved capacity ({checkIn.nextPlan.capacityHours} hours). Your reported availability is recorded here; the existing plan was not rewritten.</p> : null}
      <p>Energy or capacity: {readable(checkIn.energyOrCapacityBand)}. {checkIn.confidenceRating ? `Topic confidence: ${checkIn.confidenceRating}/5.` : "Topic confidence not rated."} {checkIn.difficultyRating ? `Difficulty: ${checkIn.difficultyRating}/5.` : ""}</p>
      {checkIn.blockers.length ? <p>Reported blockers: {checkIn.blockers.map(readable).join(", ")}.</p> : null}
      {checkIn.constraint ? <p>Temporary constraint: {readable(checkIn.constraint.type)}, {checkIn.constraint.startDate} through {checkIn.constraint.endDate}. No private explanation needed.</p> : null}
      {checkIn.notes ? <details><summary>Your optional note</summary><p className="progress-note">{checkIn.notes}</p></details> : null}
    </Card>
    <div className="roadmap-overview">
      <Card><h3>Completed</h3><OutcomeList checkIn={checkIn} outcomes={["COMPLETED"]} empty="No tasks reported completed. Your effort still matters." /></Card>
      <Card><h3>Carried forward</h3><OutcomeList checkIn={checkIn} outcomes={["PARTIAL", "MISSED"]} empty="No partial or missed tasks to carry forward." /><p>Remaining work stays eligible. Next week’s allocation below shows what fits.</p></Card>
    </div>
    <Card><h3>Removed from next week / deferred</h3><OutcomeList checkIn={checkIn} outcomes={["DEFERRED"]} empty="No tasks were explicitly deferred." /><p>Deferred work stays in the roadmap and is excluded from a new next-week allocation. An already saved plan is preserved as recorded.</p>
      <h3>Why the plan changed</h3><p>{checkIn.explanation}</p>
    </Card>
    <WeeklyPlanView plan={checkIn.nextPlan} next />
  </div>;
}
