import Link from "next/link";
import { Card } from "@/components/ui/Card";
import { JobAnalysisResult } from "@/components/JobAnalysisResult";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { JobAnalysis } from "@/types/jobs";

export default async function JobAnalysisPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  let result: JobAnalysis;
  try { result = await apiRequest<JobAnalysis>(`/api/jobs/analyses/${encodeURIComponent(id)}`, { token: await getToken() }); }
  catch (error) { return <Card><h1>Job comparison unavailable</h1><p>{error instanceof ApiClientError && [400, 404].includes(error.detail.status) ? "This comparison does not exist or belongs to another account." : "The service is temporarily unavailable. Reload this page to try again."}</p><Link href="/jobs/analyze">Analyze a job description</Link></Card>; }
  return <div className="progress-stack"><div className="page-heading"><h1>{result.reviewedJob.title || "Untitled job"}: saved comparison</h1><Link href="/jobs/analyze">Analyze another description</Link></div><JobAnalysisResult result={result} /></div>;
}
