"use server";
import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { getToken } from "@/lib/auth";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import type { ActionState } from "@/lib/forms";
function message(error: unknown) { return error instanceof ApiClientError ? error.detail.message : "Demo setup is temporarily unavailable. Reload the guide to check the saved state before retrying."; }
export async function startDemoAction(previous: ActionState, form: FormData): Promise<ActionState> {
  void previous; const token = await getToken(); if (!token) redirect("/login");
  if (form.get("confirmSynthetic") !== "yes") return { error: "Please confirm that this is an empty account for synthetic demo data." };
  try { await apiRequest("/api/demo/start", { method: "POST", token, body: JSON.stringify({ confirmSynthetic: true }) }); }
  catch (error) { return { error: message(error) }; }
  revalidatePath("/", "layout"); redirect("/demo");
}
export async function examDemoAction(previous: ActionState, form: FormData): Promise<ActionState> {
  void previous; const token = await getToken(); if (!token) redirect("/login");
  const rawRoadmap = form.get("expectedRoadmapRevision"), rawPlan = form.get("expectedPlanRevision");
  const roadmap = Number(rawRoadmap), plan = Number(rawPlan);
  if (rawRoadmap === null || rawPlan === null || !Number.isSafeInteger(roadmap) || !Number.isSafeInteger(plan) || roadmap < 0 || plan < 0) return { error: "Reload the demo guide before recording the exam scenario." };
  if (form.get("confirmExam") !== "yes") return { error: "Confirm the synthetic check-in before recording it." };
  try { await apiRequest("/api/demo/exam", { method: "POST", token, body: JSON.stringify({ expectedRoadmapRevision: roadmap, expectedPlanRevision: plan }) }); }
  catch (error) { return { error: message(error) }; }
  revalidatePath("/", "layout"); redirect("/progress?saved=1");
}
