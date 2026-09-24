"use server";

import { redirect } from "next/navigation";
import { getToken } from "@/lib/auth";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import type { ActionState } from "@/lib/forms";
import type { JobAnalysis, JobDraft, JobExtractState } from "@/types/jobs";

function errorMessage(error: unknown) {
  if (error instanceof ApiClientError) {
    const fields = Object.entries(error.detail.fieldErrors ?? {}).map(([field, message]) => `${field}: ${message}`).join("; ");
    return fields || error.detail.message;
  }
  return "Job analysis is temporarily unavailable. Your inputs are still here; try again.";
}
export async function extractJobAction(previous: JobExtractState, form: FormData): Promise<JobExtractState> {
  const token = await getToken();
  if (!token) redirect("/login");
  try {
    return { draft: await apiRequest<JobDraft>("/api/jobs/extract", { method: "POST", token, body: JSON.stringify({ description: String(form.get("description") ?? "") }) }) };
  } catch (error) { return { draft: previous.draft, error: errorMessage(error) }; }
}
export async function saveJobAnalysisAction(_previous: ActionState, form: FormData): Promise<ActionState> {
  void _previous;
  const token = await getToken();
  if (!token) redirect("/login");
  const value = (name: string) => String(form.get(name) ?? "");
  const names = (name: string) => value(name).split(/[\n,]+/).map(s => s.trim()).filter(Boolean);
  let saved: JobAnalysis;
  try {
    saved = await apiRequest<JobAnalysis>("/api/jobs/analyses", { method: "POST", token, body: JSON.stringify({
      description: value("description"), title: value("title"), responsibilities: value("responsibilities"),
      experience: value("experience"), location: value("location"), technologies: value("technologies"),
      requiredSkills: names("requiredSkills"), preferredSkills: names("preferredSkills"), unclassifiedSkills: names("unclassifiedSkills"),
      reviewed: form.get("reviewed") === "on",
    }) });
  } catch (error) { return { error: errorMessage(error) }; }
  redirect(`/jobs/analyses/${saved.id}`);
}
