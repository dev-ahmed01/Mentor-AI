"use server";
import { redirect } from "next/navigation";
import { getToken } from "@/lib/auth";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import type { ActionState } from "@/lib/forms";
import type { MentorConversation } from "@/types/mentor";

function errorText(error: unknown) { return error instanceof ApiClientError ? error.detail.message : "The mentor service is unavailable. Your input is still here. Reload the conversation to check whether the message was saved before retrying."; }
export async function createMentorAction(previous: ActionState, form: FormData): Promise<ActionState> {
  void previous; const token = await getToken(); if (!token) redirect("/login");
  let conversation: MentorConversation;
  try { conversation = await apiRequest<MentorConversation>("/api/mentor/conversations", { method: "POST", token, body: JSON.stringify({ careerId: String(form.get("careerId") ?? ""), jobAnalysisId: String(form.get("jobAnalysisId") ?? "").trim() || null }) }); }
  catch (error) { return { error: errorText(error) }; }
  redirect(`/mentor/${conversation.id}`);
}
export async function sendMentorAction(id: string, previous: ActionState, form: FormData): Promise<ActionState> {
  void previous; const token = await getToken(); if (!token) redirect("/login");
  const revision = Number(form.get("expectedRevision"));
  if (!Number.isSafeInteger(revision) || revision < 0) return { error: "Reload this conversation before sending." };
  try { await apiRequest(`/api/mentor/conversations/${encodeURIComponent(id)}/messages`, { method: "POST", token, body: JSON.stringify({ question: String(form.get("question") ?? ""), requestId: String(form.get("requestId") ?? ""), expectedRevision: revision }) }); }
  catch (error) { return { error: errorText(error) }; }
  redirect(`/mentor/${id}`);
}
