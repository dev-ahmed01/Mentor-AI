import "server-only";
import { apiRequest } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { DemoStatus } from "@/types/demo";
export async function getDemoStatus(): Promise<DemoStatus | null> {
  const token = await getToken();
  if (!token) return null;
  try { return await apiRequest<DemoStatus>("/api/demo", { token }); } catch { return null; }
}
