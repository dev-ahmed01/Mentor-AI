import "server-only";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { Roadmap } from "@/types/api";

export async function getRoadmap(id?: string): Promise<Roadmap | null> {
  const token = await getToken();
  if (!token) throw new Error("Authentication is required");
  try {
    return await apiRequest<Roadmap>(`/api/roadmaps/${id ? encodeURIComponent(id) : "current"}`, { token });
  } catch (error) {
    if (error instanceof ApiClientError && error.detail.status === 404) return null;
    throw error;
  }
}
