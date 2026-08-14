import "server-only";

import { apiRequest } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { Profile } from "@/types/api";

export async function getProfile(): Promise<Profile> {
  const token = await getToken();
  if (!token) throw new Error("Authentication is required");
  return apiRequest<Profile>("/api/profile", { token });
}
