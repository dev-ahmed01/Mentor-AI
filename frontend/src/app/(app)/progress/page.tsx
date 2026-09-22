import Link from "next/link";
import type { Metadata } from "next";
import { Card } from "@/components/ui/Card";
import { StartWeeklyPlanForm, WeeklyCheckInForm } from "@/components/WeeklyCheckInForm";
import { WeeklyPlanView, WeeklyCheckInView } from "@/components/WeeklyProgressView";
import { getRoadmap } from "@/lib/roadmaps";
import { getWeeklyPlan, getWeeklyCheckIn, getProgressHistory } from "@/lib/progress";

export const metadata: Metadata = { title: "Weekly progress" };

export default async function ProgressPage({ searchParams }: { searchParams: Promise<{ weekStart?: string; page?: string; saved?: string }> }) {
  const query = await searchParams;
  const today = new Date();
  today.setUTCHours(0, 0, 0, 0);
  today.setUTCDate(today.getUTCDate() - (today.getUTCDay() + 6) % 7);
  const currentWeek = today.toISOString().slice(0, 10);
  const week = query.weekStart ?? currentWeek;
  const parsed = typeof week === "string" && /^\d{4}-\d{2}-\d{2}$/.test(week) ? new Date(`${week}T00:00:00Z`) : null;
  const validWeek = parsed && !Number.isNaN(parsed.getTime()) && parsed.toISOString().slice(0, 10) === week && parsed.getUTCDay() === 1 && week <= currentWeek;
  const page = typeof query.page === "string" && /^\d{1,4}$/.test(query.page) ? Math.min(1000, Number(query.page)) : 0;
  const [plan, checkIn, history, roadmap] = await Promise.all([
    validWeek ? getWeeklyPlan(week) : Promise.resolve(null), validWeek ? getWeeklyCheckIn(week) : Promise.resolve(null),
    getProgressHistory(page), getRoadmap(),
  ]);
  return <div className="progress-stack">
    <div className="page-heading"><div><p className="eyebrow">Make room for real life</p><h1>Weekly check-in</h1><p>About 1–2 minutes. No perfect week required.</p></div><Link href="/dashboard" className="text-link">Back to dashboard</Link></div>
    <p>Week of {validWeek ? week : currentWeek}. Weeks run Monday–Sunday in UTC.</p>
    {week !== currentWeek ? <Link href="/progress" className="text-link">Back to this week</Link> : null}
    {!validWeek ? <Card><h2>Choose a saved week</h2><p>Open this week or a previous week from your history below.</p></Card>
      : plan ? <>
        {query.saved === "1" && checkIn ? <p className="callout" role="status">Your check-in and next-week plan were saved.</p> : null}
        {checkIn ? <WeeklyCheckInView checkIn={checkIn} /> : <>
          <WeeklyPlanView plan={plan} />
          <Card><h2>{week < currentWeek ? "Reflect on this earlier week" : "A quick reflection"}</h2><WeeklyCheckInForm key={`${plan.id}-${plan.roadmapRevision}-${plan.revision}`} plan={plan} /></Card>
        </>}
      </> : <Card><h2>{week < currentWeek ? "No saved plan for this week" : "Start with a manageable week"}</h2>
        {week < currentWeek ? <p>Only saved weeks can be checked in. Start this week when you’re ready.</p>
          : roadmap ? <><p>Save a weekly allocation from {roadmap.title}. Your roadmap’s recorded availability is {roadmap.weeklyHours} hours. You can choose a different capacity for next week in your check-in.</p><StartWeeklyPlanForm roadmapId={roadmap.id} /></>
            : <><p>Create a roadmap first so your weekly tasks have a learning direction.</p><Link href="/roadmap" className="text-link">Create your roadmap</Link></>}
      </Card>}
    <Card><h2>Weekly history</h2><p>Saved reflections and weeks still waiting for a check-in. Earlier plans stay as recorded.</p>
      {history.items.length ? <ul className="weekly-history">{history.items.map((item) => <li key={item.plan.id}>
        <div><strong>Week of {item.plan.weekStart}</strong><span>{item.plan.plannedHours} planned hours{item.checkIn ? ` · ${item.checkIn.actualHours} actual hours` : ""}</span></div>
        {item.plan.weekStart > currentWeek ? <span>Upcoming plan</span> : <Link href={`/progress?weekStart=${item.plan.weekStart}&page=${page}`} className="text-link">{item.checkIn ? "View check-in" : "Record this week"}<span className="sr-only"> for {item.plan.weekStart}</span></Link>}
      </li>)}</ul> : <p>No weekly plans saved yet.</p>}
      <nav aria-label="Weekly history pages" className="progress-pagination">
        {page > 0 ? <Link href={`/progress?weekStart=${validWeek ? week : currentWeek}&page=${page - 1}`} className="text-link">Newer weeks</Link> : null}
        {history.hasNext ? <Link href={`/progress?weekStart=${validWeek ? week : currentWeek}&page=${page + 1}`} className="text-link">Older weeks</Link> : null}
      </nav>
    </Card>
  </div>;
}
