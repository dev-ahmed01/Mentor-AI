"use server";

import { redirect } from "next/navigation";
import { getToken } from "@/lib/auth";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import type { SimulationActionState, SkillSimulation } from "@/types/simulator";

export async function simulateSkillAction(targetCareerId: string, skillId: string): Promise<SimulationActionState> {
  const token = await getToken();
  if (!token) redirect("/login");
  try {
    const result = await apiRequest<SkillSimulation>("/api/simulator/skill", {
      method: "POST", token, body: JSON.stringify({ skillId, targetCareerId }),
    });
    return { result };
  } catch (error) {
    return { error: error instanceof ApiClientError ? error.detail.message : "The simulation is unavailable. Try again in a moment." };
  }
}
