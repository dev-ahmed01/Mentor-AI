import "server-only";

import { cookies } from "next/headers";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import type { User } from "@/types/api";

export const AUTH_COOKIE = "mentorai_token";

export async function getToken(): Promise<string | undefined> {
  return (await cookies()).get(AUTH_COOKIE)?.value;
}

export async function getCurrentUser(): Promise<User | null> {
  const token = await getToken();
  if (!token) return null;
  try {
    return await apiRequest<User>("/api/auth/me", { token });
  } catch (error) {
    if (error instanceof ApiClientError && [401, 403].includes(error.detail.status)) {
      return null;
    }
    throw error;
  }
}
