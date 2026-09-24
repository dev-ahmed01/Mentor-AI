import Link from "next/link";
import type { Metadata } from "next";
import { Card } from "@/components/ui/Card";
import { CreateMentorForm } from "@/components/MentorForms";
import { getCareers } from "@/lib/careers";
import { getToken } from "@/lib/auth";
import { apiRequest } from "@/lib/api/client";
import type { MentorConversation, MentorStatus } from "@/types/mentor";
export const metadata: Metadata = { title: "AI mentor" };
export default async function MentorPage({ searchParams }: { searchParams: Promise<{ jobAnalysisId?: string | string[] }> }) {
  const query = await searchParams; const token = await getToken();
  let data: [Awaited<ReturnType<typeof getCareers>>, MentorConversation[], MentorStatus];
  try {
    data = await Promise.all([getCareers(), apiRequest<MentorConversation[]>("/api/mentor/conversations", { token }), apiRequest<MentorStatus>("/api/mentor/status", { token })]);
  } catch { return <Card><h1>AI mentor</h1><p>Conversation data is temporarily unavailable.</p><Link href="/mentor">Retry</Link></Card>; }
    const [careers, conversations, status] = data;
    return <div className="progress-stack"><div className="page-heading"><div><p className="eyebrow">Grounded guidance</p><h1>AI mentor</h1><p>Discuss your recorded learning priorities, progress and evidence. Replies cite saved facts and suggest a next step for you to review.</p></div></div>
      <Card><p>{status.enabled ? "Local AI is configured. Responses depend on the model being available." : "AI mentor is currently unavailable: local AI is disabled. You can keep a conversation and use the deterministic learning tools."}</p>
        <p>The model selects relevant evidence; MentorAI renders the recorded facts and deterministic explanations. It does not provide unrestricted advice or invent scores.</p><Link href="/dashboard">View learning priorities</Link></Card>
      <CreateMentorForm careers={careers} jobAnalysisId={typeof query.jobAnalysisId === "string" ? query.jobAnalysisId : undefined} />
      <Card><h2>Recent conversations</h2>{conversations.length ? <ul>{conversations.map(c => <li key={c.id}><Link href={`/mentor/${c.id}`}>{c.careerName}</Link> · {c.revision} messages · {c.updatedAt.slice(0, 10)}</li>)}</ul> : <p>No conversations yet.</p>}<p>The latest 20 conversations are shown. Keep an older conversation URL to revisit it.</p></Card>
    </div>;
}
