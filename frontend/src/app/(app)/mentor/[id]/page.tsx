import Link from "next/link";
import { randomUUID } from "node:crypto";
import { Card } from "@/components/ui/Card";
import { MentorComposer } from "@/components/MentorForms";
import { apiRequest } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { MentorHistory } from "@/types/mentor";
export default async function ConversationPage({ params, searchParams }: { params: Promise<{ id: string }>; searchParams: Promise<{ page?: string | string[] }> }) {
  const { id } = await params; const query = await searchParams; const page = typeof query.page === "string" ? Number(query.page) : 0;
  let history: MentorHistory;
  try { history = await apiRequest<MentorHistory>(`/api/mentor/conversations/${encodeURIComponent(id)}?page=${Number.isInteger(page) ? page : -1}`, { token: await getToken() }); }
  catch { return <Card><h1>Conversation unavailable</h1><p>The conversation may belong to another account, the page may be invalid, or the service may be unavailable.</p><Link href="/mentor">Back to mentor</Link></Card>; }
  return <div className="progress-stack"><div className="page-heading"><h1>{history.conversation.careerName}: mentor conversation</h1><Link href="/mentor">All conversations</Link></div>
    <Card><p>Replies are historical snapshots of the facts available when you asked. They do not change your profile or learning plans.</p><p>Topic memory: {history.conversation.memory || "No prior topics yet"}. Only a bounded recent context is sent to the local model.</p></Card>
    {history.turns.length ? history.turns.map(turn => <Card key={turn.id}><article aria-label={`Message ${turn.revision}`}>
      <h2>You asked</h2><p className="job-source-text">{turn.question}</p><p className="eyebrow">{turn.status === "ANSWERED" ? "AI-selected evidence · facts rendered by MentorAI" : "AI unavailable · no generated advice"}</p>
      <p className="job-source-text">{turn.answer}</p><p>{turn.marketNotice}</p>
      {turn.nextStep ? <p><Link href={turn.nextStep.href}>{turn.nextStep.label}</Link></p> : <Link href="/dashboard">Use deterministic learning priorities</Link>}
      {turn.citations.length ? <details><summary>Evidence and provenance</summary><ul>{turn.citations.map(fact => <li key={fact.id}><strong>{fact.kind}</strong>: {fact.text} <Link href={fact.href}>Inspect current source</Link>
        {Object.entries(fact.provenance).map(([name, value]) => <p key={name}>{name}: {value}</p>)}</li>)}</ul></details> : null}
      <p>Saved {turn.createdAt.slice(0, 19).replace("T", " ")} UTC · {turn.promptVersion}. Context captured {turn.context.capturedAt.slice(0, 19).replace("T", " ")} UTC.</p>
    </article></Card>) : <Card><p>No messages yet. Ask about a learning decision or a temporary constraint.</p></Card>}
    <nav aria-label="Conversation history">{history.hasOlder ? <Link href={`/mentor/${id}?page=${page + 1}`}>Older messages</Link> : null}{page > 0 ? <> · <Link href={`/mentor/${id}?page=${page - 1}`}>Newer messages</Link></> : null}</nav>
    {page === 0 && history.conversation.revision < 60 ? <MentorComposer key={history.conversation.revision} id={id} revision={history.conversation.revision} initialRequestId={randomUUID()} /> : <Card><Link href={page > 0 ? `/mentor/${id}` : "/mentor"}>{page > 0 ? "Return to latest messages to reply" : "Start another conversation (60-message limit reached)"}</Link></Card>}
  </div>;
}
