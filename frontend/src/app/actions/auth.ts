"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import { AUTH_COOKIE } from "@/lib/auth";
import type { ActionState } from "@/lib/forms";
import type { AuthResponse } from "@/types/api";

async function authenticate(
  path: "/api/auth/login" | "/api/auth/register",
  body: Record<string, string>,
): Promise<ActionState> {
  try {
    const result = await apiRequest<AuthResponse>(path, {
      method: "POST",
      body: JSON.stringify(body),
    });
    (await cookies()).set(AUTH_COOKIE, result.accessToken, {
      httpOnly: true,
      sameSite: "lax",
      secure: process.env.NODE_ENV === "production",
      path: "/",
      expires: new Date(result.expiresAt),
    });
  } catch (error) {
    if (error instanceof ApiClientError) {
      return { error: error.detail.message, fieldErrors: error.detail.fieldErrors };
    }
    return { error: "The request could not be completed." };
  }
  redirect(path.endsWith("register") ? "/onboarding" : "/dashboard");
}

export async function loginAction(_: ActionState, formData: FormData): Promise<ActionState> {
  return authenticate("/api/auth/login", {
    email: String(formData.get("email") ?? ""),
    password: String(formData.get("password") ?? ""),
  });
}

export async function registerAction(_: ActionState, formData: FormData): Promise<ActionState> {
  return authenticate("/api/auth/register", {
    displayName: String(formData.get("displayName") ?? ""),
    email: String(formData.get("email") ?? ""),
    password: String(formData.get("password") ?? ""),
  });
}

export async function logoutAction(): Promise<void> {
  (await cookies()).delete(AUTH_COOKIE);
  redirect("/");
}
