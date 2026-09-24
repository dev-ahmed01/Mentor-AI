"use server";
import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { getToken } from "@/lib/auth";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import type { ActionState } from "@/lib/forms";
import type { PivotDetail } from "@/types/pivot";

function errorText(error: unknown) { return error instanceof ApiClientError ? error.detail.message : "The pivot service is unavailable. Reload this comparison to check its status before retrying."; }
export async function createPivotAction(previous: ActionState, form: FormData): Promise<ActionState> {
  void previous; const token = await getToken(); if (!token) redirect("/login");
  let result: PivotDetail;
  try { result = await apiRequest<PivotDetail>("/api/pivots", { method: "POST", token, body: JSON.stringify({ sourceRoadmapId: String(form.get("sourceRoadmapId") ?? ""), targetCareerId: String(form.get("targetCareerId") ?? "") }) }); }
  catch (error) { return { error: errorText(error) }; }
  redirect(`/pivot/${result.id}`);
}
export async function acceptPivotAction(id: string, previous: ActionState, form: FormData): Promise<ActionState> {
  void previous; const token = await getToken(); if (!token) redirect("/login");
  const raw = form.get("expectedSourceRevision"); const revision = Number(raw);
  if (raw === null || !Number.isSafeInteger(revision) || revision < 0 || form.get("confirm") !== "yes") return { error: "Confirm the switch using the current saved preview." };
  try { await apiRequest<PivotDetail>(`/api/pivots/${encodeURIComponent(id)}/accept`, { method: "POST", token, body: JSON.stringify({ expectedSourceRevision: revision }) }); }
  catch (error) { return { error: errorText(error) }; }
  revalidatePath("/roadmap"); revalidatePath("/dashboard"); revalidatePath("/pivot");
  redirect(`/pivot/${id}`);
}
