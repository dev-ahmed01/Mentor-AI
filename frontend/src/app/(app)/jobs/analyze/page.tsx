import Link from "next/link";
import type { Metadata } from "next";
import { JobAnalysisForm } from "@/components/JobAnalysisForm";

export const metadata: Metadata = { title: "Analyze a job description" };
export default function AnalyzeJobPage() {
  return <div className="progress-stack"><div className="page-heading"><div><p className="eyebrow">Prepare for a specific role</p><h1>Analyze a job description</h1>
    <p>Review the requirements, compare them with your recorded skills and identify preparation priorities.</p></div><Link href="/profile">Review your profile</Link></div><JobAnalysisForm /></div>;
}
