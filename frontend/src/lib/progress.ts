import "server-only";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { WeeklyPlan, WeeklyCheckIn, ProgressHistory } from "@/types/progress";

async function read<T>(path: string): Promise<T | null> {
  const token = await getToken();
  if (!token) throw new Error("Authentication is required");
  try { return await apiRequest<T>(path, { token }); }
  catch (error) {
    if (error instanceof ApiClientError && error.detail.status === 404) return null;
    throw error;
  }
}
const weekQuery = (week?: string) => week ? `?weekStart=${encodeURIComponent(week)}` : "";
export const getWeeklyPlan = (week?: string) => read<WeeklyPlan>(`/api/weekly-plan/current${weekQuery(week)}`);
export const getWeeklyCheckIn = (week?: string) => read<WeeklyCheckIn>(`/api/check-ins/current${weekQuery(week)}`);
export async function getProgressHistory(page: number): Promise<ProgressHistory> {
  const token = await getToken();
  if (!token) throw new Error("Authentication is required");
  return apiRequest<ProgressHistory>(`/api/check-ins/history?page=${page}`, { token });
}
