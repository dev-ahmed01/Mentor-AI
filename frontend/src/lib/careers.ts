import "server-only";

import { apiRequest } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { CareerDetail, CareerSummary } from "@/types/api";

async function authenticatedToken(): Promise<string> {
  const token = await getToken();
  if (!token) throw new Error("Authentication is required");
  return token;
}

export async function getCareers(): Promise<CareerSummary[]> {
  return apiRequest<CareerSummary[]>("/api/careers", { token: await authenticatedToken() });
}

export async function getCareer(slug: string): Promise<CareerDetail> {
  return apiRequest<CareerDetail>(`/api/careers/by-slug/${encodeURIComponent(slug)}`, {
    token: await authenticatedToken(),
  });
}
