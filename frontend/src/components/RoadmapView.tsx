import Link from "next/link";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import { RoadmapTaskEditor, RoadmapTitleEditor } from "@/components/RoadmapForms";
import type { Roadmap, RoadmapTask } from "@/types/api";

function TaskView({ task, roadmap }: { task: RoadmapTask; roadmap: Roadmap }) {
  const missing = task.prerequisites.filter((item) => !item.satisfied);
  return <li className="roadmap-task" id={`task-${task.id}`}>
    <div className="card-heading"><h4>{task.title}</h4><Badge>{task.state.toLowerCase().replaceAll("_", " ")}</Badge></div>
    <p>{task.skillName} · Learning target: {task.targetProficiency.toLowerCase()} · {task.estimatedHours} estimated total hours</p>
    {task.satisfiedAtGeneration ? <p>Recorded learning target was already met when this roadmap was created.</p> : null}
    {missing.length ? <p className="roadmap-blocker">Foundations to review: {missing.map((item) => item.skillName).join(", ")}. Complete these tasks or review the recorded evidence before starting.</p>
      : <p>{task.prerequisites.length ? "Prerequisites satisfied by recorded skills or completed roadmap tasks." : "No prerequisites recorded in the starter graph."}</p>}
    <details className="decision-why"><summary>Why this order<span className="sr-only"> for {task.skillName}</span></summary><p>{task.orderingReason}</p>
      {task.prerequisites.length ? <ul>{task.prerequisites.map((item) => <li key={item.taskId}><a href={`#task-${item.taskId}`} className="text-link">{item.skillName}</a>: {item.satisfied ? "satisfied" : "not yet satisfied"}{item.satisfiedAtGeneration ? " by the saved profile" : ""}</li>)}</ul> : null}
    </details>
    <details className="roadmap-edit"><summary>Edit task<span className="sr-only"> for {task.skillName}</span></summary>
      <RoadmapTaskEditor key={`${task.id}-${roadmap.revision}`} roadmapId={roadmap.id} revision={roadmap.revision} task={{ id: task.id, title: task.title, estimatedHours: task.estimatedHours, state: task.state, ready: task.ready }} />
    </details>
  </li>;
}

export function RoadmapView({ roadmap }: { roadmap: Roadmap }) {
  const tasks = roadmap.phases.flatMap((phase) => phase.tasks);
  const currentPhase = roadmap.phases.find((phase) => phase.id === roadmap.currentPhaseId);
  const completed = tasks.filter((task) => task.state === "COMPLETED");
  return <div className="roadmap-content">
    <Card>
      <div className="card-heading"><span>{roadmap.careerName}</span><Badge>Saved · DEMO DATA</Badge></div>
      <h2>{roadmap.title}</h2>
      <Link href={`/simulator?careerId=${roadmap.careerId}`} className="text-link">Explore what learning a skill could unlock</Link>
      <p>Revision {roadmap.revision} · {roadmap.weeklyHours} hours/week saved with this plan.</p>
      <p>Effort estimates are editable starting points, not validated mastery times. Stages describe learning order, not fixed calendar weeks. Market evidence is unavailable.</p>
      <details className="roadmap-edit"><summary>Rename roadmap</summary><RoadmapTitleEditor key={`${roadmap.id}-${roadmap.revision}`} id={roadmap.id} revision={roadmap.revision} title={roadmap.title} /></details>
      {roadmap.previousRoadmapId ? <Link href={`/roadmap?id=${roadmap.previousRoadmapId}`} className="text-link">View previous roadmap</Link> : null}
    </Card>
    <div className="roadmap-overview">
      <Card><p className="eyebrow">Current phase</p><h3>{currentPhase?.title ?? "No unfinished tasks"}</h3>
        <p className="eyebrow">Next action</p>{roadmap.nextAction ? <><h3>{roadmap.nextAction.title}</h3><Badge>Learn now</Badge><p>Use the task editor below to record your progress.</p></> : <p>{currentPhase ? "Remaining work needs prerequisite review. Skipped foundations do not unlock later tasks." : "Review completed and skipped work below. You can reopen a task when needed."}</p>}
      </Card>
      <Card><h3>Suggested focus</h3><p>A focus suggestion within your roadmap’s saved availability. Your weekly plan keeps a separate record of what you chose to work on.</p>
        {roadmap.thisWeek.length ? <ul>{roadmap.thisWeek.map((item) => <li key={item.taskId}>{item.title}<strong className="weekly-hours">{item.plannedHours} hours of focus</strong></li>)}</ul> : <p className="empty-state">No actionable tasks are assigned.</p>}
        <p>Changing your profile does not silently regenerate this saved roadmap.</p>
        <Link href="/progress" className="text-link">Open weekly plan and check-in</Link>
      </Card>
    </div>
    <section aria-labelledby="roadmap-stages-title"><h2 id="roadmap-stages-title">Your learning stages</h2><p>The current stage is open. Expand later stages when you need them.</p>
      <div className="roadmap-phases">{roadmap.phases.map((phase) => <details className="card roadmap-phase" key={`${phase.id}-${roadmap.revision}`} open={phase.id === roadmap.currentPhaseId}>
        <summary>{phase.title} · {phase.tasks.filter((task) => !["COMPLETED", "SKIPPED"].includes(task.state)).length} unfinished{phase.id !== roadmap.currentPhaseId ? " · Later or reviewed" : " · Current"}</summary>
        <ol className="learning-decision-list">{phase.tasks.map((task) => <TaskView key={task.id} task={task} roadmap={roadmap} />)}</ol>
      </details>)}</div>
    </section>
    <Card><h3>Completed</h3>{completed.length ? <ul>{completed.map((task) => <li key={task.id}>{task.title}</li>)}</ul> : <p>No tasks marked completed yet. Work at a pace you can sustain.</p>}
      <p>{tasks.filter((task) => task.state === "SKIPPED").length} tasks skipped, including recorded learning targets already met at generation. Skipped is separate from completed.</p>
    </Card>
  </div>;
}
