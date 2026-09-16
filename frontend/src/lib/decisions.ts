import "server-only";

import { apiRequest } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { LearningPrioritiesResponse } from "@/types/api";

export async function getLearningPriorities(careerId: string): Promise<LearningPrioritiesResponse> {
  const token = await getToken();
  if (!token) throw new Error("Authentication is required");
  return apiRequest<LearningPrioritiesResponse>(
    `/api/decisions/learning-priorities?careerId=${encodeURIComponent(careerId)}`, { token },
  );
}
