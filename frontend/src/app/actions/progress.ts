"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { ActionState } from "@/lib/forms";
import type { WeeklyPlan, WeeklyCheckIn } from "@/types/progress";

export async function startWeeklyPlanAction(roadmapId: string): Promise<ActionState> {
  const token = await getToken();
  if (!token) redirect("/login");
  try {
    await apiRequest<WeeklyPlan>("/api/weekly-plan", { method: "POST", token, body: JSON.stringify({ roadmapId }) });
  } catch (error) {
    return { error: error instanceof ApiClientError ? error.detail.message : "Your week could not be started." };
  }
  revalidatePath("/progress");
  redirect("/progress");
}

export async function submitCheckInAction(planId: string, revision: number, planRevision: number, taskIds: string[],
  _: ActionState, form: FormData): Promise<ActionState> {
  const token = await getToken();
  if (!token) redirect("/login");
  const text = (name: string) => String(form.get(name) ?? "");
  const optionalRating = (name: string) => text(name) === "" ? null : Number(text(name));
  const constraintType = text("constraintType");
  const payload = {
    planId, expectedRoadmapRevision: revision, expectedPlanRevision: planRevision,
    actualHours: text("actualHours") === "" ? null : Number(text("actualHours")),
    availableHoursNextWeek: text("availableHoursNextWeek") === "" ? null : Number(text("availableHoursNextWeek")),
    difficultyRating: optionalRating("difficultyRating"), confidenceRating: optionalRating("confidenceRating"),
    energyOrCapacityBand: text("energyOrCapacityBand"), blockers: form.getAll("blockers").map(String),
    notes: text("notes").trim() || null,
    constraint: constraintType ? { type: constraintType, startDate: text("constraintStart"), endDate: text("constraintEnd") } : null,
    tasks: taskIds.map((taskId) => ({ taskId, outcome: text(`outcome-${taskId}`) })),
  };
  let saved: WeeklyCheckIn;
  try {
    saved = await apiRequest<WeeklyCheckIn>("/api/check-ins", { method: "POST", token, body: JSON.stringify(payload) });
  } catch (error) {
    return { error: error instanceof ApiClientError ? error.detail.message : "Your check-in could not be saved. Your answers are still here." };
  }
  revalidatePath("/progress");
  revalidatePath("/roadmap");
  revalidatePath("/dashboard");
  redirect(`/progress?weekStart=${encodeURIComponent(saved.weekStart)}&saved=1`);
}

export async function acceptAdaptationAction(id: string, roadmapRevision: number, planRevision: number,
  weekStart: string, _: ActionState, form: FormData): Promise<ActionState> {
  const token = await getToken();
  if (!token) redirect("/login");
  const edit = form.get("editing") === "yes" ? {
    capacityHours: form.get("capacityHours") === "" ? null : Number(form.get("capacityHours")),
    tasks: form.getAll("selectedTask").map(String).map((taskId) => ({
      taskId, plannedHours: form.get(`hours-${taskId}`) === "" ? null : Number(form.get(`hours-${taskId}`)),
    })),
  } : null;
  try {
    await apiRequest(`/api/adaptations/${encodeURIComponent(id)}/accept`, {
      method: "POST", token, body: JSON.stringify({ expectedRoadmapRevision: roadmapRevision, expectedPlanRevision: planRevision, edit }),
    });
  } catch (error) {
    return { error: error instanceof ApiClientError ? error.detail.message : "Your revised plan could not be saved. Your edits are still here." };
  }
  revalidatePath("/progress"); revalidatePath("/roadmap"); revalidatePath("/dashboard");
  redirect(`/progress?weekStart=${encodeURIComponent(weekStart)}`);
}
