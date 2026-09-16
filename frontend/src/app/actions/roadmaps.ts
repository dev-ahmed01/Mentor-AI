"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { ActionState } from "@/lib/forms";
import type { Roadmap } from "@/types/api";

export async function createRoadmapAction(_: ActionState, form: FormData): Promise<ActionState> {
  const token = await getToken();
  if (!token) redirect("/login");
  let roadmap: Roadmap;
  try {
    roadmap = await apiRequest<Roadmap>("/api/roadmaps", {
      method: "POST", token, body: JSON.stringify({ careerId: String(form.get("careerId") ?? "") }),
    });
  } catch (error) {
    return { error: error instanceof ApiClientError ? error.detail.message : "Your roadmap could not be created." };
  }
  revalidatePath("/roadmap");
  redirect(`/roadmap?id=${roadmap.id}`);
}

export async function updateRoadmapAction(roadmapId: string, revision: number, taskId: string | null,
  _: ActionState, form: FormData): Promise<ActionState> {
  const token = await getToken();
  if (!token) redirect("/login");
  const title = String(form.get("title") ?? "");
  const payload = taskId ? {
    expectedRevision: revision,
    tasks: [{ id: taskId, title, estimatedHours: Number(form.get("estimatedHours")), state: String(form.get("state") ?? "") }],
  } : { expectedRevision: revision, title, tasks: [] };
  try {
    await apiRequest<Roadmap>(`/api/roadmaps/${encodeURIComponent(roadmapId)}`, {
      method: "PUT", token, body: JSON.stringify(payload),
    });
  } catch (error) {
    return { error: error instanceof ApiClientError ? error.detail.message : "Your changes could not be saved." };
  }
  revalidatePath("/roadmap");
  redirect(`/roadmap?id=${encodeURIComponent(roadmapId)}&saved=1`);
}
